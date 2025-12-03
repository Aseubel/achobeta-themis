package com.achobeta.themis.trigger.KnowledgeBase.http;

import com.achobeta.themis.common.ApiResponse;
import com.achobeta.themis.common.PageResponse;
import com.achobeta.themis.domain.user.model.entity.LawCategory;
import com.achobeta.themis.domain.user.model.entity.LawRegulation;
import com.achobeta.themis.infrastructure.user.mapper.LawCategoryMapper;
import com.achobeta.themis.infrastructure.user.mapper.LawRegulationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 法律分类查询Controller
 * 提供国家法规和地方法规的查询功能
 *
 * @Author: ZGjie20
 * @version: 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/law")
@RequiredArgsConstructor
public class LawGet {

    private final LawCategoryMapper lawCategoryMapper;
    private final LawRegulationMapper lawRegulationMapper;

    /**
     * 查询所有国家法规及其关联的法律条款（分页）
     * 
     * @param page 页码（从1开始，默认1）
     * @param size 每页大小（默认10）
     * @return 国家法规分页列表（category_type=1）
     */
    @GetMapping("/national-laws")
    public ApiResponse<PageResponse<LawCategoryWithRegulationsVO>> getNationalLaws(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        try {
            log.info("查询国家法规 - 页码: {}, 每页大小: {}", page, size);
            PageResponse<LawCategoryWithRegulationsVO> result = getLawsByCategoryType(1, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询国家法规失败", e);
            return ApiResponse.error("查询国家法规失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有地方法规及其关联的法律条款（分页）
     * 
     * @param page 页码（从1开始，默认1）
     * @param size 每页大小（默认10）
     * @return 地方法规分页列表（category_type=0）
     */
    @GetMapping("/local-laws")
    public ApiResponse<PageResponse<LawCategoryWithRegulationsVO>> getLocalLaws(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        try {
            log.info("查询地方法规 - 页码: {}, 每页大小: {}", page, size);
            PageResponse<LawCategoryWithRegulationsVO> result = getLawsByCategoryType(0, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询地方法规失败", e);
            return ApiResponse.error("查询地方法规失败: " + e.getMessage());
        }
    }

    /**
     * 根据category_type值查询法律分类及其关联的法律条款（分页）
     * 
     * @param categoryType 1-国家法规，0-地方法规
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 法律分类分页结果
     */
    private PageResponse<LawCategoryWithRegulationsVO> getLawsByCategoryType(Integer categoryType, Integer page, Integer size) {
        // 1. 查询指定category_type的所有法律分类总数
        LambdaQueryWrapper<LawCategory> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(LawCategory::getCategoryType, categoryType);
        Long total = lawCategoryMapper.selectCount(countWrapper);
        
        if (total == null || total == 0) {
            log.warn("未找到category_type={}的法律分类", categoryType);
            return PageResponse.of(new ArrayList<>(), page, size, 0L);
        }
        
        // 2. 分页查询法律分类
        LambdaQueryWrapper<LawCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(LawCategory::getCategoryType, categoryType)
                      .orderByAsc(LawCategory::getLawId)
                      .last("LIMIT " + size + " OFFSET " + ((page - 1) * size));
        List<LawCategory> categories = lawCategoryMapper.selectList(categoryWrapper);
        
        if (categories == null || categories.isEmpty()) {
            return PageResponse.of(new ArrayList<>(), page, size, total);
        }
        
        // 3. 为每个分类查询关联的法律条款
        List<LawCategoryWithRegulationsVO> result = categories.stream().map(category -> {
            // 查询该分类下的所有法律条款
            LambdaQueryWrapper<LawRegulation> regulationWrapper = new LambdaQueryWrapper<>();
            regulationWrapper.eq(LawRegulation::getLawCategoryId, category.getLawId())
                           .orderByAsc(LawRegulation::getArticleNumber);
            List<LawRegulation> regulations = lawRegulationMapper.selectList(regulationWrapper);
            
            // 转换为VO
            List<LawCategoryWithRegulationsVO.RegulationDetailVO> regulationVOs = new ArrayList<>();
            if (regulations != null && !regulations.isEmpty()) {
                regulationVOs = regulations.stream()
                    .map(reg -> LawCategoryWithRegulationsVO.RegulationDetailVO.builder()
                        .regulationId(reg.getRegulationId())
                        .issueYear(reg.getIssueYear())
                        .articleNumber(reg.getArticleNumber())
                        .originalText(reg.getOriginalText())
                        .build())
                    .collect(Collectors.toList());
            }
            
            return LawCategoryWithRegulationsVO.builder()
                    .lawId(category.getLawId())
                    .lawName(category.getLawName())
                    .categoryType(category.getCategoryType())
                    .regulations(regulationVOs)
                    .totalCount(regulationVOs.size())
                    .build();
        }).collect(Collectors.toList());
        
        // 4. 返回分页结果
        return PageResponse.of(result, page, size, total);
    }
    /**
     * 查询具体法条
     * 根据法律分类ID和条款号查询指定法条及其所属法律的所有条款
     * 
     * @param lawId 法律分类ID
     * @param articleNumber 条款号（第n条）
     * @return 法律分类及其所有条款信息，当前查询的条款会包含在列表中
     */
    @GetMapping("/law-regulation")
    public ApiResponse<LawCategoryWithRegulationsVO> getLawRegulation(
            @RequestParam(value = "lawId") Integer lawId,
            @RequestParam(value = "articleNumber") Integer articleNumber
    ) {
        try {
            log.info("查询具体法条 - 法律ID: {}, 条款号: {}", lawId, articleNumber);
            
            // 1. 查询法律分类信息
            LawCategory lawCategory = lawCategoryMapper.selectById(lawId);
            if (lawCategory == null) {
                log.warn("未找到法律分类，lawId: {}", lawId);
                return ApiResponse.error("未找到指定的法律分类");
            }
            
            // 2. 查询指定的法条是否存在
            LawRegulation targetRegulation = lawRegulationMapper.selectOne(
                new LambdaQueryWrapper<LawRegulation>()
                    .eq(LawRegulation::getLawCategoryId, lawId)
                    .eq(LawRegulation::getArticleNumber, articleNumber)
            );
            
            if (targetRegulation == null) {
                log.warn("未找到指定法条 - 法律ID: {}, 条款号: {}", lawId, articleNumber);
                return ApiResponse.error("未找到第 " + articleNumber + " 条法律条款");
            }
            
            // 3. 查询该法律分类下的所有法条（按条款号排序）
            List<LawRegulation> allRegulations = lawRegulationMapper.selectList(
                new LambdaQueryWrapper<LawRegulation>()
                    .eq(LawRegulation::getLawCategoryId, lawId)
                    .orderByAsc(LawRegulation::getArticleNumber)
            );
            
            // 4. 转换为VO
            List<LawCategoryWithRegulationsVO.RegulationDetailVO> regulationVOs = allRegulations.stream()
                .map(reg -> LawCategoryWithRegulationsVO.RegulationDetailVO.builder()
                    .regulationId(reg.getRegulationId())
                    .issueYear(reg.getIssueYear())
                    .articleNumber(reg.getArticleNumber())
                    .originalText(reg.getOriginalText())
                    .build())
                .collect(Collectors.toList());
            
            // 5. 构建响应对象
            LawCategoryWithRegulationsVO result = LawCategoryWithRegulationsVO.builder()
                .lawId(lawCategory.getLawId())
                .lawName(lawCategory.getLawName())
                .categoryType(lawCategory.getCategoryType())
                .regulations(regulationVOs)
                .totalCount(regulationVOs.size())
                .build();
            
            log.info("成功查询法条 - 法律: {}, 条款号: {}, 总条款数: {}", 
                lawCategory.getLawName(), articleNumber, result.getTotalCount());
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询具体法条失败 - 法律ID: {}, 条款号: {}", lawId, articleNumber, e);
            return ApiResponse.error("查询具体法条失败: " + e.getMessage());
        }
    }
    /**
     * 法律分类及其关联法条响应VO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LawCategoryWithRegulationsVO {
        /**
         * 法律分类ID
         */
        private Integer lawId;

        /**
         * 法律分类名称
         */
        private String lawName;

        /**
         * 法律类型：1-国家法规，0-地方法规
         */
        private Integer categoryType;

        /**
         * 关联的法律条文列表
         */
        private List<RegulationDetailVO> regulations;

        /**
         * 法律条文总数
         */
        private Integer totalCount;

        /**
         * 法律条文详情
         */
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RegulationDetailVO {
            /**
             * 法律条文ID
             */
            private Integer regulationId;

            /**
             * 发布年月日
             */
            private String issueYear;

            /**
             * 条款号（第n条）
             */
            private Integer articleNumber;

            /**
             * 法条原文
             */
            private String originalText;
        }
    }
}
