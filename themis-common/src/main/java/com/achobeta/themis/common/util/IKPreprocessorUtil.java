package com.achobeta.themis.common.util;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class IKPreprocessorUtil {
    /**
     * 对文本进行IK分词处理
     * @param text 待分词的文本
     * @param useSmart 是否使用智能分词模式
     * @return 分词后的文本，词语之间用空格分隔
     * @throws Exception 如果分词过程中发生错误
     */
    public static String segment(String text, boolean useSmart) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        StringReader reader = new StringReader(text);
        IKSegmenter segmenter = new IKSegmenter(reader, useSmart);
        List<String> words = new ArrayList<>();
        Lexeme lexeme;
        while ((lexeme = segmenter.next()) != null) {
            words.add(lexeme.getLexemeText());
        }
        return String.join(" ", words);
    }

    /**
     * 分词方法：保留复合词（非智能模式，避免拆分），返回空格分隔的整词
     * @param text 待分词文本
     * @param useSmart 是否使用智能模式（建议传false，保留复合词）
     * @return 空格分隔的分词结果
     * @throws Exception 分词异常
     */
    public static String stopWordSegment(String text, boolean useSmart) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        StringReader reader = new StringReader(text);
        IKSegmenter segmenter = new IKSegmenter(reader, useSmart);
        List<String> words = new ArrayList<>();
        Lexeme lexeme;
        while ((lexeme = segmenter.next()) != null) {
            String word = lexeme.getLexemeText();
            if (!isStopWord(word)) {
                words.add(word);
            }
        }
        return String.join(" ", words);
    }

    /**
     * 停用词过滤（可选，进一步减少干扰词）
     */
    private static boolean isStopWord(String word) {
        // 可配置停用词表，这里示例过滤常见无意义词
        List<String> stopWords = List.of("对于", "来说", "双方", "《", "》", "呢", "谁", "劳动");
        return stopWords.contains(word);
    }


}
