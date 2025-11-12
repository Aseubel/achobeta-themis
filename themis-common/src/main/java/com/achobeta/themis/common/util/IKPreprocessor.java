package com.achobeta.themis.common.util;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class IKPreprocessor {
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
}
