package com.squareup.javapoet;

/**
 * The available format modes a lambda function can have.
 * {@code DEFAULT} example: (x, y) -> x + y;
 * {@code VISIBLE_TYPES} example: (int x, int y) -> x + y;
*/
public enum LambdaMode {
    DEFAULT,
    VISIBLE_TYPES
}
