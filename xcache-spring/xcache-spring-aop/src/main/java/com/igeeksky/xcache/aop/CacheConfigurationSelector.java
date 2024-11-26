package com.igeeksky.xcache.aop;


import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.lang.NonNull;

/**
 * 选择器，根据注解的 mode属性，选择不同的配置类。
 * <p>
 * 注意：当前仅支持默认值 {@link AdviceMode#PROXY}
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheConfigurationSelector extends AdviceModeImportSelector<EnableCache> {

    @Override
    protected String[] selectImports(@NonNull AdviceMode adviceMode) {
        if (AdviceMode.PROXY == adviceMode) {
            return getProxyImports();
        } else if (AdviceMode.ASPECTJ == adviceMode) {
            throw new IllegalStateException("AdviceMode.ASPECTJ is not supported");
        }
        return null;
    }

    private String[] getProxyImports() {
        String[] imports = new String[2];
        imports[0] = AutoProxyRegistrar.class.getName();
        imports[1] = ProxyCacheConfiguration.class.getName();
        return imports;
    }

}
