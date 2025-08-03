package io.inklot.dedekind

import io.inklot.dedekind.impl.MutableTrace

/**
 * Factory object for creating Trace instances.
 */
object TraceBuilder {
    
    /**
     * Creates a new empty trace starting at timestamp 0.
     * 
     * @param T the element type
     * @return new empty trace
     */
    fun <T> empty(): Trace<T> = MutableTrace()
    
    /**
     * Creates a new empty trace starting at the specified timestamp.
     * 
     * @param T the element type
     * @param initialTimestamp the starting logical timestamp
     * @return new empty trace
     */
    fun <T> startingAt(initialTimestamp: Long): Trace<T> = MutableTrace(initialTimestamp)
    
    /**
     * Creates a trace from a sequence of timestamped changes.
     * 
     * @param T the element type
     * @param changes the timestamped changes to include
     * @return new trace containing the changes
     */
    fun <T> fromChanges(changes: Iterable<TimestampedChange<T>>): Trace<T> {
        val trace = MutableTrace<T>()
        
        // Sort changes by timestamp to maintain chronological order
        val sortedChanges = changes.sortedBy { it.timestamp }
        
        sortedChanges.forEach { timestampedChange ->
            trace.advance(timestampedChange.timestamp)
            trace.record(timestampedChange.change)
        }
        
        return trace
    }
    
    /**
     * Creates a trace from a sequence of changes, all recorded at the current timestamp.
     * 
     * @param T the element type
     * @param changes the changes to record
     * @param timestamp the timestamp to assign to all changes (default: 0)
     * @return new trace containing the changes
     */
    fun <T> fromChanges(changes: Iterable<Change<T>>, timestamp: Long = 0L): Trace<T> {
        val trace = MutableTrace<T>(timestamp)
        trace.recordBatch(changes)
        return trace
    }
} 