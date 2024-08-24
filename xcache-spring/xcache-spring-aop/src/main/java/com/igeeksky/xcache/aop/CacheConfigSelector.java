package com.igeeksky.xcache.aop;


import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheConfigSelector extends AdviceModeImportSelector<EnableCache> {

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        if (AdviceMode.PROXY == adviceMode) {
            return getProxy();
        } else if (AdviceMode.ASPECTJ == adviceMode) {
            return null;
        }
        return null;
    }

    private String[] getProxy() {
        List<String> result = new ArrayList<>(2);
        result.add(AutoProxyRegistrar.class.getName());
        result.add(ProxyCacheConfiguration.class.getName());
        return result.toArray(new String[0]);
    }

}
