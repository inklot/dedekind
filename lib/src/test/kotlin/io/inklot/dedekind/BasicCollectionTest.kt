package io.inklot.dedekind

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BasicCollectionTest : StringSpec({
    
    "empty collection should have size 0" {
        val empty = Collection.empty<String>()
        empty.size shouldBe 0
        empty.totalCount shouldBe 0
        empty.isEmpty shouldBe true
        empty.isNotEmpty shouldBe false
    }
    
    "singleton collection should have size 1" {
        val singleton = Collection.of("hello")
        singleton.size shouldBe 1
        singleton.totalCount shouldBe 1
        singleton.isEmpty shouldBe false
        singleton.isNotEmpty shouldBe true
        singleton.count("hello") shouldBe 1
        singleton.contains("hello") shouldBe true
    }
    
    "collection from list should preserve multiplicities" {
        val elements = listOf("a", "b", "a", "c", "b", "a")
        val collection = Collection.fromIterable(elements)
        
        collection.size shouldBe 3  // a, b, c
        collection.totalCount shouldBe 6
        collection.count("a") shouldBe 3
        collection.count("b") shouldBe 2
        collection.count("c") shouldBe 1
    }
    
    "map should transform elements correctly" {
        val collection = Collection.fromIterable(listOf(1, 2, 3, 2))
        val mapped = collection.map { it * 2 }
        
        mapped.totalCount shouldBe 4
        mapped.count(2) shouldBe 1  // from 1 * 2
        mapped.count(4) shouldBe 2  // from 2 * 2 (appears twice)
        mapped.count(6) shouldBe 1  // from 3 * 2
    }
    
    "filter should keep only matching elements" {
        val collection = Collection.fromIterable(listOf(1, 2, 3, 4, 5, 2))
        val filtered = collection.filter { it % 2 == 0 }
        
        filtered.totalCount shouldBe 3  // 2, 4, 2
        filtered.count(2) shouldBe 2
        filtered.count(4) shouldBe 1
        filtered.count(1) shouldBe 0
        filtered.count(3) shouldBe 0
        filtered.count(5) shouldBe 0
    }
    
    "distinct should remove duplicates" {
        val collection = Collection.fromIterable(listOf("a", "b", "a", "c", "b", "a"))
        val distinct = collection.distinct()
        
        distinct.size shouldBe 3
        distinct.totalCount shouldBe 3
        distinct.count("a") shouldBe 1
        distinct.count("b") shouldBe 1
        distinct.count("c") shouldBe 1
    }
    
    "concat should combine collections" {
        val collection1 = Collection.fromIterable(listOf("a", "b"))
        val collection2 = Collection.fromIterable(listOf("b", "c"))
        val combined = collection1.concat(collection2)
        
        combined.totalCount shouldBe 4
        combined.count("a") shouldBe 1
        combined.count("b") shouldBe 2
        combined.count("c") shouldBe 1
    }
    
    "join should match elements correctly" {
        val users = Collection.fromIterable(listOf(
            User(1, "Alice"),
            User(2, "Bob")
        ))
        val purchases = Collection.fromIterable(listOf(
            Purchase(1, "Book"),
            Purchase(1, "Pen"),
            Purchase(2, "Laptop")
        ))
        
        val joined = users.join(
            purchases,
            keySelector = { it.id },
            otherKeySelector = { it.userId },
            resultSelector = { user, purchase -> "${user.name} bought ${purchase.item}" }
        )
        
        joined.totalCount shouldBe 3
        joined.count("Alice bought Book") shouldBe 1
        joined.count("Alice bought Pen") shouldBe 1
        joined.count("Bob bought Laptop") shouldBe 1
    }
    
    "groupBy should group elements correctly" {
        val collection = Collection.fromIterable(listOf(1, 2, 3, 4, 5, 6))
        val grouped = collection.groupBy(
            keySelector = { it % 3 },
            aggregator = { key, values -> key to values.size }
        )
        
        grouped.totalCount shouldBe 3
        grouped.count(0 to 2) shouldBe 1  // 3, 6
        grouped.count(1 to 2) shouldBe 1  // 1, 4  
        grouped.count(2 to 2) shouldBe 1  // 2, 5
    }
})

data class User(val id: Int, val name: String)
data class Purchase(val userId: Int, val item: String) 