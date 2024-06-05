package com.igeeksky.xcache.extension.compress;

/**
 * @author Patrick.Lau
 * @since 0.03 2021-06-22
 */
public interface Compressor {

    /**
     * 压缩
     *
     * @return 压缩后的字节数组
     */
    byte[] compress(byte[] source);

    /**
     * 解压缩
     *
     * @return 解压缩后的字节数组
     */
    byte[] decompress(byte[] source);

}
