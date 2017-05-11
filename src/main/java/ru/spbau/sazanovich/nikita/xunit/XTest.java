package ru.spbau.sazanovich.nikita.xunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which allows to mark methods as test methods for XUnit .
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XTest {

    /**
     * Reason for why the test method should be ignored. If it is the zero-length string then it is not ignored.
     */
    String ignore() default "";

    /**
     * Expected during the execution exception. {@link None} represents no exception.
     */
    Class<? extends Throwable> expected() default None.class;

    /**
     * Class representing no exception.
     */
    final class None extends Throwable {

        private None() {
        }
    }
}
