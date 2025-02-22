
package com.til.util;

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
     * 正则表达式匹配非中文字符、非字母数字和非空格的字符
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

}
