
package com.til.util;

import com.til.util.tuple.Ptr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharactersUtil {
    public static String removeSpecialCharacters(String str) {
        String regEx = "[^a-zA-Z0-9]";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("");
    }

    /***
     * 正则表达式匹配中文字符、字母数字和空格的字符
     */
    public static String removeSpecialCharactersExceptChinese(String str) {
        String regEx = "[^\\u4E00-\\u9FA5A-Za-z0-9]";
        Pattern pattern = Pattern.compile(regEx, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("");
    }

    /***
     * 仅保留汉字
     */
    public static String keepChineseCharactersOnly(String str) {
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern pattern = Pattern.compile(regEx, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("");
    }

/*    public static boolean isLicensePlate(String str) {
        // 普通车牌正则（传统燃油车）
        // 格式：汉字 + 发证机关（A-HJ-NP-Z，排除I/O） + 5位字母数字组合（允许字母但排除I/O）
        String normalPlate = "^[\\u4e00-\\u9fa5][A-HJ-NP-Z][A-HJ-NP-Z0-9]{5}$";

        // 新能源车牌正则（2016年后新增）
        // 格式：汉字 + 发证机关 + D/F（新能源标识） + 5位数字
        String newEnergyPlate = "^[\\u4e00-\\u9fa5][A-HJ-NP-Z][DF]\\d{5}$";

        return removeSpecialCharactersExceptChinese(str).matches(normalPlate + "|" + newEnergyPlate);
    }*/

    public static boolean isLicensePlate(String str, Ptr<String> processedPtr) {
        if (str == null || str.isEmpty()) return false;

        // 预处理步骤：处理OCR可能产生的异常字符
        String processed = str
                .toUpperCase() // 统一转大写
                .replaceAll("[^\\u4e00-\\u9fa5A-Z0-9]", "") // 移除非汉字/字母/数字的字符
                .replaceAll("O", "0") // 常见OCR错误：数字0识别为字母O
                .replaceAll("[ⅠΙ]", "I") // 处理特殊字符误识别
                .replaceAll("[Ⅱ]", "II"); // 处理罗马数字误识别

        if (processedPtr != null) {
            processedPtr.setT(processed);
        }

        // 普通车牌正则（调整后）
        String normalPlate = "^["
                + "\\u4e00-\\u9fa5" // 汉字
                + "][A-HJ-NP-Z]"    // 发证机关（排除I/O）-
                + "[A-HJ-NP-Z0-9]{5}$"; // 后续字符

        // 新能源车牌正则（调整后）
        String newEnergyPlate = "^["
                + "\\u4e00-\\u9fa5" // 汉字
                + "][A-HJ-NP-Z]"    // 发证机关
                + "[DF]"            // 新能源标识
                + "\\d{5}$";        // 五位数字

        return processed.matches(normalPlate + "|" + newEnergyPlate);
    }
}
