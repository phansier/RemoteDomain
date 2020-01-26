package ru.beryukhov.common.diffutil

/**
 * This class holds the information about the result of a
 * [DiffUtil.calculateDiff] call.
 */
class DiffResult internal constructor(
    private val callback: Callback,
    // The Myers' snakes. At this point, we only care about their diagonal sections.
    val snakes: MutableList<Snake>,
    // The list to keep oldItemStatuses. As we traverse old items, we assign flags to them
    // which also includes whether they were a real removal or a move (and its new index).
    private val mOldItemStatuses: IntArray,
    // The list to keep newItemStatuses. As we traverse new items, we assign flags to them
    // which also includes whether they were a real addition or a move(and its old index).
    private val mNewItemStatuses: IntArray,
    detectMoves: Boolean
) {
    // The callback that was given to calcualte diff method.
    //private val mCallback: Callback
    private val mOldListSize: Int
    private val mNewListSize: Int
    private val mDetectMoves: Boolean
    /**
     * We always add a Snake to 0/0 so that we can run loops from end to beginning and be done
     * when we run out of snakes.
     */
    private fun addRootSnake() {
        val firstSnake = if (snakes.isEmpty()) null else snakes[0]
        if (firstSnake == null || firstSnake.x != 0 || firstSnake.y != 0) {
            val root = Snake(
                x = 0,
                y = 0,
                removal = false,
                size = 0,
                reverse = false
            )
            snakes.add(0, root)
        }
    }

    /**
     * This method traverses each addition / removal and tries to match it to a previous
     * removal / addition. This is how we detect move operations.
     *
     *
     * This class also flags whether an item has been changed or not.
     *
     *
     * DiffUtil does this pre-processing so that if it is running on a big list, it can be moved
     * to background thread where most of the expensive stuff will be calculated and kept in
     * the statuses maps. DiffResult uses this pre-calculated information while dispatching
     * the updates (which is probably being called on the main thread).
     */
    private fun findMatchingItems() {
        var posOld = mOldListSize
        var posNew = mNewListSize
        // traverse the matrix from right bottom to 0,0.
        for (i in snakes.indices.reversed()) {
            val snake = snakes[i]
            val endX = snake.x + snake.size
            val endY = snake.y + snake.size
            if (mDetectMoves) {
                while (posOld > endX) { // this is a removal. Check remaining snakes to see if this was added before
                    findAddition(posOld, posNew, i)
                    posOld--
                }
                while (posNew > endY) { // this is an addition. Check remaining snakes to see if this was removed before
                    findRemoval(posOld, posNew, i)
                    posNew--
                }
            }
            for (j in 0 until snake.size) { // matching items. Check if it is changed or not
                val oldItemPos = snake.x + j
                val newItemPos = snake.y + j
                val theSame = callback
                    .areContentsTheSame(oldItemPos, newItemPos)
                val changeFlag =
                    if (theSame) FLAG_NOT_CHANGED else FLAG_CHANGED
                mOldItemStatuses[oldItemPos] =
                    newItemPos shl FLAG_OFFSET or changeFlag
                mNewItemStatuses[newItemPos] =
                    oldItemPos shl FLAG_OFFSET or changeFlag
            }
            posOld = snake.x
            posNew = snake.y
        }
    }

    private fun findAddition(x: Int, y: Int, snakeIndex: Int) {
        if (mOldItemStatuses[x - 1] != 0) {
            return  // already set by a latter item
        }
        findMatchingItem(x, y, snakeIndex, false)
    }

    private fun findRemoval(x: Int, y: Int, snakeIndex: Int) {
        if (mNewItemStatuses[y - 1] != 0) {
            return  // already set by a latter item
        }
        findMatchingItem(x, y, snakeIndex, true)
    }

    /**
     * Given a position in the old list, returns the position in the new list, or
     * `NO_POSITION` if it was removed.
     *
     * @param oldListPosition Position of item in old list
     *
     * @return Position of item in new list, or `NO_POSITION` if not present.
     *
     * @see .NO_POSITION
     *
     * @see .convertNewPositionToOld
     */
    fun convertOldPositionToNew(/*@androidx.annotation.IntRange(from = 0)*/ oldListPosition: Int): Int {
        if (oldListPosition < 0 || oldListPosition >= mOldItemStatuses.size) {
            throw IndexOutOfBoundsException(
                "Index out of bounds - passed position = "
                        + oldListPosition + ", old list size = " + mOldItemStatuses.size
            )
        }
        val status = mOldItemStatuses[oldListPosition]
        return if (status and FLAG_MASK == 0) {
            NO_POSITION
        } else {
            status shr FLAG_OFFSET
        }
    }

    /**
     * Given a position in the new list, returns the position in the old list, or
     * `NO_POSITION` if it was removed.
     *
     * @param newListPosition Position of item in new list
     *
     * @return Position of item in old list, or `NO_POSITION` if not present.
     *
     * @see .NO_POSITION
     *
     * @see .convertOldPositionToNew
     */
    fun convertNewPositionToOld(/*@androidx.annotation.IntRange(from = 0)*/ newListPosition: Int): Int {
        if (newListPosition < 0 || newListPosition >= mNewItemStatuses.size) {
            throw IndexOutOfBoundsException(
                "Index out of bounds - passed position = "
                        + newListPosition + ", new list size = " + mNewItemStatuses.size
            )
        }
        val status = mNewItemStatuses[newListPosition]
        return if (status and FLAG_MASK == 0) {
            NO_POSITION
        } else {
            status shr FLAG_OFFSET
        }
    }

    /**
     * Finds a matching item that is before the given coordinates in the matrix
     * (before : left and above).
     *
     * @param x The x position in the matrix (position in the old list)
     * @param y The y position in the matrix (position in the new list)
     * @param snakeIndex The current snake index
     * @param removal True if we are looking for a removal, false otherwise
     *
     * @return True if such item is found.
     */
    private fun findMatchingItem(
        x: Int, y: Int, snakeIndex: Int,
        removal: Boolean
    ): Boolean {
        val myItemPos: Int
        var curX: Int
        var curY: Int
        if (removal) {
            myItemPos = y - 1
            curX = x
            curY = y - 1
        } else {
            myItemPos = x - 1
            curX = x - 1
            curY = y
        }
        for (i in snakeIndex downTo 0) {
            val snake = snakes[i]
            val endX = snake.x + snake.size
            val endY = snake.y + snake.size
            if (removal) { // check removals for a match
                for (pos in curX - 1 downTo endX) {
                    if (callback.areItemsTheSame(pos, myItemPos)) { // found!
                        val theSame = callback.areContentsTheSame(pos, myItemPos)
                        val changeFlag = if (theSame) FLAG_MOVED_NOT_CHANGED else FLAG_MOVED_CHANGED
                        mNewItemStatuses[myItemPos] = pos shl FLAG_OFFSET or FLAG_IGNORE
                        mOldItemStatuses[pos] = myItemPos shl FLAG_OFFSET or changeFlag
                        return true
                    }
                }
            } else { // check for additions for a match
                for (pos in curY - 1 downTo endY) {
                    if (callback.areItemsTheSame(myItemPos, pos)) { // found
                        val theSame =
                            callback.areContentsTheSame(myItemPos, pos)
                        val changeFlag =
                            if (theSame) FLAG_MOVED_NOT_CHANGED else FLAG_MOVED_CHANGED
                        mOldItemStatuses[x - 1] = pos shl FLAG_OFFSET or FLAG_IGNORE
                        mNewItemStatuses[pos] = x - 1 shl FLAG_OFFSET or changeFlag
                        return true
                    }
                }
            }
            curX = snake.x
            curY = snake.y
        }
        return false
    }

    /**
     * Dispatches update operations to the given Callback.
     * <p>
     * These updates are atomic such that the first update call affects every update call that
     * comes after it (the same as RecyclerView).
     *
     * @param updateCallback The callback to receive the update operations.
     * @see #dispatchUpdatesTo(RecyclerView.Adapter)
     */
    fun dispatchUpdatesTo(updateCallback: ListUpdateCallback) {
        val batchingCallback: BatchingListUpdateCallback =
            if (updateCallback is BatchingListUpdateCallback) {
                updateCallback
            } else {
                BatchingListUpdateCallback(updateCallback)
            }
        // These are add/remove ops that are converted to moves. We track their positions until
        // their respective update operations are processed.

        val postponedUpdates: MutableList<PostponedUpdate> = ArrayList()
        var posOld = mOldListSize
        var posNew = mNewListSize

        for (snakeIndex in snakes.size - 1 downTo 0) {
            val snake = snakes[snakeIndex]
            val snakeSize = snake.size
            val endX = snake.x + snakeSize
            val endY = snake.y + snakeSize
            if (endX < posOld) {
                dispatchRemovals(postponedUpdates, batchingCallback, endX, posOld - endX, endX)
            }

            if (endY < posNew) {
                dispatchAdditions(postponedUpdates, batchingCallback, endX, posNew - endY, endY)
            }
            for (i in snakeSize - 1 downTo 0) {
                if ((mOldItemStatuses[snake.x + i] and FLAG_MASK) == FLAG_CHANGED) {
                    batchingCallback.onChanged(snake.x + i, 1, callback.getChangePayload(snake.x + i, snake.y + i))
                }
            }
            posOld = snake.x
            posNew = snake.y
        }
        batchingCallback.dispatchLastEvent()
    }

    private fun dispatchAdditions(
        postponedUpdates: MutableList<PostponedUpdate>,
        updateCallback: ListUpdateCallback, start: Int, count: Int, globalIndex: Int
    ) {
        if (!mDetectMoves) {
            updateCallback.onInserted(start, count)
            return
        }
        for (i in count - 1 downTo 0) {
            val status = mNewItemStatuses [globalIndex + i] and FLAG_MASK
            when(status) {
                0 -> { // real addition
                    updateCallback.onInserted(start, 1)
                    for (update in postponedUpdates) {
                        update.currentPos += 1
                    }
                }
                FLAG_MOVED_CHANGED, FLAG_MOVED_NOT_CHANGED -> {
                    val pos = mNewItemStatuses[globalIndex + i] shr FLAG_OFFSET
                    val update = removePostponedUpdate(
                        postponedUpdates, pos,
                        true
                    )
                    // the item was moved from that position
                    //noinspection ConstantConditions
                    updateCallback.onMoved(update!!.currentPos, start);
                    //update?.currentPos?.let { updateCallback.onMoved(it, start) }
                    if (status == FLAG_MOVED_CHANGED) {
                        // also dispatch a change
                        updateCallback.onChanged(
                            start, 1,
                            callback.getChangePayload(pos, globalIndex + i)
                        )
                    }
                }
                FLAG_IGNORE -> {
                    postponedUpdates.add(PostponedUpdate (globalIndex + i, start, false))
                }
                else ->
                throw  IllegalStateException (
                    "unknown flag for pos ${globalIndex + i} ${status.toString(2)}"
                )
            }
        }
    }

    private fun dispatchRemovals(postponedUpdates:MutableList<PostponedUpdate> ,
     updateCallback:ListUpdateCallback, start:Int, count:Int, globalIndex:Int)
    {
        if (!mDetectMoves) {
            updateCallback.onRemoved(start, count)
            return
        }
        for (i in count-1 downTo 0) {
        val status = mOldItemStatuses[globalIndex + i] and FLAG_MASK
            when(status) {
            0-> { // real removal
                updateCallback.onRemoved(start + i, 1)
                for (update in postponedUpdates) {
                    update.currentPos -= 1
                }
            }
            FLAG_MOVED_CHANGED, FLAG_MOVED_NOT_CHANGED -> {
                val pos = mOldItemStatuses[globalIndex + i] shr FLAG_OFFSET
                val update = removePostponedUpdate(
                    postponedUpdates, pos,
                    false
                )
                // the item was moved to that position. we do -1 because this is a move not
                // add and removing current item offsets the target move by 1
                //noinspection ConstantConditions
                updateCallback.onMoved(start + i, update!!.currentPos - 1);
                //update?.currentPos?.minus(1)?.let { updateCallback.onMoved(start + i, it) }
                if (status == FLAG_MOVED_CHANGED) {
                    // also dispatch a change
                    updateCallback.onChanged(update.currentPos - 1, 1,
                        callback.getChangePayload(globalIndex + i, pos));
                }
            }

            FLAG_IGNORE -> { // ignoring this
                postponedUpdates.add(PostponedUpdate (globalIndex + i, start + i, true))
            }
            else->
            throw IllegalStateException (
                "unknown flag for pos ${globalIndex + i} ${status.toString(2)}"
            )
        }
    }
    }


    /**
     * Represents an update that we skipped because it was a move.
     *
     *
     * When an update is skipped, it is tracked as other updates are dispatched until the matching
     * add/remove operation is found at which point the tracked position is used to dispatch the
     * update.
     */
    private class PostponedUpdate(
        var posInOwnerList: Int,
        var currentPos: Int,
        var removal: Boolean
    )

    companion object {
        /**
         * Signifies an item not present in the list.
         */
        const val NO_POSITION = -1
        /**
         * While reading the flags below, keep in mind that when multiple items move in a list,
         * Myers's may pick any of them as the anchor item and consider that one NOT_CHANGED while
         * picking others as additions and removals. This is completely fine as we later detect
         * all moves.
         *
         *
         * Below, when an item is mentioned to stay in the same "location", it means we won't
         * dispatch a move/add/remove for it, it DOES NOT mean the item is still in the same
         * position.
         */
        // item stayed the same.
        private const val FLAG_NOT_CHANGED = 1
        // item stayed in the same location but changed.
        private const val FLAG_CHANGED = FLAG_NOT_CHANGED shl 1
        // Item has moved and also changed.
        private const val FLAG_MOVED_CHANGED = FLAG_CHANGED shl 1
        // Item has moved but did not change.
        private const val FLAG_MOVED_NOT_CHANGED = FLAG_MOVED_CHANGED shl 1
        // Ignore this update.
        // If this is an addition from the new list, it means the item is actually removed from an
        // earlier position and its move will be dispatched when we process the matching removal
        // from the old list.
        // If this is a removal from the old list, it means the item is actually added back to an
        // earlier index in the new list and we'll dispatch its move when we are processing that
        // addition.
        private const val FLAG_IGNORE = FLAG_MOVED_NOT_CHANGED shl 1
        // since we are re-using the int arrays that were created in the Myers' step, we mask
        // change flags
        private const val FLAG_OFFSET = 5
        private const val FLAG_MASK = (1 shl FLAG_OFFSET) - 1
        private fun removePostponedUpdate(
            updates: MutableList<PostponedUpdate>,
            pos: Int, removal: Boolean
        ): PostponedUpdate? {
            for (i in updates.indices.reversed()) {
                val update = updates[i]
                if (update.posInOwnerList == pos && update.removal == removal) {
                    updates.removeAt(i)
                    for (j in i until updates.size) { // offset other ops since they swapped positions
                        updates[j].currentPos += if (removal) 1 else -1
                    }
                    return update
                }
            }
            return null
        }
    }


    /**
     * @param mOldItemStatuses An int[] that can be re-purposed to keep metadata
     * @param mNewItemStatuses An int[] that can be re-purposed to keep metadata
     * @param mDetectMoves True if this DiffResult will try to detect moved items
     */
    init {
        mOldItemStatuses.fill(0)
        mNewItemStatuses.fill(0)
        mOldListSize = callback.oldListSize
        mNewListSize = callback.newListSize
        mDetectMoves = detectMoves
        addRootSnake()
        findMatchingItems()
    }
}