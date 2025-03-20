package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 如果数据未全部命中缓存，则反射执行方法并缓存；<br>
 * 如果数据已全部命中缓存，则直接返回已缓存数据。
 * <p>
 * {@link Cacheable} 仅支持缓存单个元素，{@link CacheableAll} 支持批量缓存多个元素。
 * <p>
 * 如果一个类中有多个同名缓存实例的注解，{@code name}, {@code keyType}, {@code valueType}，
 * 这三个公共属性可用类注解 {@link CacheConfig} 配置，此注解保持默认即可。
 * <p>
 * <b>注意</b>：<p>
 * 1. 请勿与其它缓存注解用于同一方法！<br>
 * 因为当成功获取缓存数据时，此注解的目标方法将不会被调用。
 * <p>
 * 2. 如使用 SpEL表达式通过参数名获取数据，项目编译时必须使用 -parameters 记录方法参数信息，
 * 否则无法正确解析 SpEL表达式。<br>
 * 如使用 maven-compiler-plugin，必须配置：{@code <parameters>true</parameters> }
 * <p>
 * 3. 被 {@code @CacheableAll} 注解修饰的方法，其返回的 {@code Map} 类型必须是可修改的，
 * 因为缓存结果集需要添加到该 {@code Map}。
 * <p>
 * 4. ⭐传入缓存的键集与方法创建的 {@code Map} 的值集必须是对应的。⭐<br>
 * 例如，如传入缓存的键集为 {@code {1, 2}}，
 * 方法创建的 {@code Map} 为 {@code {{1,value}, {2, value}, {3, value}}}，多出一个 {@code {3, value}}。<br>
 * 这种情况下，被注解方法的返回结果集是不确定的。<br>
 * 当已全部命中缓存时，不调用方法，返回的是缓存结果 {@code {{1,value}, {2, value}}}；<br>
 * 当未全部命中缓存时，会调用方法，返回的是方法结果 {@code {{1,value}, {2, value}, {3, value}}}。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CacheableAll {

    /**
     * SpEL表达式，用于从参数中提取键集。
     * <p>
     * 如果未配置，使用被注解方法的第一个参数。
     * <p>
     * <b>注意：</b><br>
     * 1. 键集必须为可写的 set 类型，因为读取缓存后，需先移除已命中缓存的键，然后再调用方法获取未命中缓存的数据。<br>
     * 2. 因为可能移除 set 中的全部或部分元素，所以被注解方法不能有依赖于该 set 的完整性和元素顺序的代码。
     */
    String keys() default "";

    /**
     * SpEL表达式，用于判断是否执行缓存操作。
     * <p>
     * 如果未配置，condition 表达式结果默认为 true。
     * <p>
     * 调用被注解方法前解析此表达式，如 condition 表达式结果为 false，不执行缓存操作。
     */
    String condition() default "";

    /**
     * 缓存名称
     */
    String name() default "";

    /**
     * 键类型
     */
    Class<?> keyType() default Undefined.class;

    /**
     * 值类型
     */
    Class<?> valueType() default Undefined.class;

}
