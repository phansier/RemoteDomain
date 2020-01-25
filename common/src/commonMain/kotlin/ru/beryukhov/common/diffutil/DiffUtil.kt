package ru.beryukhov.common.diffutil

import kotlin.jvm.JvmOverloads
import kotlin.math.abs

/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



/**
 * DiffUtil is a utility class that calculates the difference between two lists and outputs a
 * list of update operations that converts the first list into the second one.
 *
 *
 * It can be used to calculate updates for a RecyclerView Adapter. See [ListAdapter] and
 * [AsyncListDiffer] which can simplify the use of DiffUtil on a background thread.
 *
 *
 * DiffUtil uses Eugene W. Myers's difference algorithm to calculate the minimal number of updates
 * to convert one list into another. Myers's algorithm does not handle items that are moved so
 * DiffUtil runs a second pass on the result to detect items that were moved.
 *
 *
 * Note that DiffUtil, ListAdapter, and AsyncListDiffer require the list to not mutate while in use.
 * This generally means that both the lists themselves and their elements (or at least, the
 * properties of elements used in diffing) should not be modified directly. Instead, new lists
 * should be provided any time content changes. It's common for lists passed to DiffUtil to share
 * elements that have not mutated, so it is not strictly required to reload all data to use
 * DiffUtil.
 *
 *
 * If the lists are large, this operation may take significant time so you are advised to run this
 * on a background thread, get the [DiffResult] then apply it on the RecyclerView on the main
 * thread.
 *
 *
 * This algorithm is optimized for space and uses O(N) space to find the minimal
 * number of addition and removal operations between the two lists. It has O(N + D^2) expected time
 * performance where D is the length of the edit script.
 *
 *
 * If move detection is enabled, it takes an additional O(N^2) time where N is the total number of
 * added and removed items. If your lists are already sorted by the same constraint (e.g. a created
 * timestamp for a list of posts), you can disable move detection to improve performance.
 *
 *
 * The actual runtime of the algorithm significantly depends on the number of changes in the list
 * and the cost of your comparison methods. Below are some average run times for reference:
 * (The test list is composed of random UUID Strings and the tests are run on Nexus 5X with M)
 *
 *  * 100 items and 10 modifications: avg: 0.39 ms, median: 0.35 ms
 *  * 100 items and 100 modifications: 3.82 ms, median: 3.75 ms
 *  * 100 items and 100 modifications without moves: 2.09 ms, median: 2.06 ms
 *  * 1000 items and 50 modifications: avg: 4.67 ms, median: 4.59 ms
 *  * 1000 items and 50 modifications without moves: avg: 3.59 ms, median: 3.50 ms
 *  * 1000 items and 200 modifications: 27.07 ms, median: 26.92 ms
 *  * 1000 items and 200 modifications without moves: 13.54 ms, median: 13.36 ms
 *
 *
 *
 * Due to implementation constraints, the max size of the list can be 2^26.
 *
 * @see ListAdapter
 *
 * @see AsyncListDiffer
 */
object DiffUtil {
    private val SNAKE_COMPARATOR: Comparator<Snake> =
        object : Comparator<Snake> {
            override fun compare(a: Snake, b: Snake): Int {
                val cmpX = a.x - b.x
                return if (cmpX == 0) a.y - b.y else cmpX
            }
        }
    /**
     * Calculates the list of update operations that can covert one list into the other one.
     *
     *
     * If your old and new lists are sorted by the same constraint and items never move (swap
     * positions), you can disable move detection which takes `O(N^2)` time where
     * N is the number of added, moved, removed items.
     *
     * @param cb The callback that acts as a gateway to the backing list data
     * @param detectMoves True if DiffUtil should try to detect moved items, false otherwise.
     *
     * @return A DiffResult that contains the information about the edit sequence to convert the
     * old list into the new list.
     */
// Myers' algorithm uses two lists as axis labels. In DiffUtil's implementation, `x` axis is
// used for old list and `y` axis is used for new list.
    /**
     * Calculates the list of update operations that can covert one list into the other one.
     *
     * @param cb The callback that acts as a gateway to the backing list data
     *
     * @return A DiffResult that contains the information about the edit sequence to convert the
     * old list into the new list.
     */
    @JvmOverloads
    fun calculateDiff(cb: Callback, detectMoves: Boolean = true): DiffResult {
        val oldSize = cb.oldListSize
        val newSize = cb.newListSize
        val snakes: MutableList<Snake> = ArrayList()
        // instead of a recursive implementation, we keep our own stack to avoid potential stack
// overflow exceptions
        val stack: MutableList<Range> =
            ArrayList()
        stack.add(
            Range(
                0,
                oldSize,
                0,
                newSize
            )
        )
        val max: Int = oldSize + newSize + abs(oldSize - newSize)
        // allocate forward and backward k-lines. K lines are diagonal lines in the matrix. (see the
// paper for details)
// These arrays lines keep the max reachable position for each k-line.
        val forward = IntArray(max * 2)
        val backward = IntArray(max * 2)
        // We pool the ranges to avoid allocations for each recursive call.
        val rangePool: MutableList<Range> =
            ArrayList()
        while (!stack.isEmpty()) {
            val range = stack.removeAt(stack.size - 1)
            val snake = diffPartial(
                cb, range.oldListStart, range.oldListEnd,
                range.newListStart, range.newListEnd, forward, backward, max
            )
            if (snake != null) {
                if (snake.size > 0) {
                    snakes.add(snake)
                }
                // offset the snake to convert its coordinates from the Range's area to global
                snake.x += range.oldListStart
                snake.y += range.newListStart
                // add new ranges for left and right
                val left =
                    if (rangePool.isEmpty()) Range() else rangePool.removeAt(
                        rangePool.size - 1
                    )
                left.oldListStart = range.oldListStart
                left.newListStart = range.newListStart
                if (snake.reverse) {
                    left.oldListEnd = snake.x
                    left.newListEnd = snake.y
                } else {
                    if (snake.removal) {
                        left.oldListEnd = snake.x - 1
                        left.newListEnd = snake.y
                    } else {
                        left.oldListEnd = snake.x
                        left.newListEnd = snake.y - 1
                    }
                }
                stack.add(left)
                // re-use range for right
                if (snake.reverse) {
                    if (snake.removal) {
                        range.oldListStart = snake.x + snake.size + 1
                        range.newListStart = snake.y + snake.size
                    } else {
                        range.oldListStart = snake.x + snake.size
                        range.newListStart = snake.y + snake.size + 1
                    }
                } else {
                    range.oldListStart = snake.x + snake.size
                    range.newListStart = snake.y + snake.size
                }
                stack.add(range)
            } else {
                rangePool.add(range)
            }
        }
        // sort snakes
        snakes.sortWith(SNAKE_COMPARATOR)
        return DiffResult(
            cb,
            snakes,
            forward,
            backward,
            detectMoves
        )
    }

