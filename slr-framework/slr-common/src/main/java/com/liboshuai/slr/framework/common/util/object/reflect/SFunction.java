package com.liboshuai.slr.framework.common.util.object.reflect;

import java.io.Serializable;

@FunctionalInterface
public interface SFunction<T> extends Serializable {
    Object apply(T t);
}