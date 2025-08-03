package io.inklot.dedekind

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Example usage test demonstrating the differential dataflow library.
 * This shows the kind of usage described in the project requirements.
 */
class ExampleUsageTest : StringSpec({
    
    "user purchase count example should work correctly" {
        // Create users collection
        val users = Collection.fromIterable(listOf(
            User(1, "Alice"),
            User(2, "Bob"), 
            User(3, "Charlie")
        ))
        
        // Create purchases collection
        val purchases = Collection.fromIterable(listOf(
            Purchase(1, "Book"),
            Purchase(1, "Pen"),
            Purchase(2, "Laptop"),
            Purchase(1, "Notebook"),
            Purchase(3, "Phone")
        ))
        
        // Calculate user purchase counts using dataflow operations
        val userPurchaseCounts = users
            .join(
                purchases,
                keySelector = { it.id },
                otherKeySelector = { it.userId },
                resultSelector = { user, purchase -> user to purchase }
            )
            .groupBy(
                keySelector = { (user, _) -> user },
                aggregator = { user, purchases -> user to purchases.size }
            )
        
        // Verify results
        userPurchaseCounts.size shouldBe 3
        userPurchaseCounts.count(User(1, "Alice") to 3) shouldBe 1  // Alice has 3 purchases
        userPurchaseCounts.count(User(2, "Bob") to 1) shouldBe 1    // Bob has 1 purchase
        userPurchaseCounts.count(User(3, "Charlie") to 1) shouldBe 1 // Charlie has 1 purchase
    }
    
    "differential update simulation should work" {
        // Start with initial data
        val initialPurchases = listOf(
            Purchase(1, "Book"),
            Purchase(2, "Laptop")
        )
        val purchases = Collection.fromIterable(initialPurchases)
        
        // Simulate adding new purchases (incremental update)
        val newPurchases = listOf(
            Purchase(1, "Pen"),
            Purchase(3, "Phone")
        )
        val updatedPurchases = purchases.concat(Collection.fromIterable(newPurchases))
        
        // Verify the updated state
        updatedPurchases.totalCount shouldBe 4
        updatedPurchases.count(Purchase(1, "Book")) shouldBe 1
        updatedPurchases.count(Purchase(1, "Pen")) shouldBe 1
        updatedPurchases.count(Purchase(2, "Laptop")) shouldBe 1
        updatedPurchases.count(Purchase(3, "Phone")) shouldBe 1
        
        // Group purchases by user after update
        val purchasesByUser = updatedPurchases.groupBy(
            keySelector = { it.userId },
            aggregator = { userId, purchases -> userId to purchases.size }
        )
        
        purchasesByUser.count(1 to 2) shouldBe 1  // User 1 has 2 purchases
        purchasesByUser.count(2 to 1) shouldBe 1  // User 2 has 1 purchase
        purchasesByUser.count(3 to 1) shouldBe 1  // User 3 has 1 purchase
    }
    
    "chained operations should work correctly" {
        val numbers = Collection.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        
        // Chain multiple operations: filter even numbers, double them, group by remainder when divided by 3
        val result = numbers
            .filter { it % 2 == 0 }           // Keep even numbers: [2, 4, 6, 8, 10]
            .map { it * 2 }                   // Double them: [4, 8, 12, 16, 20]
            .groupBy(
                keySelector = { it % 3 },      // Group by remainder when divided by 3
                aggregator = { remainder, values -> remainder to values.sorted() }
            )
        
        result.totalCount shouldBe 3
        result.count(1 to listOf(4, 16)) shouldBe 1    // 4 % 3 = 1, 16 % 3 = 1
        result.count(2 to listOf(8, 20)) shouldBe 1    // 8 % 3 = 2, 20 % 3 = 2  
        result.count(0 to listOf(12)) shouldBe 1       // 12 % 3 = 0
    }
    
    "trace example should track changes over time" {
        val trace = TraceBuilder.empty<String>()
        
        // Record some changes at different timestamps
        trace.advance(1)
        trace.insert("apple")
        trace.insert("banana")
        
        trace.advance(2)
        trace.insert("apple")  // Another apple
        trace.remove("banana")
        
        trace.advance(3)
        trace.insert("cherry")
        
        // Check consolidated state
        val finalState = trace.consolidate()
        finalState["apple"] shouldBe 2    // 1 + 1 = 2
        finalState["banana"] shouldBe null // 1 - 1 = 0 (removed)
        finalState["cherry"] shouldBe 1   // 1
        
        // Check changes from timestamp 2 onwards
        val recentChanges = trace.changesFrom(2).toList()
        recentChanges.size shouldBe 3  // insert apple, remove banana, insert cherry
    }
}) 