package com.baioretto.baiolib.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark types as instantiable
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Instantiable {

}