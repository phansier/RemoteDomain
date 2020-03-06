package ru.beryukhov.remote_domain.baserecyclerview

/**
 * Created by Andrey Beryukhov
 */
interface IBaseListAdapter<T> {
    fun add(newItem: T)
    fun add(newItems: List<T>?)
    fun addAtPosition(pos : Int, newItem : T)
    fun remove(position: Int)
    fun clearAll()
}