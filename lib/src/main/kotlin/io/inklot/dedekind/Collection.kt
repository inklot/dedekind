package io.inklot.dedekind

/**
 * Represents a multiset of values that supports differential dataflow operations.
 * Collections are immutable - all operations return new collections.
 * 
 * @param T the type of elements in the collection
 */
interface Collection<T> {
    /**
     * The number of distinct elements in this collection.
     */
    val size: Int
    
    /**
     * The total count of all elements including multiplicities.
     */
    val totalCount: Long
    
    /**
     * Returns true if this collection is empty.
     */
    val isEmpty: Boolean get() = size == 0
    
    /**
     * Returns true if this collection is not empty.
     */
    val isNotEmpty: Boolean get() = size > 0
    
    /**
     * Returns the multiplicity of the specified element.
     * 
     * @param element the element to count
     * @return the number of times the element appears (0 if not present)
     */
    fun count(element: T): Int
    
    /**
     * Returns true if this collection contains the specified element.
     * 
     * @param element the element to check
     * @return true if the element is present (count > 0)
     */
    operator fun contains(element: T): Boolean = count(element) > 0
    
    /**
     * Transforms each element using the provided function.
     * 
     * @param R the result type
     * @param transform the transformation function
     * @return new collection with transformed elements
     */
    fun <R> map(transform: (T) -> R): Collection<R>
    
    /**
     * Keeps only elements that match the predicate.
     * 
     * @param predicate the filtering condition
     * @return new collection with filtered elements
     */
    fun filter(predicate: (T) -> Boolean): Collection<T>
    
    /**
     * Transforms each element to a collection and flattens the results.
     * 
     * @param R the result element type
     * @param transform the transformation function
     * @return new collection with flattened results
     */
    fun <R> flatMap(transform: (T) -> Collection<R>): Collection<R>
    
    /**
     * Removes duplicate elements, keeping only one copy of each.
     * 
     * @return new collection with distinct elements
     */
    fun distinct(): Collection<T>
    
    /**
     * Combines this collection with another collection.
     * 
     * @param other the collection to union with
     * @return new collection containing elements from both collections
     */
    fun concat(other: Collection<T>): Collection<T>
    
    /**
     * Performs an equijoin with another collection using the provided key extractors.
     * 
     * @param K the key type for joining
     * @param R the result type
     * @param other the collection to join with
     * @param keySelector key extractor for this collection
     * @param otherKeySelector key extractor for the other collection
     * @param resultSelector function to combine matching elements
     * @return new collection with join results
     */
    fun <K, R, U> join(
        other: Collection<U>,
        keySelector: (T) -> K,
        otherKeySelector: (U) -> K,
        resultSelector: (T, U) -> R
    ): Collection<R>
    
    /**
     * Groups elements by key and applies an aggregation function.
     * 
     * @param K the key type
     * @param R the result type
     * @param keySelector key extractor function
     * @param aggregator function to aggregate grouped elements
     * @return new collection with aggregated results
     */
    fun <K, R> groupBy(
        keySelector: (T) -> K,
        aggregator: (K, List<T>) -> R
    ): Collection<R>
    
    /**
     * Reduces the collection to a single value using an associative operation.
     * 
     * @param operation the associative reduction operation
     * @return the reduced result, or null if the collection is empty
     */
    fun reduce(operation: (T, T) -> T): T?
    
    /**
     * Converts this collection to a list, preserving multiplicities.
     * 
     * @return list representation of this collection
     */
    fun toList(): List<T>
    
    /**
     * Converts this collection to a set, ignoring multiplicities.
     * 
     * @return set representation of this collection
     */
    fun toSet(): Set<T>
    
    /**
     * Converts this collection to a map of elements to their multiplicities.
     * 
     * @return map from elements to their counts
     */
    fun toMultiplicityMap(): Map<T, Int>
    
    /**
     * Returns a sequence of all elements with their multiplicities.
     * 
     * @return sequence of element-count pairs
     */
    fun asSequence(): Sequence<Pair<T, Int>>
    
    companion object {
        /**
         * Creates an empty collection.
         * 
         * @param T the element type
         * @return empty collection
         */
        fun <T> empty(): Collection<T> = emptyCollection()
        
        /**
         * Creates a collection from a single element.
         * 
         * @param T the element type
         * @param element the single element
         * @return collection containing the element
         */
        fun <T> of(element: T): Collection<T> = singletonCollection(element)
        
        /**
         * Creates a collection from multiple elements.
         * 
         * @param T the element type
         * @param elements the elements to include
         * @return collection containing the elements
         */
        fun <T> of(vararg elements: T): Collection<T> = fromIterable(elements.asIterable())
        
        /**
         * Creates a collection from an iterable.
         * 
         * @param T the element type
         * @param elements the elements to include
         * @return collection containing the elements
         */
        fun <T> fromIterable(elements: Iterable<T>): Collection<T> = 
            elements.fold(empty<T>()) { acc, element -> acc.concat(of(element)) }
        
        /**
         * Creates a collection from a sequence of changes.
         * 
         * @param T the element type
         * @param changes the changes to apply
         * @return collection resulting from applying the changes
         */
        fun <T> fromChanges(changes: Iterable<Change<T>>): Collection<T> = 
            changes.fold(empty<T>()) { acc, change -> acc.applyChange(change) }
    }
}

/**
 * Applies a change to this collection.
 * 
 * @param change the change to apply
 * @return new collection with the change applied
 */
fun <T> Collection<T>.applyChange(change: Change<T>): Collection<T> {
    return when {
        change.isNoop -> this
        change.isInsertion -> this.concat(Collection.of(change.data))
        else -> this.filter { it != change.data || count(change.data) > change.magnitude }
    }
}

// Private implementation functions
private fun <T> emptyCollection(): Collection<T> = io.inklot.dedekind.impl.EmptyCollection()
private fun <T> singletonCollection(element: T): Collection<T> = io.inklot.dedekind.impl.SingletonCollection(element) 