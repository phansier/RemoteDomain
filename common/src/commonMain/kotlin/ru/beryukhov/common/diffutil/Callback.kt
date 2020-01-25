package ru.beryukhov.common.diffutil

/**
 * A Callback class used by DiffUtil while calculating the diff between two lists.
 */
abstract class Callback {
    /**
     * Returns the size of the old list.
     *
     * @return The size of the old list.
     */
    abstract val oldListSize: Int

    /**
     * Returns the size of the new list.
     *
     * @return The size of the new list.
     */
    abstract val newListSize: Int

    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return True if the two items represent the same object or false if they are different.
     */
    abstract fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     *
     *
     * DiffUtil uses this method to check equality instead of [Object.equals]
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns
     * `true` for these items.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     * oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    abstract fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean

    /**
     * When [.areItemsTheSame] returns `true` for two items and
     * [.areContentsTheSame] returns false for them, DiffUtil
     * calls this method to get a payload about the change.
     *
     *
     * For example, if you are using DiffUtil with [RecyclerView], you can return the
     * particular field that changed in the item and your
     * [ItemAnimator][RecyclerView.ItemAnimator] can use that
     * information to run the correct animation.
     *
     *
     * Default implementation returns `null`.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     *
     * @return A payload object that represents the change between the two items.
     */
    open fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return null
    }
}