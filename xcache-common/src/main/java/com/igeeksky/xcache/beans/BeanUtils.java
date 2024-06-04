/*
 * Copyright 2017 Patrick.lau All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.beans;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bean工具类
 *
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-02
 */
public abstract class BeanUtils {

    private BeanUtils() {
    }

    public static Object getBeanProperty(Object bean, String fieldName) throws ReflectiveOperationException {
        Objects.requireNonNull(bean, "bean must not be null");
        Field field = bean.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(bean);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeansProperty(Object[] list, String fieldName, Class<T> type) throws ReflectiveOperationException {
        Objects.requireNonNull(list, "beanList must not be null");

        if (list.length > 0) {
            List<T> ids = new ArrayList<>();
            for (Object o : list) {
                ids.add((T) getBeanProperty(o, fieldName));
            }
            return ids;
        }
        return null;
    }

    public static <T> T instantiateClass(Class<T> clazz, Object... params) throws ReflectiveOperationException {
        if (clazz.isInterface()) {
            throw new InstantiationException(clazz.getName() + " is an interface");
        }

        Class<?>[] parameterTypes = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            Object obj = params[i];
            if (null != obj) {
                parameterTypes[i] = params[i].getClass();
            }
        }

        try {
            Constructor<T> constructor = clazz.getConstructor(parameterTypes);
            return constructor.newInstance(params);
        } catch (NoSuchMethodException e) {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        }
    }

}
