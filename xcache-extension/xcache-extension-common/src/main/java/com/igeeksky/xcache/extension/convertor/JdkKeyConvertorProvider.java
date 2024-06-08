package com.igeeksky.xcache.extension.convertor;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class JdkKeyConvertorProvider implements KeyConvertorProvider {

    public static final JdkKeyConvertorProvider INSTANCE = new JdkKeyConvertorProvider();

    @Override
    public KeyConvertor get(Charset charset) {
        return JdkKeyConvertor.getInstance(charset);
    }

}
