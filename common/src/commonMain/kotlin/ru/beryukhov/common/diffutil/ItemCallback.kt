package ru.beryukhov.common.diffutil

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 *
 * [Callback] serves two roles - list indexing, and item diffing. ItemCallback handles
 * just the second of these, which allows separation of code that indexes into an array or List
 * from the presentation-layer and content specific diffing code.
 *
 * @param <T> Type of items to compare.
</T> */
abstract class ItemCallback<T> {
    /**
     * Called to check whether two objects represent the same item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     *
     * Note: `null` items in the list are assumed to be the same as another `null`
     * item and are assumed to not be the same as a non-`null` item. This callback will
     * not be invoked for either of those cases.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the two items represent the same object or false if they are different.
     *
     * @see Callback.areItemsTheSame
     */
    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    /**
     * Called to check whether two items have the same data.
     *
     *
     * This information is used to detect if the contents of an item have changed.
     *
     *
     * This method to check equality instead of [Object.equals] so that you can
     * change its behavior depending on your UI.
     *
     *
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns `true` for
     * these items.
     *
     *
     * Note: Two `null` items are assumed to represent the same contents. This callback
     * will not be invoked for this case.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the contents of the items are the same or false if they are different.
     *
     * @see Callback.areContentsTheSame
     */
    abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    /**
     * When [.areItemsTheSame] returns `true` for two items and
     * [.areContentsTheSame] returns false for them, this method is called to
     * get a payload about the change.
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
     * @see Callback.getChangePayload
     */
    fun getChangePayload(oldItem: T, newItem: T): Any? {
        return null
    }
}