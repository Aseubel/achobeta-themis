package com.achobeta.themis.trigger.file.http;

import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.agent.service.IAiChatService;
import com.achobeta.themis.common.exception.BusinessException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    @Autowired
    private IAiChatService chatService;

    @Autowired
    @Qualifier("redisChatMemoryStore")
    private ChatMemoryStore chatMemoryStore;

    private static final int MAX_TEXT_LENGTH = 20000;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @PostMapping(value = "/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ReviewResult> review(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") @NotBlank(message = "对话ID不能为空") String conversationId
    ) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("上传文件不能为空");
            }
            if (conversationId == null || conversationId.isBlank()) {
                throw new BusinessException("对话ID不能为空");
            }

            String savedPath = saveToLocal(file, conversationId);

            String text = extractText(file);
            if (text == null || text.isBlank()) {
                throw new BusinessException("未能从文件中提取到文本，请检查文件内容或格式");
            }
            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            String prompt = buildReviewPrompt(file.getOriginalFilename(), text);
            Flux<String> stream = chatService.chat(conversationId, prompt);
            List<String> chunks = stream.collectList().block();
            String review = String.join("", chunks);

            return ApiResponse.success(new ReviewResult(conversationId, savedPath, review));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件审查失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 查询对话历史记录
     */
    @GetMapping("/history")
    public ApiResponse<List<ChatHistoryVO>> history(
            @RequestParam("conversationId") @NotBlank(message = "对话ID不能为空") String conversationId
    ) {
        try {
            List<ChatMessage> messages = chatMemoryStore.getMessages(conversationId);
            List<ChatHistoryVO> history = messages.stream()
                    .map(this::toHistory)
                    .collect(Collectors.toList());
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("查询对话历史失败", e);
            return ApiResponse.error("查询对话历史失败: " + e.getMessage());
        }
    }

    /**
     * 重置对话历史状态
     */
    @DeleteMapping("/history")
    public ApiResponse<Void> resetHistory(
            @RequestParam("conversationId") @NotBlank(message = "对话ID不能为空") String conversationId
    ) {
        try {
            chatMemoryStore.deleteMessages(conversationId);
            log.info("已重置对话历史，conversationId: {}", conversationId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("重置对话历史失败", e);
            return ApiResponse.error("重置对话历史失败: " + e.getMessage());
        }
    }

    private String saveToLocal(MultipartFile file, String conversationId) throws IOException {
        Path base = Paths.get("D:\\A\\ruku\\upload");
        Path dir = base.resolve(sanitize(conversationId));
        Files.createDirectories(dir);
        String ts = LocalDateTime.now().format(TS_FMT);
        String original = file.getOriginalFilename();
        String safeName = ts + "_" + sanitize(original == null ? "unknown" : original);
        Path dest = dir.resolve(safeName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("文件已保存到本地：{}", dest.toAbsolutePath());
        return dest.toAbsolutePath().toString();
    }

    private String extractText(MultipartFile file) throws IOException, TikaException, SAXException {
        try (InputStream is = file.getInputStream()) {
            Tika tika = new Tika();
            String text = tika.parseToString(is);
            return text != null ? text.trim() : null;
        }
    }

    private String buildReviewPrompt(String filename, String content) {
        String header =
                "你是一名中国劳动法与劳动合规的专业助手，现需审查以下合同文本。\n" +
                "请严格按照“分级+结构化条目”的规则输出结果：\n\n" +
                "【分级要求】只在存在时呈现以下分级；若三类均不存在，则仅输出：完全合规条款\n" +
                "1) 明显违法：明确违反现行中国劳动法律法规或强制性标准的条款或表述。\n" +
                "2) 缺失必备：法律法规或监管实践中普遍要求但合同中缺失的关键条款（如必备要素、程序性条款）。\n" +
                "3) 存在风险：可能引发争议或合规风险，但未必直接违法的条款或表述。\n\n" +
                "【每个问题项的固定结构】\n" +
                "- 合同原句：\"...原文精确片段...\"\n" +
                "- 相关条款：简要给出对应的法律/法规/司法解释（可概述条文要点）\n" +
                "- 修改建议（个人用户）：站在个人用户视角的可操作修改建议\n" +
                "- 修改建议（企业账号）：站在企业用工合规视角的可操作修改建议\n\n" +
                "【输出格式要求】\n" +
                "- 仅输出存在的分级板块，并按顺序：明显违法、缺失必备、存在风险。\n" +
                "- 每个分级下使用有序列表逐条列出问题项，严格按上述四行结构填写。\n" +
                "- 若三类均不存在，仅输出：完全合规条款\n\n" +
                "文件名：" + (filename == null ? "未知" : filename) + "\n\n" +
                "以下为待审查文本：\n\n";
        return header + content;
    }

    private String sanitize(String s) {
        return s.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private ChatHistoryVO toHistory(ChatMessage message) {
        return new ChatHistoryVO(
                message.type().name(),
                resolveContent(message),
                LocalDateTime.now()
        );
    }

    private String resolveContent(ChatMessage message) {
        try {
            Method textMethod = message.getClass().getMethod("text");
            Object value = textMethod.invoke(message);
            if (value instanceof String text) {
                return text;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        return message.toString();
    }

    @Data
    @AllArgsConstructor
    public static class ReviewResult {
        private String id;
        private String localPath;
        private String review;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatHistoryVO {
        private String role;
        private String content;
        private LocalDateTime timestamp;
    }
}

