package io.inklot.dedekind

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class ChangeTest : StringSpec({
    
    "Change should properly identify insertion, deletion, and noop" {
        checkAll(Arb.string(), Arb.int()) { data, diff ->
            val change = Change(data, diff)
            
            when {
                diff > 0 -> {
                    change.isInsertion shouldBe true
                    change.isDeletion shouldBe false
                    change.isNoop shouldBe false
                }
                diff < 0 -> {
                    change.isInsertion shouldBe false
                    change.isDeletion shouldBe true
                    change.isNoop shouldBe false
                }
                else -> {
                    change.isInsertion shouldBe false
                    change.isDeletion shouldBe false
                    change.isNoop shouldBe true
                }
            }
        }
    }
    
    "magnitude should return absolute value of diff" {
        checkAll(Arb.string(), Arb.int()) { data, diff ->
            val change = Change(data, diff)
            change.magnitude shouldBe kotlin.math.abs(diff)
        }
    }
    
    "inverse should negate the diff" {
        checkAll(Arb.string(), Arb.int()) { data, diff ->
            val change = Change(data, diff)
            val inverse = change.inverse()
            
            inverse.data shouldBe data
            inverse.diff shouldBe -diff
        }
    }
    
    "double inverse should return original change" {
        checkAll(Arb.string(), Arb.int()) { data, diff ->
            val change = Change(data, diff)
            change.inverse().inverse() shouldBe change
        }
    }
    
    "plus operator should combine changes for same data" {
        checkAll(Arb.string(), Arb.int(), Arb.int()) { data, diff1, diff2 ->
            val change1 = Change(data, diff1)
            val change2 = Change(data, diff2)
            val combined = change1 + change2
            
            combined.data shouldBe data
            combined.diff shouldBe (diff1 + diff2)
        }
    }
    
    "plus operator should be commutative" {
        checkAll(Arb.string(), Arb.int(), Arb.int()) { data, diff1, diff2 ->
            val change1 = Change(data, diff1)
            val change2 = Change(data, diff2)
            
            (change1 + change2) shouldBe (change2 + change1)
        }
    }
    
    "plus operator should be associative" {
        checkAll(Arb.string(), Arb.int(), Arb.int(), Arb.int()) { data, diff1, diff2, diff3 ->
            val change1 = Change(data, diff1)
            val change2 = Change(data, diff2)
            val change3 = Change(data, diff3)
            
            ((change1 + change2) + change3) shouldBe (change1 + (change2 + change3))
        }
    }
    
    "Change.insert should create positive diff" {
        checkAll(Arb.string()) { data ->
            val change = Change.insert(data)
            change.data shouldBe data
            change.diff shouldBe 1
            change.isInsertion shouldBe true
        }
    }
    
    "Change.remove should create negative diff" {
        checkAll(Arb.string()) { data ->
            val change = Change.remove(data)
            change.data shouldBe data
            change.diff shouldBe -1
            change.isDeletion shouldBe true
        }
    }
    
    "Change.insert with count should create positive diff with specified count" {
        checkAll(Arb.string(), Arb.int(0..1000)) { data, count ->
            val change = Change.insert(data, count)
            change.data shouldBe data
            change.diff shouldBe count
            if (count > 0) {
                change.isInsertion shouldBe true
            } else {
                change.isNoop shouldBe true
            }
        }
    }
    
    "Change.remove with count should create negative diff with specified count" {
        checkAll(Arb.string(), Arb.int(0..1000)) { data, count ->
            val change = Change.remove(data, count)
            change.data shouldBe data
            change.diff shouldBe -count
            if (count > 0) {
                change.isDeletion shouldBe true
            } else {
                change.isNoop shouldBe true
            }
        }
    }
    
    "adding a change and its inverse should result in noop" {
        checkAll(Arb.string(), Arb.int()) { data, diff ->
            val change = Change(data, diff)
            val inverse = change.inverse()
            val combined = change + inverse
            
            combined.data shouldBe data
            combined.diff shouldBe 0
            combined.isNoop shouldBe true
        }
    }
    
    "changes with same data and diff should be equal" {
        checkAll(Arb.string(), Arb.int()) { data, diff ->
            val change1 = Change(data, diff)
            val change2 = Change(data, diff)
            
            change1 shouldBe change2
            change1.hashCode() shouldBe change2.hashCode()
        }
    }
    
    "changes with different data should not be equal" {
        checkAll(Arb.string(), Arb.string(), Arb.int()) { data1, data2, diff ->
            if (data1 != data2) {
                val change1 = Change(data1, diff)
                val change2 = Change(data2, diff)
                
                change1 shouldNotBe change2
            }
        }
    }
    
    "changes with different diff should not be equal" {
        checkAll(Arb.string(), Arb.int(), Arb.int()) { data, diff1, diff2 ->
            if (diff1 != diff2) {
                val change1 = Change(data, diff1)
                val change2 = Change(data, diff2)
                
                change1 shouldNotBe change2
            }
        }
    }
}) 