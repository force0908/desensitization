/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.zyc.desensitization.metadata.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author zyc
 */
public class UnsafeAllocator {

    private static final Unsafe UNSAFE;

    /**
     * {@link Logger}
     */
    private static final Logger LOG = LoggerFactory.getLogger(UnsafeAllocator.class);

    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("初始化" + Unsafe.class + "失败！");
        }
    }

    /**
     * 实例化指定{@link Class}的对象
     *
     * @param clazz 对象的{@link Class
     * @param <T>   对象的类型
     * @return 指定{@link Class}的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("实例化" + clazz + "失败");
        }
    }

    /**
     * 设置指定对象中的域值
     *
     * @param object   指定对象
     * @param field    指定对象的域
     * @param newValue 将要设置的值
     */
    static void setFieldValue(Object object, Field field, Object newValue) {
        long fieldOffset;
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers)) {
            fieldOffset = UNSAFE.staticFieldOffset(field);
        } else {
            fieldOffset = UNSAFE.objectFieldOffset(field);
        }
        UNSAFE.putObject(object, fieldOffset, newValue);
    }

}
