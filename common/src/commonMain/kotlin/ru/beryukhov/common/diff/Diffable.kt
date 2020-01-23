package ru.beryukhov.common.diff

/**
 * Created by Andrey Beryukhov
 */
interface Diffable {
    operator fun <T : Diffable> plus(d: Diff<T>): T
    //fun <T : Diffable> plus(v: T, d: Diff<T>):T
    //fun <T : Diffable> plus(d: Diff<T>, v: T):T = plus(v,d)
    operator fun <T : Diffable> minus(v: T): Diff<T>
    //fun <T : Diffable> minus(v1: T, v2:T):Diff<T>
}

interface Diff<T : Diffable> {
    operator fun plus(v: T): T = v + this
}

class DiffableBoolen(value: Boolean) : Comparable<Boolean> by value, Diffable {
    override fun <T : Diffable> plus(d: Diff<T>): T {

    }

    override fun <T : Diffable> minus(v: T): Diff<T> {

    }

}

class BooleanDiff : Diff<DiffableBoolen>
