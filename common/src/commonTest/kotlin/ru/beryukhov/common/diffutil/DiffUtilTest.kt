/*
 * Copyright (C) 2016 The Android Open Source Project
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
package ru.beryukhov.common.diffutil


import com.benasher44.uuid.uuid4
import ru.beryukhov.common.diffutil.DiffUtil.calculateDiff
import kotlin.random.Random
import kotlin.test.*

@Ignore
class DiffUtilTest {
    private val mBefore: MutableList<Item> =
        ArrayList()
    private val mAfter: MutableList<Item> =
        ArrayList()
    private val mLog: StringBuilder = StringBuilder()
    private val mCallback: Callback =
        object : Callback() {
            override val oldListSize: Int
                get() = mBefore.size

            override val newListSize: Int
                get() = mAfter.size

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return mBefore[oldItemPosition].id == mAfter[newItemPosition].id
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                assertEquals(mBefore[oldItemPosition].id, mAfter[newItemPosition].id)
                return mBefore[oldItemPosition].data == mAfter[newItemPosition].data
            }

            override fun getChangePayload(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Any? {
                assertEquals(mBefore[oldItemPosition].id, mAfter[newItemPosition].id)
                assertNotEquals(mBefore[oldItemPosition].data, mAfter[newItemPosition].data)
                return mAfter[newItemPosition].payload
            }
        }

    @Test
    fun testNoChange() {
        initWithSize(5)
        check()
    }

    @Test
    fun testAddItems() {
        initWithSize(2)
        add(1)
        check()
    }

    @Test
    @Ignore
    fun testRandom() {
        for (i in 0..99) {
            for (j in 2..39) {
                testRandom(i, j)
            }
        }
    }

    @Test
    fun testGen2() {
        initWithSize(5)
        add(5)
        delete(3)
        delete(1)
        check()
    }

    @Test
    fun testGen3() {
        initWithSize(5)
        add(0)
        delete(1)
        delete(3)
        check()
    }

    @Test
    fun testGen4() {
        initWithSize(5)
        add(5)
        add(1)
        add(4)
        add(4)
        check()
    }

    @Test
    fun testGen5() {
        initWithSize(5)
        delete(0)
        delete(2)
        add(0)
        add(2)
        check()
    }

    @Test
    fun testGen6() {
        initWithSize(2)
        delete(0)
        delete(0)
        check()
    }

    @Test
    fun testGen7() {
        initWithSize(3)
        move(2, 0)
        delete(2)
        add(2)
        check()
    }

    @Test
    fun testGen8() {
        initWithSize(3)
        delete(1)
        add(0)
        move(2, 0)
        check()
    }

    @Test
    fun testGen9() {
        initWithSize(2)
        add(2)
        move(0, 2)
        check()
    }

    @Test
    fun testGen10() {
        initWithSize(3)
        move(0, 1)
        move(1, 2)
        add(0)
        check()
    }

    @Test
    fun testGen11() {
        initWithSize(4)
        move(2, 0)
        move(2, 3)
        check()
    }

    @Test
    fun testGen12() {
        initWithSize(4)
        move(3, 0)
        move(2, 1)
        check()
    }

    @Test
    fun testGen13() {
        initWithSize(4)
        move(3, 2)
        move(0, 3)
        check()
    }

    @Test
    fun testGen14() {
        initWithSize(4)
        move(3, 2)
        add(4)
        move(0, 4)
        check()
    }

    @Test
    fun testAdd1() {
        initWithSize(1)
        add(1)
        check()
    }

    @Test
    fun testMove1() {
        initWithSize(3)
        move(0, 2)
        check()
    }

    @Test
    fun tmp() {
        initWithSize(4)
        move(0, 2)
        check()
    }

    @Test
    fun testUpdate1() {
        initWithSize(3)
        update(2)
        check()
    }

    @Test
    fun testUpdate2() {
        initWithSize(2)
        add(1)
        update(1)
        update(2)
        check()
    }

    @Test
    fun testDisableMoveDetection() {
        initWithSize(5)
        move(0, 4)
        val applied: List<Item> =
            applyUpdates(mBefore, calculateDiff(mCallback, false))
        assertEquals(applied.size, 5)
        assertEquals(applied[4].newItem, true)
        assertEquals(applied.contains(mBefore[0]), false)
    }

    private fun testRandom(initialSize: Int, operationCount: Int) {
        //mLog.length = 0
        initWithSize(initialSize)
        for (i in 0 until operationCount) {
            when (sRand.nextInt(5)) {
                0 -> add(sRand.nextInt(mAfter.size + 1))
                1 -> if (mAfter.isNotEmpty()) {
                    delete(sRand.nextInt(mAfter.size))
                }
                2 ->  // move
                    if (mAfter.size > 0) {
                        move(
                            sRand.nextInt(mAfter.size),
                            sRand.nextInt(mAfter.size)
                        )
                    }
                3 ->  // update
                    if (mAfter.size > 0) {
                        update(sRand.nextInt(mAfter.size))
                    }
                4 ->  // update with payload
                    if (mAfter.size > 0) {
                        updateWithPayload(
                            sRand.nextInt(
                                mAfter.size
                            )
                        )
                    }
            }
        }
        check()
    }

    private fun check() {
        val result = calculateDiff(mCallback)
        log("before", mBefore)
        log("after", mAfter)
        log("snakes", result.snakes)
        val applied: List<Item> = applyUpdates(mBefore, result)
        log("applied", applied)
        assertEquals(applied, mAfter)
    }

    private fun initWithSize(size: Int) {
        mBefore.clear()
        mAfter.clear()
        for (i in 0 until size) {
            mBefore.add(Item(newItem = false))
        }
        mAfter.addAll(mBefore)
        mLog.append("initWithSize($size);\n")
    }

    private fun log(title: String, items: List<*>) {
        mLog.append(title).append(":").append(items.size).append("\n")
        for (item in items) {
            mLog.append("  ").append(item).append("\n")
        }
    }

    private fun applyUpdates(
        before: List<Item>,
        result: DiffResult
    ): List<Item> {
        val target: MutableList<Item> =
            ArrayList()
        target.addAll(before)
        result.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                for (i in 0 until count) {
                    target.add(i + position, Item(newItem = true))
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                for (i in 0 until count) {
                    target.removeAt(position)
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                val item: Item = target.removeAt(fromPosition)
                target.add(toPosition, item)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                for (i in 0 until count) {
                    val positionInList = position + i
                    val existing: Item =
                        target[positionInList]
                    // make sure we don't update same item twice in callbacks
                    assertEquals(existing.changed, false)
                    assertEquals(existing.newItem, false)
                    assertEquals(existing.payload, null)
                    val replica = Item(existing)
                    replica.payload = payload as String?
                    replica.changed = true
                    target.removeAt(positionInList)
                    target.add(positionInList, replica)
                }
            }
        })
        return target
    }

    private fun add(index: Int) {
        mAfter.add(
            index,
            Item(newItem = true)
        )
        mLog.append("add(").append(index).append(");\n")
    }

    private fun delete(index: Int) {
        mAfter.removeAt(index)
        mLog.append("delete(").append(index).append(");\n")
    }

    private fun update(index: Int) {
        val existing: Item = mAfter[index]
        if (existing.newItem) {
            return  //new item cannot be changed
        }
        val replica = Item(existing)
        replica.changed = true
        // clean the payload since this might be after an updateWithPayload call
        replica.payload = null
        replica.data = uuid4().toString()
        mAfter.removeAt(index)
        mAfter.add(index, replica)
        mLog.append("update(").append(index).append(");\n")
    }

    private fun updateWithPayload(index: Int) {
        val existing: Item = mAfter[index]
        if (existing.newItem) {
            return  //new item cannot be changed
        }
        val replica = Item(existing)
        replica.changed = true
        replica.data = uuid4().toString()
        replica.payload = uuid4().toString()
        mAfter.removeAt(index)
        mAfter.add(index, replica)
        mLog.append("update(").append(index).append(");\n")
    }

    private fun move(from: Int, to: Int) {
        val removed: Item = mAfter.removeAt(from)
        mAfter.add(to, removed)
        mLog.append("move(").append(from).append(",").append(to).append(");\n")
    }

    private fun assertEquals(applied: List<Item>, after: List<Item>) {
        log("applied", applied)
        val report = mLog.toString()
        assertEquals(applied.size, after.size, report)
        for (i in after.indices) {
            val item = applied.get(i)
            when {
                after[i].newItem -> {
                    assertEquals(item.newItem, true, report)
                }
                after.get(i).changed -> {
                    assertEquals(item.newItem, false, report)
                    assertEquals(item.changed, true, report)
                    assertEquals(item.id, after[i].id, report)
                    assertEquals(item.payload, after[i].payload, report)
                }
                else -> {
                    assertEquals(item, after[i], report)
                }
            }
        }
        println(mLog)
        mLog.clear()
    }

    internal data class Item(
        val id: Long,
        val newItem: Boolean,
        var changed: Boolean = false,
        var payload: String? = null,
        var data: String = uuid4().toString()
    ) {

        constructor(other: Item) : this(
            id = other.id,
            newItem = other.newItem,
            changed = other.changed,
            payload = other.payload,
            data = other.data
        )

        constructor(newItem: Boolean) : this(id = idCounter++, newItem = newItem)


        companion object {
            private var idCounter: Long = 0
        }
    }

    companion object {
        private val sRand: Random = Random.Default
    }
}