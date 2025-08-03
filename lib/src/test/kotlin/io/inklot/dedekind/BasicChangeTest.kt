package io.inklot.dedekind

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BasicChangeTest : StringSpec({
    
    "Change should identify insertion, deletion, and noop correctly" {
        val insertion = Change("data", 5)
        insertion.isInsertion shouldBe true
        insertion.isDeletion shouldBe false
        insertion.isNoop shouldBe false
        insertion.magnitude shouldBe 5
        
        val deletion = Change("data", -3)
        deletion.isInsertion shouldBe false
        deletion.isDeletion shouldBe true
        deletion.isNoop shouldBe false
        deletion.magnitude shouldBe 3
        
        val noop = Change("data", 0)
        noop.isInsertion shouldBe false
        noop.isDeletion shouldBe false
        noop.isNoop shouldBe true
        noop.magnitude shouldBe 0
    }
    
    "inverse should negate the diff" {
        val change = Change("test", 5)
        val inverse = change.inverse()
        
        inverse.data shouldBe "test"
        inverse.diff shouldBe -5
        
        // Double inverse should return original
        inverse.inverse() shouldBe change
    }
    
    "plus operator should combine changes correctly" {
        val change1 = Change("data", 3)
        val change2 = Change("data", 2)
        val combined = change1 + change2
        
        combined.data shouldBe "data"
        combined.diff shouldBe 5
        
        // Adding inverse should create noop
        val withInverse = change1 + change1.inverse()
        withInverse.isNoop shouldBe true
    }
    
    "factory methods should work correctly" {
        val insert = Change.insert("item")
        insert.data shouldBe "item"
        insert.diff shouldBe 1
        insert.isInsertion shouldBe true
        
        val remove = Change.remove("item")
        remove.data shouldBe "item"
        remove.diff shouldBe -1
        remove.isDeletion shouldBe true
        
        val insertMultiple = Change.insert("item", 5)
        insertMultiple.diff shouldBe 5
        
        val removeMultiple = Change.remove("item", 3)
        removeMultiple.diff shouldBe -3
    }
}) 