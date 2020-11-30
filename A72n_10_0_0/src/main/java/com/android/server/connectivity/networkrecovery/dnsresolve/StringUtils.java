package com.android.server.connectivity.networkrecovery.dnsresolve;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String CR = "\r";
    public static final String EMPTY = "";
    public static final int INDEX_NOT_FOUND = -1;
    public static final String LF = "\n";
    private static final int PAD_LIMIT = 8192;
    public static final String SPACE = " ";
    private static final int STRING_BUILDER_SIZE = 256;

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String trim(String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    public static String trimToNull(String str) {
        String ts = trim(str);
        if (isEmpty(ts)) {
            return null;
        }
        return ts;
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    public static String truncate(String str, int maxWidth) {
        return truncate(str, 0, maxWidth);
    }

    public static String truncate(String str, int offset, int maxWidth) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        } else if (maxWidth < 0) {
            throw new IllegalArgumentException("maxWith cannot be negative");
        } else if (str == null) {
            return null;
        } else {
            if (offset > str.length()) {
                return "";
            }
            if (str.length() <= maxWidth) {
                return str.substring(offset);
            }
            return str.substring(offset, offset + maxWidth > str.length() ? str.length() : offset + maxWidth);
        }
    }

    public static String strip(String str) {
        return strip(str, null);
    }

    public static String stripToNull(String str) {
        if (str == null) {
            return null;
        }
        String str2 = strip(str, null);
        if (str2.isEmpty()) {
            return null;
        }
        return str2;
    }

    public static String stripToEmpty(String str) {
        return str == null ? "" : strip(str, null);
    }

    public static String strip(String str, String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        return stripEnd(stripStart(str, stripChars), stripChars);
    }

    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
                start++;
            }
        }
        return str.substring(start);
    }

    public static String stripEnd(String str, String stripChars) {
        if (str != null) {
            int length = str.length();
            int end = length;
            if (length != 0) {
                if (stripChars == null) {
                    while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                        end--;
                    }
                } else if (stripChars.isEmpty()) {
                    return str;
                } else {
                    while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
                        end--;
                    }
                }
                return str.substring(0, end);
            }
        }
        return str;
    }

    public static String[] stripAll(String... strs) {
        return stripAll(strs, null);
    }

    public static String[] stripAll(String[] strs, String stripChars) {
        int strsLen;
        if (strs == null || (strsLen = strs.length) == 0) {
            return strs;
        }
        String[] newArr = new String[strsLen];
        for (int i = 0; i < strsLen; i++) {
            newArr[i] = strip(strs[i], stripChars);
        }
        return newArr;
    }

    public static String stripAccents(String input) {
        if (input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        StringBuilder decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        convertRemainingAccentCharacters(decomposed);
        return pattern.matcher(decomposed).replaceAll("");
    }

    private static void convertRemainingAccentCharacters(StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); i++) {
            if (decomposed.charAt(i) == 321) {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'L');
            } else if (decomposed.charAt(i) == 322) {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'l');
            }
        }
    }

    public static int compare(String str1, String str2) {
        return compare(str1, str2, true);
    }

    public static int compare(String str1, String str2, boolean nullIsLess) {
        if (str1 == str2) {
            return 0;
        }
        if (str1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (str2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return str1.compareTo(str2);
    }

    public static int compareIgnoreCase(String str1, String str2) {
        return compareIgnoreCase(str1, str2, true);
    }

    public static int compareIgnoreCase(String str1, String str2, boolean nullIsLess) {
        if (str1 == str2) {
            return 0;
        }
        if (str1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (str2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return str1.compareToIgnoreCase(str2);
    }

    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start += str.length();
        }
        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return "";
        }
        return str.substring(start);
    }

    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        if (end < 0) {
            end += str.length();
        }
        if (start < 0) {
            start += str.length();
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return "";
        }
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        return str.substring(start, end);
    }

    public static String left(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return "";
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    public static String right(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return "";
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    public static String mid(String str, int pos, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return "";
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= pos + len) {
            return str.substring(pos);
        }
        return str.substring(pos, pos + len);
    }

    private static StringBuilder newStringBuilder(int noOfItems) {
        return new StringBuilder(noOfItems * 16);
    }

    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return "";
        }
        int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringAfter(String str, String separator) {
        int pos;
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null || (pos = str.indexOf(separator)) == -1) {
            return "";
        }
        return str.substring(separator.length() + pos);
    }

    public static String substringBeforeLast(String str, String separator) {
        int pos;
        if (isEmpty(str) || isEmpty(separator) || (pos = str.lastIndexOf(separator)) == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringAfterLast(String str, String separator) {
        int pos;
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator) || (pos = str.lastIndexOf(separator)) == -1 || pos == str.length() - separator.length()) {
            return "";
        }
        return str.substring(separator.length() + pos);
    }

    public static String substringBetween(String str, String tag) {
        return substringBetween(str, tag, tag);
    }

    public static String substringBetween(String str, String open, String close) {
        int start;
        int end;
        if (str == null || open == null || close == null || (start = str.indexOf(open)) == -1 || (end = str.indexOf(close, open.length() + start)) == -1) {
            return null;
        }
        return str.substring(open.length() + start, end);
    }

    @SafeVarargs
    public static <T> String join(T... elements) {
        return join(elements, (String) null);
    }

    public static String join(Object[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(long[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(int[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(short[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(byte[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(char[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(float[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(double[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public static String join(long[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String join(int[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String join(byte[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append((int) array[i]);
        }
        return buf.toString();
    }

    public static String join(short[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append((int) array[i]);
        }
        return buf.toString();
    }

    public static String join(char[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String join(double[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String join(float[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return "";
        }
        StringBuilder buf = newStringBuilder(noOfItems);
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public static String join(Iterator<?> iterator, char separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first, "");
        }
        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }
        while (iterator.hasNext()) {
            buf.append(separator);
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first, "");
        }
        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }
        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    public static String join(Iterable<?> iterable, char separator) {
        if (iterable == null) {
            return null;
        }
        return join(iterable.iterator(), separator);
    }

    public static String join(Iterable<?> iterable, String separator) {
        if (iterable == null) {
            return null;
        }
        return join(iterable.iterator(), separator);
    }

    public static String join(List<?> list, char separator, int startIndex, int endIndex) {
        if (list == null) {
            return null;
        }
        if (endIndex - startIndex <= 0) {
            return "";
        }
        return join(list.subList(startIndex, endIndex).iterator(), separator);
    }

    public static String join(List<?> list, String separator, int startIndex, int endIndex) {
        if (list == null) {
            return null;
        }
        if (endIndex - startIndex <= 0) {
            return "";
        }
        return join(list.subList(startIndex, endIndex).iterator(), separator);
    }

    public static String joinWith(String separator, Object... objects) {
        if (objects != null) {
            String sanitizedSeparator = defaultString(separator);
            StringBuilder result = new StringBuilder();
            Iterator<Object> iterator = Arrays.asList(objects).iterator();
            while (iterator.hasNext()) {
                result.append(Objects.toString(iterator.next(), ""));
                if (iterator.hasNext()) {
                    result.append(sanitizedSeparator);
                }
            }
            return result.toString();
        }
        throw new IllegalArgumentException("Object varargs must not be null");
    }

    public static String deleteWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count] = str.charAt(i);
                count++;
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }

    public static String removeStart(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove) || !str.startsWith(remove)) {
            return str;
        }
        return str.substring(remove.length());
    }

    public static String removeEnd(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove) || !str.endsWith(remove)) {
            return str;
        }
        return str.substring(0, str.length() - remove.length());
    }

    public static String remove(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replace(str, remove, "", -1);
    }

    public static String removeIgnoreCase(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replaceIgnoreCase(str, remove, "", -1);
    }

    public static String remove(String str, char remove) {
        if (isEmpty(str) || str.indexOf(remove) == -1) {
            return str;
        }
        char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos] = chars[i];
                pos++;
            }
        }
        return new String(chars, 0, pos);
    }

    public static String replaceOnce(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, 1);
    }

    public static String replaceOnceIgnoreCase(String text, String searchString, String replacement) {
        return replaceIgnoreCase(text, searchString, replacement, 1);
    }

    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    public static String replaceIgnoreCase(String text, String searchString, String replacement) {
        return replaceIgnoreCase(text, searchString, replacement, -1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        return replace(text, searchString, replacement, max, false);
    }

    private static String replace(String text, String searchString, String replacement, int max, boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        String searchText = text;
        if (ignoreCase) {
            searchText = text.toLowerCase();
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = searchText.indexOf(searchString, 0);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        int increase2 = increase < 0 ? 0 : increase;
        int i = 64;
        if (max < 0) {
            i = 16;
        } else if (max <= 64) {
            i = max;
        }
        StringBuilder buf = new StringBuilder(text.length() + (increase2 * i));
        while (end != -1) {
            buf.append((CharSequence) text, start, end);
            buf.append(replacement);
            start = end + replLength;
            max--;
            if (max == 0) {
                break;
            }
            end = searchText.indexOf(searchString, start);
        }
        buf.append((CharSequence) text, start, text.length());
        return buf.toString();
    }

    public static String replaceIgnoreCase(String text, String searchString, String replacement, int max) {
        return replace(text, searchString, replacement, max, true);
    }

    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        return replaceEach(text, searchList, replacementList, false, 0);
    }

    public static String replaceEachRepeatedly(String text, String[] searchList, String[] replacementList) {
        return replaceEach(text, searchList, replacementList, true, searchList == null ? 0 : searchList.length);
    }

    private static String replaceEach(String text, String[] searchList, String[] replacementList, boolean repeat, int timeToLive) {
        int greater;
        if (text == null || text.isEmpty() || searchList == null || searchList.length == 0 || replacementList == null || replacementList.length == 0) {
            return text;
        }
        if (timeToLive >= 0) {
            int searchLength = searchList.length;
            int replacementLength = replacementList.length;
            if (searchLength == replacementLength) {
                boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];
                int textIndex = -1;
                int replaceIndex = -1;
                for (int i = 0; i < searchLength; i++) {
                    if (!noMoreMatchesForReplIndex[i] && searchList[i] != null && !searchList[i].isEmpty() && replacementList[i] != null) {
                        int tempIndex = text.indexOf(searchList[i]);
                        if (tempIndex == -1) {
                            noMoreMatchesForReplIndex[i] = true;
                        } else if (textIndex == -1 || tempIndex < textIndex) {
                            textIndex = tempIndex;
                            replaceIndex = i;
                        }
                    }
                }
                if (textIndex == -1) {
                    return text;
                }
                int start = 0;
                int increase = 0;
                for (int i2 = 0; i2 < searchList.length; i2++) {
                    if (!(searchList[i2] == null || replacementList[i2] == null || (greater = replacementList[i2].length() - searchList[i2].length()) <= 0)) {
                        increase += greater * 3;
                    }
                }
                StringBuilder buf = new StringBuilder(text.length() + Math.min(increase, text.length() / 5));
                while (textIndex != -1) {
                    for (int i3 = start; i3 < textIndex; i3++) {
                        buf.append(text.charAt(i3));
                    }
                    buf.append(replacementList[replaceIndex]);
                    start = textIndex + searchList[replaceIndex].length();
                    textIndex = -1;
                    replaceIndex = -1;
                    for (int i4 = 0; i4 < searchLength; i4++) {
                        if (!noMoreMatchesForReplIndex[i4] && searchList[i4] != null) {
                            if (!searchList[i4].isEmpty()) {
                                if (replacementList[i4] != null) {
                                    int tempIndex2 = text.indexOf(searchList[i4], start);
                                    if (tempIndex2 == -1) {
                                        noMoreMatchesForReplIndex[i4] = true;
                                    } else if (textIndex == -1 || tempIndex2 < textIndex) {
                                        textIndex = tempIndex2;
                                        replaceIndex = i4;
                                    }
                                }
                            }
                        }
                    }
                }
                int textLength = text.length();
                for (int i5 = start; i5 < textLength; i5++) {
                    buf.append(text.charAt(i5));
                }
                String result = buf.toString();
                if (!repeat) {
                    return result;
                }
                return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
            }
            throw new IllegalArgumentException("Search and Replace array lengths don't match: " + searchLength + " vs " + replacementLength);
        }
        throw new IllegalStateException("Aborting to protect against StackOverflowError - output of one loop is the input of another");
    }

    public static String replaceChars(String str, char searchChar, char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    public static String replaceChars(String str, String searchChars, String replaceChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return str;
        }
        if (replaceChars == null) {
            replaceChars = "";
        }
        boolean modified = false;
        int replaceCharsLength = replaceChars.length();
        int strLength = str.length();
        StringBuilder buf = new StringBuilder(strLength);
        for (int i = 0; i < strLength; i++) {
            char ch = str.charAt(i);
            int index = searchChars.indexOf(ch);
            if (index >= 0) {
                modified = true;
                if (index < replaceCharsLength) {
                    buf.append(replaceChars.charAt(index));
                }
            } else {
                buf.append(ch);
            }
        }
        if (modified) {
            return buf.toString();
        }
        return str;
    }

    public static String overlay(String str, String overlay, int start, int end) {
        if (str == null) {
            return null;
        }
        if (overlay == null) {
            overlay = "";
        }
        int len = str.length();
        if (start < 0) {
            start = 0;
        }
        if (start > len) {
            start = len;
        }
        if (end < 0) {
            end = 0;
        }
        if (end > len) {
            end = len;
        }
        if (start > end) {
            start = end;
            end = start;
        }
        return str.substring(0, start) + overlay + str.substring(end);
    }

    @Deprecated
    public static String chomp(String str, String separator) {
        return removeEnd(str, separator);
    }

    public static String repeat(String str, int repeat) {
        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return "";
        }
        int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= 8192) {
            return repeat(str.charAt(0), repeat);
        }
        int outputLength = inputLength * repeat;
        if (inputLength == 1) {
            return repeat(str.charAt(0), repeat);
        }
        if (inputLength != 2) {
            StringBuilder buf = new StringBuilder(outputLength);
            for (int i = 0; i < repeat; i++) {
                buf.append(str);
            }
            return buf.toString();
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        char[] output2 = new char[outputLength];
        for (int i2 = (repeat * 2) - 2; i2 >= 0; i2 = (i2 - 1) - 1) {
            output2[i2] = ch0;
            output2[i2 + 1] = ch1;
        }
        return new String(output2);
    }

    public static String repeat(String str, String separator, int repeat) {
        if (str == null || separator == null) {
            return repeat(str, repeat);
        }
        return removeEnd(repeat(str + separator, repeat), separator);
    }

    public static String repeat(char ch, int repeat) {
        if (repeat <= 0) {
            return "";
        }
        char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    public static String rightPad(String str, int size) {
        return rightPad(str, size, ' ');
    }

    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > 8192) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(repeat(padChar, pads));
    }

    public static String rightPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        int padLen = padStr.length();
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= 8192) {
            return rightPad(str, size, padStr.charAt(0));
        }
        if (pads == padLen) {
            return str.concat(padStr);
        }
        if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        }
        char[] padding = new char[pads];
        char[] padChars = padStr.toCharArray();
        for (int i = 0; i < pads; i++) {
            padding[i] = padChars[i % padLen];
        }
        return str.concat(new String(padding));
    }

    public static String leftPad(String str, int size) {
        return leftPad(str, size, ' ');
    }

    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > 8192) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        int padLen = padStr.length();
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= 8192) {
            return leftPad(str, size, padStr.charAt(0));
        }
        if (pads == padLen) {
            return padStr.concat(str);
        }
        if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        }
        char[] padding = new char[pads];
        char[] padChars = padStr.toCharArray();
        for (int i = 0; i < pads; i++) {
            padding[i] = padChars[i % padLen];
        }
        return new String(padding).concat(str);
    }

    public static int length(CharSequence cs) {
        if (cs == null) {
            return 0;
        }
        return cs.length();
    }

    public static String center(String str, int size) {
        return center(str, size, ' ');
    }

    public static String center(String str, int size, char padChar) {
        int strLen;
        int pads;
        if (str == null || size <= 0 || (pads = size - (strLen = str.length())) <= 0) {
            return str;
        }
        return rightPad(leftPad(str, (pads / 2) + strLen, padChar), size, padChar);
    }

    public static String center(String str, int size, String padStr) {
        if (str == null || size <= 0) {
            return str;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        return rightPad(leftPad(str, (pads / 2) + strLen, padStr), size, padStr);
    }

    public static String upperCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
    }

    public static String upperCase(String str, Locale locale) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase(locale);
    }

    public static String lowerCase(String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
    }

    public static String lowerCase(String str, Locale locale) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase(locale);
    }

    public static String capitalize(String str) {
        int strLen;
        int firstCodepoint;
        int newCodePoint;
        if (str == null || (strLen = str.length()) == 0 || firstCodepoint == (newCodePoint = Character.toTitleCase((firstCodepoint = str.codePointAt(0))))) {
            return str;
        }
        int[] newCodePoints = new int[strLen];
        int outOffset = 0 + 1;
        newCodePoints[0] = newCodePoint;
        int inOffset = Character.charCount(firstCodepoint);
        while (inOffset < strLen) {
            int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset] = codepoint;
            inOffset += Character.charCount(codepoint);
            outOffset++;
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static String uncapitalize(String str) {
        int strLen;
        int firstCodepoint;
        int newCodePoint;
        if (str == null || (strLen = str.length()) == 0 || firstCodepoint == (newCodePoint = Character.toLowerCase((firstCodepoint = str.codePointAt(0))))) {
            return str;
        }
        int[] newCodePoints = new int[strLen];
        int outOffset = 0 + 1;
        newCodePoints[0] = newCodePoint;
        int inOffset = Character.charCount(firstCodepoint);
        while (inOffset < strLen) {
            int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset] = codepoint;
            inOffset += Character.charCount(codepoint);
            outOffset++;
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static String swapCase(String str) {
        int newCodePoint;
        if (isEmpty(str)) {
            return str;
        }
        int strLen = str.length();
        int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        int i = 0;
        while (i < strLen) {
            int oldCodepoint = str.codePointAt(i);
            if (Character.isUpperCase(oldCodepoint)) {
                newCodePoint = Character.toLowerCase(oldCodepoint);
            } else if (Character.isTitleCase(oldCodepoint)) {
                newCodePoint = Character.toLowerCase(oldCodepoint);
            } else if (Character.isLowerCase(oldCodepoint)) {
                newCodePoint = Character.toUpperCase(oldCodepoint);
            } else {
                newCodePoint = oldCodepoint;
            }
            newCodePoints[outOffset] = newCodePoint;
            i += Character.charCount(newCodePoint);
            outOffset++;
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static int countMatches(CharSequence str, char ch) {
        if (isEmpty(str)) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (ch == str.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    public static boolean isAlpha(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetter(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlphaSpace(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!(Character.isLetter(cs.charAt(i)) || cs.charAt(i) == ' ')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlphanumeric(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetterOrDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAlphanumericSpace(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!(Character.isLetterOrDigit(cs.charAt(i)) || cs.charAt(i) == ' ')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumeric(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumericSpace(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!(Character.isDigit(cs.charAt(i)) || cs.charAt(i) == ' ')) {
                return false;
            }
        }
        return true;
    }

    public static String getDigits(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        StringBuilder strDigits = new StringBuilder(sz);
        for (int i = 0; i < sz; i++) {
            char tempChar = str.charAt(i);
            if (Character.isDigit(tempChar)) {
                strDigits.append(tempChar);
            }
        }
        return strDigits.toString();
    }

    public static boolean isWhitespace(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllLowerCase(CharSequence cs) {
        if (cs == null || isEmpty(cs)) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLowerCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllUpperCase(CharSequence cs) {
        if (cs == null || isEmpty(cs)) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isUpperCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isMixedCase(CharSequence cs) {
        if (isEmpty(cs) || cs.length() == 1) {
            return false;
        }
        boolean containsUppercase = false;
        boolean containsLowercase = false;
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (containsUppercase && containsLowercase) {
                return true;
            }
            if (Character.isUpperCase(cs.charAt(i))) {
                containsUppercase = true;
            } else if (Character.isLowerCase(cs.charAt(i))) {
                containsLowercase = true;
            }
        }
        return containsUppercase && containsLowercase;
    }

    public static String defaultString(String str) {
        return defaultString(str, "");
    }

    public static String defaultString(String str, String defaultStr) {
        return str == null ? defaultStr : str;
    }

    @SafeVarargs
    public static <T extends CharSequence> T firstNonBlank(T... values) {
        if (values == null) {
            return null;
        }
        for (T val : values) {
            if (isNotBlank(val)) {
                return val;
            }
        }
        return null;
    }

    @SafeVarargs
    public static <T extends CharSequence> T firstNonEmpty(T... values) {
        if (values == null) {
            return null;
        }
        for (T val : values) {
            if (isNotEmpty(val)) {
                return val;
            }
        }
        return null;
    }

    public static <T extends CharSequence> T defaultIfBlank(T str, T defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    public static <T extends CharSequence> T defaultIfEmpty(T str, T defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    public static String rotate(String str, int shift) {
        if (str == null) {
            return null;
        }
        int strLen = str.length();
        if (shift == 0 || strLen == 0 || shift % strLen == 0) {
            return str;
        }
        StringBuilder builder = new StringBuilder(strLen);
        int offset = -(shift % strLen);
        builder.append(substring(str, offset));
        builder.append(substring(str, 0, offset));
        return builder.toString();
    }

    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, "...", 0, maxWidth);
    }

    public static String abbreviate(String str, int offset, int maxWidth) {
        return abbreviate(str, "...", offset, maxWidth);
    }

    public static String abbreviate(String str, String abbrevMarker, int maxWidth) {
        return abbreviate(str, abbrevMarker, 0, maxWidth);
    }

    public static String abbreviate(String str, String abbrevMarker, int offset, int maxWidth) {
        if (isEmpty(str) || isEmpty(abbrevMarker)) {
            return str;
        }
        int abbrevMarkerLength = abbrevMarker.length();
        int minAbbrevWidth = abbrevMarkerLength + 1;
        int minAbbrevWidthOffset = abbrevMarkerLength + abbrevMarkerLength + 1;
        if (maxWidth < minAbbrevWidth) {
            throw new IllegalArgumentException(String.format("Minimum abbreviation width is %d", Integer.valueOf(minAbbrevWidth)));
        } else if (str.length() <= maxWidth) {
            return str;
        } else {
            if (offset > str.length()) {
                offset = str.length();
            }
            if (str.length() - offset < maxWidth - abbrevMarkerLength) {
                offset = str.length() - (maxWidth - abbrevMarkerLength);
            }
            if (offset <= abbrevMarkerLength + 1) {
                return str.substring(0, maxWidth - abbrevMarkerLength) + abbrevMarker;
            } else if (maxWidth < minAbbrevWidthOffset) {
                throw new IllegalArgumentException(String.format("Minimum abbreviation width with offset is %d", Integer.valueOf(minAbbrevWidthOffset)));
            } else if ((offset + maxWidth) - abbrevMarkerLength < str.length()) {
                return abbrevMarker + abbreviate(str.substring(offset), abbrevMarker, maxWidth - abbrevMarkerLength);
            } else {
                return abbrevMarker + str.substring(str.length() - (maxWidth - abbrevMarkerLength));
            }
        }
    }

    public static String abbreviateMiddle(String str, String middle, int length) {
        if (isEmpty(str) || isEmpty(middle) || length >= str.length() || length < middle.length() + 2) {
            return str;
        }
        int targetSting = length - middle.length();
        int endOffset = str.length() - (targetSting / 2);
        return str.substring(0, (targetSting / 2) + (targetSting % 2)) + middle + str.substring(endOffset);
    }

    public static String difference(String str1, String str2) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        int at = indexOfDifference(str1, str2);
        if (at == -1) {
            return "";
        }
        return str2.substring(at);
    }

    public static int indexOfDifference(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return -1;
        }
        if (cs1 == null || cs2 == null) {
            return 0;
        }
        int i = 0;
        while (i < cs1.length() && i < cs2.length() && cs1.charAt(i) == cs2.charAt(i)) {
            i++;
        }
        if (i < cs2.length() || i < cs1.length()) {
            return i;
        }
        return -1;
    }

    public static int indexOfDifference(CharSequence... css) {
        if (css == null || css.length <= 1) {
            return -1;
        }
        boolean allStringsNull = true;
        int arrayLen = css.length;
        int shortestStrLen = Integer.MAX_VALUE;
        int longestStrLen = 0;
        boolean anyStringNull = false;
        for (CharSequence cs : css) {
            if (cs == null) {
                anyStringNull = true;
                shortestStrLen = 0;
            } else {
                allStringsNull = false;
                shortestStrLen = Math.min(cs.length(), shortestStrLen);
                longestStrLen = Math.max(cs.length(), longestStrLen);
            }
        }
        if (allStringsNull || (longestStrLen == 0 && !anyStringNull)) {
            return -1;
        }
        if (shortestStrLen == 0) {
            return 0;
        }
        int firstDiff = -1;
        for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
            char comparisonChar = css[0].charAt(stringPos);
            int arrayPos = 1;
            while (true) {
                if (arrayPos >= arrayLen) {
                    break;
                } else if (css[arrayPos].charAt(stringPos) != comparisonChar) {
                    firstDiff = stringPos;
                    break;
                } else {
                    arrayPos++;
                }
            }
            if (firstDiff != -1) {
                break;
            }
        }
        if (firstDiff != -1 || shortestStrLen == longestStrLen) {
            return firstDiff;
        }
        return shortestStrLen;
    }

    public static String getCommonPrefix(String... strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }
        int smallestIndexOfDiff = indexOfDifference(strs);
        if (smallestIndexOfDiff == -1) {
            if (strs[0] == null) {
                return "";
            }
            return strs[0];
        } else if (smallestIndexOfDiff == 0) {
            return "";
        } else {
            return strs[0].substring(0, smallestIndexOfDiff);
        }
    }

    @Deprecated
    public static int getLevenshteinDistance(CharSequence s, CharSequence t) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        int n = s.length();
        int m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        if (n > m) {
            s = t;
            t = s;
            n = m;
            m = t.length();
        }
        int[] p = new int[(n + 1)];
        for (int i = 0; i <= n; i++) {
            p[i] = i;
        }
        for (int j = 1; j <= m; j++) {
            int upper_left = p[0];
            char t_j = t.charAt(j - 1);
            p[0] = j;
            for (int i2 = 1; i2 <= n; i2++) {
                int upper = p[i2];
                p[i2] = Math.min(Math.min(p[i2 - 1] + 1, p[i2] + 1), upper_left + (s.charAt(i2 + -1) == t_j ? 0 : 1));
                upper_left = upper;
            }
        }
        return p[n];
    }

    @Deprecated
    public static int getLevenshteinDistance(CharSequence s, CharSequence t, int threshold) {
        CharSequence t2;
        CharSequence s2;
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        } else if (threshold >= 0) {
            int n = s.length();
            int m = t.length();
            int i = -1;
            if (n == 0) {
                if (m <= threshold) {
                    return m;
                }
                return -1;
            } else if (m == 0) {
                if (n <= threshold) {
                    return n;
                }
                return -1;
            } else if (Math.abs(n - m) > threshold) {
                return -1;
            } else {
                if (n > m) {
                    s2 = t;
                    t2 = s;
                    n = m;
                    m = t2.length();
                } else {
                    s2 = s;
                    t2 = t;
                }
                int[] p = new int[(n + 1)];
                int[] d = new int[(n + 1)];
                int boundary = Math.min(n, threshold) + 1;
                for (int i2 = 0; i2 < boundary; i2++) {
                    p[i2] = i2;
                }
                int i3 = Integer.MAX_VALUE;
                Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
                Arrays.fill(d, Integer.MAX_VALUE);
                int j = 1;
                while (j <= m) {
                    char t_j = t2.charAt(j - 1);
                    d[0] = j;
                    int min = Math.max(1, j - threshold);
                    int max = j > i3 - threshold ? n : Math.min(n, j + threshold);
                    if (min > max) {
                        return i;
                    }
                    if (min > 1) {
                        d[min - 1] = i3;
                    }
                    for (int i4 = min; i4 <= max; i4++) {
                        if (s2.charAt(i4 - 1) == t_j) {
                            d[i4] = p[i4 - 1];
                        } else {
                            d[i4] = Math.min(Math.min(d[i4 - 1], p[i4]), p[i4 - 1]) + 1;
                        }
                    }
                    p = d;
                    d = p;
                    j++;
                    i = -1;
                    i3 = Integer.MAX_VALUE;
                }
                if (p[n] <= threshold) {
                    return p[n];
                }
                return -1;
            }
        } else {
            throw new IllegalArgumentException("Threshold must not be negative");
        }
    }

    @Deprecated
    public static double getJaroWinklerDistance(CharSequence first, CharSequence second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        int[] mtp = matches(first, second);
        double m = (double) mtp[0];
        if (m == 0.0d) {
            return 0.0d;
        }
        double j = (((m / ((double) first.length())) + (m / ((double) second.length()))) + ((m - ((double) mtp[1])) / m)) / 3.0d;
        return ((double) Math.round((j < 0.7d ? j : ((Math.min(0.1d, 1.0d / ((double) mtp[3])) * ((double) mtp[2])) * (1.0d - j)) + j) * 100.0d)) / 100.0d;
    }

    /* JADX INFO: Multiple debug info for r10v2 char[]: [D('mi' int), D('ms1' char[])] */
    private static int[] matches(CharSequence first, CharSequence second) {
        CharSequence min;
        CharSequence max;
        if (first.length() > second.length()) {
            max = first;
            min = second;
        } else {
            max = second;
            min = first;
        }
        int range = Math.max((max.length() / 2) - 1, 0);
        int[] matchIndexes = new int[min.length()];
        Arrays.fill(matchIndexes, -1);
        boolean[] matchFlags = new boolean[max.length()];
        int matches = 0;
        for (int mi = 0; mi < min.length(); mi++) {
            char c1 = min.charAt(mi);
            int xi = Math.max(mi - range, 0);
            int xn = Math.min(mi + range + 1, max.length());
            while (true) {
                if (xi >= xn) {
                    break;
                }
                if (!matchFlags[xi] && c1 == max.charAt(xi)) {
                    matchIndexes[mi] = xi;
                    matchFlags[xi] = true;
                    matches++;
                    break;
                }
                xi++;
            }
        }
        char[] ms1 = new char[matches];
        char[] ms2 = new char[matches];
        int si = 0;
        for (int i = 0; i < min.length(); i++) {
            if (matchIndexes[i] != -1) {
                ms1[si] = min.charAt(i);
                si++;
            }
        }
        int si2 = 0;
        for (int i2 = 0; i2 < max.length(); i2++) {
            if (matchFlags[i2]) {
                ms2[si2] = max.charAt(i2);
                si2++;
            }
        }
        int transpositions = 0;
        for (int mi2 = 0; mi2 < ms1.length; mi2++) {
            if (ms1[mi2] != ms2[mi2]) {
                transpositions++;
            }
        }
        int prefix = 0;
        int mi3 = 0;
        while (true) {
            if (mi3 >= min.length()) {
                break;
            }
            if (first.charAt(mi3) != second.charAt(mi3)) {
                break;
            }
            prefix++;
            mi3++;
        }
        return new int[]{matches, transpositions / 2, prefix, max.length()};
    }

    @Deprecated
    public static int getFuzzyDistance(CharSequence term, CharSequence query, Locale locale) {
        if (term == null || query == null) {
            throw new IllegalArgumentException("Strings must not be null");
        } else if (locale != null) {
            String termLowerCase = term.toString().toLowerCase(locale);
            String queryLowerCase = query.toString().toLowerCase(locale);
            int score = 0;
            int termIndex = 0;
            int previousMatchingCharacterIndex = Integer.MIN_VALUE;
            for (int queryIndex = 0; queryIndex < queryLowerCase.length(); queryIndex++) {
                char queryChar = queryLowerCase.charAt(queryIndex);
                boolean termCharacterMatchFound = false;
                while (termIndex < termLowerCase.length() && !termCharacterMatchFound) {
                    if (queryChar == termLowerCase.charAt(termIndex)) {
                        score++;
                        if (previousMatchingCharacterIndex + 1 == termIndex) {
                            score += 2;
                        }
                        previousMatchingCharacterIndex = termIndex;
                        termCharacterMatchFound = true;
                    }
                    termIndex++;
                }
            }
            return score;
        } else {
            throw new IllegalArgumentException("Locale must not be null");
        }
    }

    public static String normalizeSpace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int size = str.length();
        char[] newChars = new char[size];
        int count = 0;
        int whitespacesCount = 0;
        boolean startWhitespaces = true;
        for (int i = 0; i < size; i++) {
            char actualChar = str.charAt(i);
            if (Character.isWhitespace(actualChar)) {
                if (whitespacesCount == 0 && !startWhitespaces) {
                    newChars[count] = SPACE.charAt(0);
                    count++;
                }
                whitespacesCount++;
            } else {
                startWhitespaces = false;
                int count2 = count + 1;
                newChars[count] = actualChar == 160 ? ' ' : actualChar;
                whitespacesCount = 0;
                count = count2;
            }
        }
        if (startWhitespaces) {
            return "";
        }
        return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0)).trim();
    }

    @Deprecated
    public static String toString(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
        String str;
        if (charsetName == null) {
            str = new String(bytes, Charset.defaultCharset());
        }
        return str;
    }

    public static String toEncodedString(byte[] bytes, Charset charset) {
        return new String(bytes, charset != null ? charset : Charset.defaultCharset());
    }

    public static String wrap(String str, String wrapWith) {
        if (isEmpty(str) || isEmpty(wrapWith)) {
            return str;
        }
        return wrapWith.concat(str).concat(wrapWith);
    }
}
