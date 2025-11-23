package com.achobeta.themis.common;

/**
 * @author Aseubel
 * @date 2025/6/28 下午9:28
 */
public class Constant {
    // 短信验证码有效期
    public static final int SMS_CODE_EXPIRE_MINUTES = 5;
    // 权限认证相关
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // 阿里云OSS相关
    public static final String ENDPOINT = "oss-cn-guangzhou.aliyuncs.com";
    public static final String BUCKET_NAME = "dgut-weave-themis-test";
    //public static final String BUCKET_NAME = "dgut-weave";  // 填写Bucket名称，例如examplebucket。
    public static final String REGION = "cn-guangzhou"; // 填写Bucket所在地域。以华东1（杭州）为例，Region填写为cn-hangzhou。
    public static final long FILE_PART_SIZE = 1 * 1024 * 1024L;
    public static final String APP = "dgut-themis";
    //public static final String APP = "dgut-weave";

    // 系统文件路径 （Windows 下） #TODO Linux下需要修改
    public static final String[] SYSTEM_FONTS = {
            "C:\\Windows\\Fonts\\simsun.ttc",
            "C:\\Windows\\Fonts\\simsun.ttf",
            "C:\\Windows\\Fonts\\msyh.ttf",
            "C:\\Windows\\Fonts\\simhei.ttf"
    };
    public static final String SYSTEM_LOCAL_PATH = "D:\\A\\ruku\\upload";

    // 系统字体路径（Linux 下）
//    public static final String[] SYSTEM_FONTS = {
//            "/usr/share/fonts/truetype/simsun/simsun.ttc",       // 宋体（Linux下常见路径）
//            "/usr/share/fonts/truetype/microsoft/msyh.ttf",      // 微软雅黑（需安装Windows字体包）
//            "/usr/share/fonts/truetype/simhei/simhei.ttf",       // 黑体（Linux下常见路径）
//            "/usr/share/fonts/truetype/noto/NotoSerifCJKsc.ttf"  // 备选：Linux原生中文字体（Noto宋体）
//    };
//    public static final String SYSTEM_LOCAL_PATH = "/data/ruku/upload";  // Linux本地存储路径（建议）

    // meilisearch相关
     public static final String QUESTION_TITLE_DOCUMENTS = "question_title_documents";
     public static final String KNOWLEDGE_BASE_QUESTION_DOCUMENTS = "knowledge_base_question_documents";



     // 个性化的系统提示词 TODO

     // 问题-法律-热点-场景
     public static final String KNOWLEDGE_BASE_INSERT_SYSTEM_PROMPT = "你是一个信息填充机器人，你的任务是根据userQuestion和与问题相关的法律文件列表的regulationID，regulationContent，填充出JSON格式数据。" +
             "并严格按照JSON格式输出。（regulationID为整数，regulationContent为字符串，aiTranslation为字符串，relevantCases为数组，relevantQuestions为数组）" +
             "{\n" +
             "     \"topic\": \"\",\n" +
             "     \"caseBackground\": \"\",\n" +
             "     \"regulationList\": [\n" +
             "         {\n" +
             "             \"regulationID\": ,\n" +
             "             \"aiTranslation\": \"\",\n" +
             "             \"relevantCases\": [\n" +
             "                 {\n" +
             "                     \"caseContent\": \"\",\n" +
             "                     \"caseLink\": \"\"\n" +
             "                 }\n" +
             "             ],\n" +
             "             \"relevantQuestions\": [\n" +
             "                 \"\"\n" +
             "             ]\n" +
             "         }\n" +
             "     ]\n" +
             "}";

    /**
     * {
     *      "topic": "",
     *      "caseBackground": "",
     *      "regulationList": [
     *          {
     *              "regulationID": ,
     *              "aiTranslation": "",
     *              "relevantCases": [
     *                  {
     *                      "caseContent": "",
     *                      "caseLink": ""
     *                  }
     *              ],
     *              "relevantQuestions": [
     *                  ""
     *              ]
     *          }
     *      ]
     * }
     */

    // 知识库搜索历史记录key
     public static final String KNOWLEDGE_BASE_SEARCH_HISTORY_KEY = "knowledge_base_search_history:";

}
