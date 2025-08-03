package io.inklot.dedekind

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class CollectionTest : StringSpec({
    
    "empty collection should have size 0" {
        val empty = Collection.empty<String>()
        empty.size shouldBe 0
        empty.totalCount shouldBe 0
        empty.isEmpty shouldBe true
        empty.isNotEmpty shouldBe false
    }
    
    "singleton collection should have size 1" {
        checkAll(Arb.string()) { element ->
            val singleton = Collection.of(element)
            singleton.size shouldBe 1
            singleton.totalCount shouldBe 1
            singleton.isEmpty shouldBe false
            singleton.isNotEmpty shouldBe true
            singleton.count(element) shouldBe 1
            singleton.contains(element) shouldBe true
        }
    }
    
    "collection from list should preserve multiplicities" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val elementCounts = elements.groupingBy { it }.eachCount()
            
            collection.size shouldBe elementCounts.size
            collection.totalCount shouldBe elements.size.toLong()
            
            elementCounts.forEach { (element, expectedCount) ->
                collection.count(element) shouldBe expectedCount
                collection.contains(element) shouldBe true
            }
        }
    }
    
    "map should preserve total count" {
        checkAll(Arb.list(Arb.int(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val mapped = collection.map { it.toString() }
            
            mapped.totalCount shouldBe collection.totalCount
        }
    }
    
    "map should transform all elements" {
        checkAll(Arb.list(Arb.int(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val mapped = collection.map { it * 2 }
            
            // Check that total count is preserved
            mapped.totalCount shouldBe collection.totalCount
            
            // Check that each original element's count contributes to the mapped element
            val expectedMappedCounts = mutableMapOf<Int, Int>()
            elements.forEach { element ->
                val mappedElement = element * 2
                expectedMappedCounts[mappedElement] = 
                    (expectedMappedCounts[mappedElement] ?: 0) + 1
            }
            
            expectedMappedCounts.forEach { (mappedElement, expectedCount) ->
                mapped.count(mappedElement) shouldBe expectedCount
            }
        }
    }
    
    "filter should only keep matching elements" {
        checkAll(Arb.list(Arb.int(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val filtered = collection.filter { it > 0 }
            
            elements.forEach { element ->
                if (element > 0) {
                    filtered.count(element) shouldBe collection.count(element)
                } else {
                    filtered.count(element) shouldBe 0
                }
            }
        }
    }
    
    "filter then map should equal map then filter (when possible)" {
        checkAll(Arb.list(Arb.int(0..100), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            
            // Filter positive numbers then double them
            val filterThenMap = collection.filter { it > 0 }.map { it * 2 }
            
            // Double all numbers then filter positive results  
            val mapThenFilter = collection.map { it * 2 }.filter { it > 0 }
            
            // These should be equal because:
            // - filter(x > 0) then map(x * 2) gives positive even numbers
            // - map(x * 2) then filter(x > 0) gives positive even numbers
            // - Both preserve the same multiplicities
            filterThenMap.toMultiplicityMap() shouldBe mapThenFilter.toMultiplicityMap()
        }
    }
    
    "distinct should remove duplicates" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val distinct = collection.distinct()
            
            val uniqueElements = elements.toSet()
            distinct.size shouldBe uniqueElements.size
            distinct.totalCount shouldBe uniqueElements.size.toLong()
            
            uniqueElements.forEach { element ->
                distinct.count(element) shouldBe 1
            }
        }
    }
    
    "concat should combine collections" {
        checkAll(Arb.list(Arb.string(), 0..10), Arb.list(Arb.string(), 0..10)) { list1, list2 ->
            val collection1 = Collection.fromIterable(list1)
            val collection2 = Collection.fromIterable(list2)
            val combined = collection1.concat(collection2)
            
            combined.totalCount shouldBe (collection1.totalCount + collection2.totalCount)
            
            val allElements = (list1 + list2).toSet()
            allElements.forEach { element ->
                val expectedCount = collection1.count(element) + collection2.count(element)
                combined.count(element) shouldBe expectedCount
            }
        }
    }
    
    "concat should be associative" {
        checkAll(
            Arb.list(Arb.string(), 0..5),
            Arb.list(Arb.string(), 0..5),
            Arb.list(Arb.string(), 0..5)
        ) { list1, list2, list3 ->
            val c1 = Collection.fromIterable(list1)
            val c2 = Collection.fromIterable(list2)
            val c3 = Collection.fromIterable(list3)
            
            val leftAssoc = (c1.concat(c2)).concat(c3)
            val rightAssoc = c1.concat(c2.concat(c3))
            
            leftAssoc.toMultiplicityMap() shouldBe rightAssoc.toMultiplicityMap()
        }
    }
    
    "concat with empty should be identity" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val empty = Collection.empty<String>()
            
            collection.concat(empty) shouldBe collection
            empty.concat(collection) shouldBe collection
        }
    }
    
    "join should match elements with same keys" {
        checkAll(Arb.list(Arb.int(1..10), 1..5), Arb.list(Arb.int(1..10), 1..5)) { list1, list2 ->
            val collection1 = Collection.fromIterable(list1)
            val collection2 = Collection.fromIterable(list2)
            
            val joined = collection1.join(
                collection2,
                keySelector = { it },
                otherKeySelector = { it },
                resultSelector = { a, b -> a to b }
            )
            
            // Verify join results
            val expectedResults = mutableListOf<Pair<Int, Int>>()
            list1.forEach { a ->
                list2.forEach { b ->
                    if (a == b) {
                        expectedResults.add(a to b)
                    }
                }
            }
            
            joined.totalCount shouldBe expectedResults.size.toLong()
        }
    }
    
    "groupBy should group elements correctly" {
        checkAll(Arb.list(Arb.int(1..20), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val grouped = collection.groupBy(
                keySelector = { it % 3 },
                aggregator = { key, values -> key to values.size }
            )
            
            val expectedGroups = elements.groupBy { it % 3 }
            grouped.size shouldBe expectedGroups.size
            
            expectedGroups.forEach { (key, values) ->
                grouped.count(key to values.size) shouldBe 1
            }
        }
    }
    
    "reduce should work on non-empty collections" {
        checkAll(Arb.list(Arb.int(), 1..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val reduced = collection.reduce { a, b -> a + b }
            
            reduced shouldBe elements.sum()
        }
    }
    
    "reduce should return null on empty collection" {
        val empty = Collection.empty<Int>()
        empty.reduce { a, b -> a + b } shouldBe null
    }
    
    "toList should preserve all elements with multiplicities" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val list = collection.toList()
            
            // Lists should have same elements (order may differ)
            list.size shouldBe elements.size
            elements.groupingBy { it }.eachCount() shouldBe list.groupingBy { it }.eachCount()
        }
    }
    
    "toSet should contain all unique elements" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val set = collection.toSet()
            
            set shouldBe elements.toSet()
        }
    }
    
    "toMultiplicityMap should accurately represent collection" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection = Collection.fromIterable(elements)
            val multiplicityMap = collection.toMultiplicityMap()
            
            val expectedMap = elements.groupingBy { it }.eachCount()
            multiplicityMap shouldBe expectedMap
        }
    }
    
    "collections with same multiplicities should be equal" {
        checkAll(Arb.list(Arb.string(), 0..20)) { elements ->
            val collection1 = Collection.fromIterable(elements)
            val collection2 = Collection.fromIterable(elements.shuffled())
            
            collection1 shouldBe collection2
            collection1.hashCode() shouldBe collection2.hashCode()
        }
    }
    
    "collections with different multiplicities should not be equal" {
        checkAll(Arb.list(Arb.string(), 1..20)) { elements ->
            if (elements.isNotEmpty()) {
                val collection1 = Collection.fromIterable(elements)
                val collection2 = Collection.fromIterable(elements + elements.first())
                
                collection1 shouldNotBe collection2
            }
        }
    }
    
    "flatMap should flatten collections correctly" {
        checkAll(Arb.list(Arb.int(1..5), 1..10)) { elements ->
            val collection = Collection.fromIterable(elements)
            val flattened = collection.flatMap { element ->
                Collection.fromIterable((1..element).toList())
            }
            
            val expectedElements = elements.flatMap { element ->
                (1..element).toList()
            }
            
            flattened.totalCount shouldBe expectedElements.size.toLong()
            flattened.toMultiplicityMap() shouldBe expectedElements.groupingBy { it }.eachCount()
        }
    }
}) 