    private fun diffPartial(
        cb: Callback, startOld: Int, endOld: Int,
        startNew: Int, endNew: Int, forward: IntArray, backward: IntArray, kOffset: Int
    ): Snake? {
        val oldSize = endOld - startOld
        val newSize = endNew - startNew
        if (endOld - startOld < 1 || endNew - startNew < 1) {
            return null
        }
        val delta = oldSize - newSize
        val dLimit = (oldSize + newSize + 1) / 2
        forward.fill(0, kOffset - dLimit - 1, kOffset + dLimit + 1)
        backward.fill(oldSize, kOffset - dLimit - 1 + delta, kOffset + dLimit + 1 + delta)
        val checkInFwd = delta % 2 != 0
        for (d in 0..dLimit) {
            run {
                var k = -d
                while (k <= d) {
                    // find forward path
                    // we can reach k from k - 1 or k + 1. Check which one is further in the graph
                    var x: Int
                    val removal: Boolean
                    if (k == -d || k != d && forward[kOffset + k - 1] < forward[kOffset + k + 1]) {
                        x = forward[kOffset + k + 1]
                        removal = false
                    } else {
                        x = forward[kOffset + k - 1] + 1
                        removal = true
                    }
                    // set y based on x
                    var y = x - k
                    // move diagonal as long as items match
                    while (x < oldSize && y < newSize && cb.areItemsTheSame(
                            startOld + x,
                            startNew + y
                        )
                    ) {
                        x++
                        y++
                    }
                    forward[kOffset + k] = x
                    if (checkInFwd && k >= delta - d + 1 && k <= delta + d - 1) {
                        if (forward[kOffset + k] >= backward[kOffset + k]) {
                            val outSnake = Snake()
                            outSnake.x = backward[kOffset + k]
                            outSnake.y = outSnake.x - k
                            outSnake.size = forward[kOffset + k] - backward[kOffset + k]
                            outSnake.removal = removal
                            outSnake.reverse = false
                            return outSnake
                        }
                    }
                    k += 2
                }
            }
            var k = -d
            while (k <= d) {
                // find reverse path at k + delta, in reverse
                val backwardK = k + delta
                var x: Int
                val removal: Boolean
                if (backwardK == d + delta || (backwardK != -d + delta
                            && backward[kOffset + backwardK - 1] < backward[kOffset + backwardK + 1])
                ) {
                    x = backward[kOffset + backwardK - 1]
                    removal = false
                } else {
                    x = backward[kOffset + backwardK + 1] - 1
                    removal = true
                }
                // set y based on x
                var y = x - backwardK
                // move diagonal as long as items match
                while (x > 0 && y > 0 && cb.areItemsTheSame(startOld + x - 1, startNew + y - 1)
                ) {
                    x--
                    y--
                }
                backward[kOffset + backwardK] = x
                if (!checkInFwd && k + delta >= -d && k + delta <= d) {
                    if (forward[kOffset + backwardK] >= backward[kOffset + backwardK]) {
                        val outSnake = Snake()
                        outSnake.x = backward[kOffset + backwardK]
                        outSnake.y = outSnake.x - backwardK
                        outSnake.size =
                            forward[kOffset + backwardK] - backward[kOffset + backwardK]
                        outSnake.removal = removal
                        outSnake.reverse = true
                        return outSnake
                    }
                }
                k += 2
            }
        }
        throw IllegalStateException(
            "DiffUtil hit an unexpected case while trying to calculate"
                    + " the optimal path. Please make sure your data is not changing during the"
                    + " diff calculation."
        )
    }

    /**
     * Represents a range in two lists that needs to be solved.
     *
     *
     * This internal class is used when running Myers' algorithm without recursion.
     */
    internal class Range {
        var oldListStart = 0
        var oldListEnd = 0
        var newListStart = 0
        var newListEnd = 0

        constructor() {}
        constructor(oldListStart: Int, oldListEnd: Int, newListStart: Int, newListEnd: Int) {
            this.oldListStart = oldListStart
            this.oldListEnd = oldListEnd
            this.newListStart = newListStart
            this.newListEnd = newListEnd
        }
    }


}
