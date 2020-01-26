package ru.beryukhov.common.diffutil

/**
 * Snakes represent a match between two lists. It is optionally prefixed or postfixed with an
 * add or remove operation. See the Myers' paper for details.
 */
data class Snake(
    /**
     * Position in the old list
     */
    var x: Int = 0,
    /**
     * Position in the new list
     */
    var y: Int = 0,
    /**
     * Number of matches. Might be 0.
     */
    val size: Int = 0,
    /**
     * If true, this is a removal from the original list followed by `size` matches.
     * If false, this is an addition from the new list followed by `size` matches.
     */
    val removal: Boolean = false,
    /**
     * If true, the addition or removal is at the end of the snake.
     * If false, the addition or removal is at the beginning of the snake.
     */
    val reverse: Boolean = false
)