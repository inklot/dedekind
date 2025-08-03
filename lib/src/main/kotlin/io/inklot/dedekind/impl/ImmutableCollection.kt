package io.inklot.dedekind.impl

import io.inklot.dedekind.Collection

/**
 * Immutable implementation of Collection backed by a map from elements to their multiplicities.
 * 
 * @param T the element type
 * @param multiplicities map from elements to their counts (must be positive)
 */
internal class ImmutableCollection<T>(
    private val multiplicities: Map<T, Int>
) : Collection<T> {
    
    init {
        require(multiplicities.values.all { it > 0 }) {
            "All multiplicities must be positive"
        }
    }
    
    override val size: Int = multiplicities.size
    
    override val totalCount: Long = multiplicities.values.sumOf { it.toLong() }
    
    override fun count(element: T): Int = multiplicities[element] ?: 0
    
    override fun <R> map(transform: (T) -> R): Collection<R> {
        val newMultiplicities = mutableMapOf<R, Int>()
        multiplicities.forEach { (element, count) ->
            val transformed = transform(element)
            newMultiplicities[transformed] = (newMultiplicities[transformed] ?: 0) + count
        }
        return if (newMultiplicities.isEmpty()) {
            EmptyCollection()
        } else {
            ImmutableCollection(newMultiplicities)
        }
    }
    
    override fun filter(predicate: (T) -> Boolean): Collection<T> {
        val filtered = multiplicities.filterKeys(predicate)
        return if (filtered.isEmpty()) {
            EmptyCollection()
        } else {
            ImmutableCollection(filtered)
        }
    }
    
    override fun <R> flatMap(transform: (T) -> Collection<R>): Collection<R> {
        val resultMultiplicities = mutableMapOf<R, Int>()
        
        multiplicities.forEach { (element, count) ->
            val transformed = transform(element)
            transformed.asSequence().forEach { (transformedElement, transformedCount) ->
                val totalCount = transformedCount * count
                resultMultiplicities[transformedElement] = 
                    (resultMultiplicities[transformedElement] ?: 0) + totalCount
            }
        }
        
        return if (resultMultiplicities.isEmpty()) {
            EmptyCollection()
        } else {
            ImmutableCollection(resultMultiplicities)
        }
    }
    
    override fun distinct(): Collection<T> {
        val distinctMultiplicities = multiplicities.mapValues { 1 }
        return ImmutableCollection(distinctMultiplicities)
    }
    
    override fun concat(other: Collection<T>): Collection<T> {
        if (other.isEmpty) return this
        if (this.isEmpty) return other
        
        val combined = multiplicities.toMutableMap()
        other.asSequence().forEach { (element, count) ->
            combined[element] = (combined[element] ?: 0) + count
        }
        
        return ImmutableCollection(combined)
    }
    
    override fun <K, R, U> join(
        other: Collection<U>,
        keySelector: (T) -> K,
        otherKeySelector: (U) -> K,
        resultSelector: (T, U) -> R
    ): Collection<R> {
        if (this.isEmpty || other.isEmpty) return EmptyCollection()
        
        // Build index for other collection
        val otherIndex = mutableMapOf<K, MutableList<Pair<U, Int>>>()
        other.asSequence().forEach { (element, count) ->
            val key = otherKeySelector(element)
            otherIndex.getOrPut(key) { mutableListOf() }.add(element to count)
        }
        
        val resultMultiplicities = mutableMapOf<R, Int>()
        
        multiplicities.forEach { (element, count) ->
            val key = keySelector(element)
            otherIndex[key]?.forEach { (otherElement, otherCount) ->
                val result = resultSelector(element, otherElement)
                val combinedCount = count * otherCount
                resultMultiplicities[result] = 
                    (resultMultiplicities[result] ?: 0) + combinedCount
            }
        }
        
        return if (resultMultiplicities.isEmpty()) {
            EmptyCollection()
        } else {
            ImmutableCollection(resultMultiplicities)
        }
    }
    
    override fun <K, R> groupBy(
        keySelector: (T) -> K,
        aggregator: (K, List<T>) -> R
    ): Collection<R> {
        if (isEmpty) return EmptyCollection()
        
        val groups = mutableMapOf<K, MutableList<T>>()
        multiplicities.forEach { (element, count) ->
            val key = keySelector(element)
            val group = groups.getOrPut(key) { mutableListOf() }
            repeat(count) { group.add(element) }
        }
        
        val results = groups.map { (key, elements) ->
            aggregator(key, elements)
        }
        
        return Collection.fromIterable(results)
    }
    
    override fun reduce(operation: (T, T) -> T): T? {
        if (isEmpty) return null
        
        val allElements = toList()
        return allElements.reduce(operation)
    }
    
    override fun toList(): List<T> {
        val result = mutableListOf<T>()
        multiplicities.forEach { (element, count) ->
            repeat(count) { result.add(element) }
        }
        return result
    }
    
    override fun toSet(): Set<T> = multiplicities.keys
    
    override fun toMultiplicityMap(): Map<T, Int> = multiplicities.toMap()
    
    override fun asSequence(): Sequence<Pair<T, Int>> = multiplicities.asSequence().map { it.key to it.value }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection<*>) return false
        return this.toMultiplicityMap() == other.toMultiplicityMap()
    }
    
    override fun hashCode(): Int = multiplicities.hashCode()
    
    override fun toString(): String = "Collection(${multiplicities})"
}

