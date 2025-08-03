package io.inklot.dedekind

/**
 * Represents a historical sequence of changes to a collection over logical time.
 * Traces enable efficient incremental computation by tracking when changes occurred.
 * 
 * @param T the type of data in the trace
 */
interface Trace<T> {
    /**
     * The current logical timestamp of this trace.
     */
    val timestamp: Long
    
    /**
     * Advances the logical time of this trace to the specified timestamp.
     * All subsequent operations will be associated with this timestamp.
     * 
     * @param newTimestamp the new logical timestamp (must be >= current timestamp)
     */
    fun advance(newTimestamp: Long)
    
    /**
     * Records an insertion at the current timestamp.
     * 
     * @param data the data item to insert
     * @param count the multiplicity of the insertion (default: 1)
     */
    fun insert(data: T, count: Int = 1)
    
    /**
     * Records a deletion at the current timestamp.
     * 
     * @param data the data item to remove
     * @param count the multiplicity of the deletion (default: 1)
     */
    fun remove(data: T, count: Int = 1)
    
    /**
     * Records a change at the current timestamp.
     * 
     * @param change the change to record
     */
    fun record(change: Change<T>)
    
    /**
     * Records a batch of changes at the current timestamp.
     * 
     * @param changes the changes to record
     */
    fun recordBatch(changes: Iterable<Change<T>>)
    
    /**
     * Returns all changes recorded at or after the specified timestamp.
     * 
     * @param since the timestamp threshold (inclusive)
     * @return sequence of timestamped changes
     */
    fun changesFrom(since: Long): Sequence<TimestampedChange<T>>
    
    /**
     * Returns all changes recorded between the specified timestamps.
     * 
     * @param from the start timestamp (inclusive)
     * @param to the end timestamp (exclusive)
     * @return sequence of timestamped changes
     */
    fun changesBetween(from: Long, to: Long): Sequence<TimestampedChange<T>>
    
    /**
     * Consolidates all changes to produce the current state of the collection.
     * 
     * @return map from data items to their current multiplicities
     */
    fun consolidate(): Map<T, Int>
    
    /**
     * Creates a new trace containing only changes from the specified timestamp onwards.
     * 
     * @param from the timestamp threshold (inclusive)
     * @return new trace with filtered changes
     */
    fun trimBefore(from: Long): Trace<T>
}

/**
 * Represents a change associated with a specific logical timestamp.
 * 
 * @param T the type of data being changed
 * @param change the change that occurred
 * @param timestamp the logical time when the change occurred
 */
data class TimestampedChange<T>(
    val change: Change<T>,
    val timestamp: Long
) {
    val data: T get() = change.data
    val diff: Int get() = change.diff
} 