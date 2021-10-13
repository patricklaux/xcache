package com.igeeksky.xcache.util;

/**
 * 哈希字符串工具类
 *
 * @author Patrick.Lau
 * @since 0.0.1 2017-01-11
 */
public abstract class HexUtil {

    private static final char[] HEX_DIGITS_UC = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] HEX_DIGITS_LC = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private HexUtil() {
    }

    /**
     * byte[]转16进制字符串
     */
    public static String encodeHexString(boolean toLowerCase, byte[] digest) {
        //判断大小写
        char[] hexDigits = HEX_DIGITS_UC;
        if (toLowerCase) {
            hexDigits = HEX_DIGITS_LC;
        }
        //转换成16进制
        int length = digest.length;
        char[] chars = new char[length * 2];
        int k = 0;
        for (int i = 0; i < length; i++) {
            byte byte0 = digest[i];
            chars[k++] = hexDigits[byte0 >>> 0x4 & 0xf];
            chars[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(chars);
    }

}
