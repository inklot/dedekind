package io.inklot.dedekind

/**
 * Represents a change (difference) in a collection with multiplicity.
 * 
 * @param T the type of data being changed
 * @param data the data item being inserted or removed
 * @param diff the multiplicity change:
 *   - positive values represent insertions
 *   - negative values represent deletions
 *   - zero represents no change
 */
data class Change<T>(
    val data: T,
    val diff: Int
) {
    /**
     * Returns true if this change represents an insertion (positive diff).
     */
    val isInsertion: Boolean get() = diff > 0
    
    /**
     * Returns true if this change represents a deletion (negative diff).
     */
    val isDeletion: Boolean get() = diff < 0
    
    /**
     * Returns true if this change has no effect (zero diff).
     */
    val isNoop: Boolean get() = diff == 0
    
    /**
     * Returns the absolute magnitude of the change.
     */
    val magnitude: Int get() = kotlin.math.abs(diff)
    
    /**
     * Returns the inverse of this change (negated diff).
     */
    fun inverse(): Change<T> = Change(data, -diff)
    
    /**
     * Combines this change with another change for the same data item.
     * The diffs are added together.
     */
    operator fun plus(other: Change<T>): Change<T> {
        require(this.data == other.data) { "Cannot combine changes for different data items" }
        return Change(data, diff + other.diff)
    }
    
    companion object {
        /**
         * Creates an insertion change with multiplicity 1.
         */
        fun <T> insert(data: T): Change<T> = Change(data, 1)
        
        /**
         * Creates a deletion change with multiplicity 1.
         */
        fun <T> remove(data: T): Change<T> = Change(data, -1)
        
        /**
         * Creates an insertion change with the specified multiplicity.
         */
        fun <T> insert(data: T, count: Int): Change<T> {
            require(count >= 0) { "Insert count must be non-negative" }
            return Change(data, count)
        }
        
        /**
         * Creates a deletion change with the specified multiplicity.
         */
        fun <T> remove(data: T, count: Int): Change<T> {
            require(count >= 0) { "Remove count must be non-negative" }
            return Change(data, -count)
        }
    }
} 