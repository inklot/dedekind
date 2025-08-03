package io.inklot.dedekind.impl

import io.inklot.dedekind.Change
import io.inklot.dedekind.Trace
import io.inklot.dedekind.TimestampedChange

/**
 * Mutable implementation of Trace that stores timestamped changes in chronological order.
 * 
 * @param T the element type
 * @param initialTimestamp the starting logical timestamp (default: 0)
 */
internal class MutableTrace<T>(
    initialTimestamp: Long = 0L
) : Trace<T> {
    
    private var _timestamp: Long = initialTimestamp
    private val changes = mutableListOf<TimestampedChange<T>>()
    
    override val timestamp: Long get() = _timestamp
    
    override fun advance(newTimestamp: Long) {
        require(newTimestamp >= _timestamp) {
            "Cannot move timestamp backwards: $newTimestamp < $_timestamp"
        }
        _timestamp = newTimestamp
    }
    
    override fun insert(data: T, count: Int) {
        require(count >= 0) { "Insert count must be non-negative: $count" }
        if (count > 0) {
            record(Change.insert(data, count))
        }
    }
    
    override fun remove(data: T, count: Int) {
        require(count >= 0) { "Remove count must be non-negative: $count" }
        if (count > 0) {
            record(Change.remove(data, count))
        }
    }
    
    override fun record(change: Change<T>) {
        if (!change.isNoop) {
            changes.add(TimestampedChange(change, _timestamp))
        }
    }
    
    override fun recordBatch(changes: Iterable<Change<T>>) {
        changes.forEach { change ->
            record(change)
        }
    }
    
    override fun changesFrom(since: Long): Sequence<TimestampedChange<T>> =
        changes.asSequence().filter { it.timestamp >= since }
    
    override fun changesBetween(from: Long, to: Long): Sequence<TimestampedChange<T>> =
        changes.asSequence().filter { it.timestamp >= from && it.timestamp < to }
    
    override fun consolidate(): Map<T, Int> {
        val multiplicities = mutableMapOf<T, Int>()
        
        changes.forEach { timestampedChange ->
            val data = timestampedChange.data
            val currentCount = multiplicities[data] ?: 0
            val newCount = currentCount + timestampedChange.diff
            
            if (newCount <= 0) {
                multiplicities.remove(data)
            } else {
                multiplicities[data] = newCount
            }
        }
        
        return multiplicities.toMap()
    }
    
    override fun trimBefore(from: Long): Trace<T> {
        val trimmedChanges = changes.filter { it.timestamp >= from }
        val trimmedTrace = MutableTrace<T>(maxOf(from, _timestamp))
        trimmedChanges.forEach { trimmedTrace.changes.add(it) }
        return trimmedTrace
    }
    
    /**
     * Returns the total number of recorded changes.
     */
    fun changeCount(): Int = changes.size
    
    /**
     * Returns all recorded changes in chronological order.
     */
    fun allChanges(): List<TimestampedChange<T>> = changes.toList()
    
    /**
     * Clears all recorded changes while preserving the current timestamp.
     */
    fun clear() {
        changes.clear()
    }
    
    override fun toString(): String = 
        "MutableTrace(timestamp=$_timestamp, changes=${changes.size})"
} 