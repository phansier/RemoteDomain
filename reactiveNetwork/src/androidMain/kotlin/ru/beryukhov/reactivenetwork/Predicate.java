package ru.beryukhov.reactivenetwork;

import org.jetbrains.annotations.NotNull;


/**
 * A functional interface (callback) that returns true or false for the given input value.
 * @param <T> the first value
 */
public interface Predicate<T> {
    /**
     * Test the given input value and return a boolean.
     * @param t the value
     * @return the boolean result
     * @throws Exception on error
     */
    boolean test(@NotNull T t) throws Exception;
}