package com.igeeksky.xcache.extension.convertor;

import com.igeeksky.xcache.extension.serializer.JdkSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class JdkKeyConvertor implements KeyConvertor {

    public static final JdkKeyConvertor UTF_8 = new JdkKeyConvertor(StandardCharsets.UTF_8);

    public static final JdkKeyConvertor ASCII = new JdkKeyConvertor(StandardCharsets.US_ASCII);

    private final Charset charset;

    private final JdkSerializer<Object> jdkSerializer;

    public JdkKeyConvertor(Charset charset) {
        this.charset = charset;
        this.jdkSerializer = JdkSerializer.getInstance(charset);
    }

    public static JdkKeyConvertor getInstance(Charset charset) {
        if (Objects.equals(StandardCharsets.UTF_8, charset)) {
            return UTF_8;
        } else if (Objects.equals(StandardCharsets.US_ASCII, charset)) {
            return ASCII;
        } else {
            return new JdkKeyConvertor(charset);
        }
    }

    @Override
    public String doApply(Object original) {
        byte[] bytes = jdkSerializer.serialize(original);
        return new String(bytes, charset);
    }

}