/**
 * Empty collection implementation for efficiency.
 */
internal class EmptyCollection<T> : Collection<T> {
    override val size: Int = 0
    override val totalCount: Long = 0
    
    override fun count(element: T): Int = 0
    
    override fun <R> map(transform: (T) -> R): Collection<R> = EmptyCollection()
    
    override fun filter(predicate: (T) -> Boolean): Collection<T> = this
    
    override fun <R> flatMap(transform: (T) -> Collection<R>): Collection<R> = EmptyCollection()
    
    override fun distinct(): Collection<T> = this
    
    override fun concat(other: Collection<T>): Collection<T> = other
    
    override fun <K, R, U> join(
        other: Collection<U>,
        keySelector: (T) -> K,
        otherKeySelector: (U) -> K,
        resultSelector: (T, U) -> R
    ): Collection<R> = EmptyCollection()
    
    override fun <K, R> groupBy(
        keySelector: (T) -> K,
        aggregator: (K, List<T>) -> R
    ): Collection<R> = EmptyCollection()
    
    override fun reduce(operation: (T, T) -> T): T? = null
    
    override fun toList(): List<T> = emptyList()
    
    override fun toSet(): Set<T> = emptySet()
    
    override fun toMultiplicityMap(): Map<T, Int> = emptyMap()
    
    override fun asSequence(): Sequence<Pair<T, Int>> = emptySequence()
    
    override fun equals(other: Any?): Boolean = other is Collection<*> && other.isEmpty
    
    override fun hashCode(): Int = 0
    
    override fun toString(): String = "Collection(empty)"
}

/**
 * Singleton collection implementation for efficiency.
 */
internal class SingletonCollection<T>(private val element: T) : Collection<T> {
    override val size: Int = 1
    override val totalCount: Long = 1
    
    override fun count(element: T): Int = if (this.element == element) 1 else 0
    
    override fun <R> map(transform: (T) -> R): Collection<R> = 
        SingletonCollection(transform(element))
    
    override fun filter(predicate: (T) -> Boolean): Collection<T> = 
        if (predicate(element)) this else EmptyCollection()
    
    override fun <R> flatMap(transform: (T) -> Collection<R>): Collection<R> = 
        transform(element)
    
    override fun distinct(): Collection<T> = this
    
    override fun concat(other: Collection<T>): Collection<T> = 
        if (other.isEmpty) this else ImmutableCollection(mapOf(element to 1)).concat(other)
    
    override fun <K, R, U> join(
        other: Collection<U>,
        keySelector: (T) -> K,
        otherKeySelector: (U) -> K,
        resultSelector: (T, U) -> R
    ): Collection<R> {
        if (other.isEmpty) return EmptyCollection()
        
        val key = keySelector(element)
        val matches = mutableListOf<R>()
        
        other.asSequence().forEach { (otherElement, count) ->
            if (otherKeySelector(otherElement) == key) {
                val result = resultSelector(element, otherElement)
                repeat(count) { matches.add(result) }
            }
        }
        
        return Collection.fromIterable(matches)
    }
    
    override fun <K, R> groupBy(
        keySelector: (T) -> K,
        aggregator: (K, List<T>) -> R
    ): Collection<R> {
        val key = keySelector(element)
        val result = aggregator(key, listOf(element))
        return SingletonCollection(result)
    }
    
    override fun reduce(operation: (T, T) -> T): T = element
    
    override fun toList(): List<T> = listOf(element)
    
    override fun toSet(): Set<T> = setOf(element)
    
    override fun toMultiplicityMap(): Map<T, Int> = mapOf(element to 1)
    
    override fun asSequence(): Sequence<Pair<T, Int>> = sequenceOf(element to 1)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection<*>) return false
        @Suppress("UNCHECKED_CAST")
        val typedOther = other as Collection<T>
        return other.size == 1 && typedOther.count(element) == 1
    }
    
    override fun hashCode(): Int = element.hashCode()
    
    override fun toString(): String = "Collection([$element])"
} 