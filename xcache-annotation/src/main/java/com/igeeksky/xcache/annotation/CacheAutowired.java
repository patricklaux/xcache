package com.igeeksky.xcache.annotation;

import java.lang.annotation.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-06
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAutowired {

    String name() default "";

    Class<?> keyType() default Object.class;

    Class<?> valueType() default Undefined.class;

    /**
     * <P>值的泛型参数，配置的目的是 JSON 序列化时避免记录类型信息，可以节省存储空间</P>
     * <p/>
     * <p>例：值类型为 Order&lt;Integer, User&gt;</p>
     * <p>泛型的配置：valueType=Order.class, valueParams={Integer.class, User.class}</p>
     * <p/>
     * <p>更复杂的嵌套泛型，这里无法配置，例如 List&lt;Order&lt;Integer, User&gt;&gt;。解决办法：</p>
     * <p>1.可以创建特殊的 SerializerProvider 来解决反序列化问题。</p>
     * <p>2.创建一个包装类，将可变的泛型参数变为固定参数,类似于这样的一个类：</p>
     * <pre>
     * public class Orders{
     *     private List&lt;Order&lt;Integer, User&gt;&gt list;
     *     ……
     * }
     * </pre>
     *
     * @return 泛型参数。
     * 无泛型时返回空数组
     */
    Class<?>[] valueParams() default {};
}
