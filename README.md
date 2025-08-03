# Dedekind: Differential Dataflow Library for Kotlin

## Project Overview

**Dedekind** is a differential dataflow library for Kotlin that enables efficient incremental computation over changing datasets by tracking changes (differences) rather than recomputing entire results. Named after mathematician Richard Dedekind, this library provides a foundation for building reactive data processing systems.

## Current Status

✅ **Phase 1 Complete**: Core data structures and basic operators

### Implemented Features

#### Core Data Structures
- **Collection<T>**: Immutable multiset supporting differential dataflow operations
- **Change<T>**: Represents insertions/deletions with multiplicity
- **Trace<T>**: Historical sequence of changes with timestamp management
- **TimestampedChange<T>**: Changes associated with logical time

#### Basic Operators
- **map**: Transform elements `(T) -> R`
- **filter**: Keep elements matching a predicate
- **flatMap**: Transform and flatten collections
- **distinct**: Remove duplicates
- **concat**: Union of collections

#### Relational Operators
- **join**: Equijoin between collections with key selectors
- **groupBy**: Group by key with aggregation function
- **reduce**: Reduce with associative operation

#### Utility Operations
- **count**: Get multiplicity of elements
- **toList/toSet/toMultiplicityMap**: Convert to standard collections
- **asSequence**: Stream elements with multiplicities

## Project Setup

### Requirements
- **Kotlin**: 2.2.0
- **JVM**: Java 17+
- **Build System**: Gradle with Kotlin DSL
- **Testing**: Kotest 5.9.1

### Building and Testing

```bash
# Build the library
./gradlew build

# Run tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test classes
./gradlew test --tests "*BasicCollectionTest*"
./gradlew test --tests "*BasicChangeTest*"
./gradlew test --tests "*ExampleUsageTest*"
```

### Development Environment
The project uses devenv.nix for consistent development environments:

```bash
# Enter development environment
devenv shell

# Or use direnv for automatic environment loading
echo "use devenv" > .envrc
direnv allow
```

## Usage Examples

### Basic Collection Operations

```kotlin
// Create collections
val numbers = Collection.fromIterable(listOf(1, 2, 3, 2))
val empty = Collection.empty<Int>()
val singleton = Collection.of("hello")

// Basic operations
val doubled = numbers.map { it * 2 }        // [2, 4, 6, 4]
val evens = numbers.filter { it % 2 == 0 }  // [2, 2]
val unique = numbers.distinct()             // [1, 2, 3]

// Multiplicities are preserved
println(numbers.count(2))      // 2 (appears twice)
println(doubled.count(4))      // 2 (from 2 * 2, appears twice)
```

### Data Processing Example

```kotlin
// User and purchase data
val users = Collection.fromIterable(listOf(
    User(1, "Alice"),
    User(2, "Bob"),
    User(3, "Charlie")
))

val purchases = Collection.fromIterable(listOf(
    Purchase(1, "Book"),
    Purchase(1, "Pen"), 
    Purchase(2, "Laptop"),
    Purchase(1, "Notebook"),
    Purchase(3, "Phone")
))

// Calculate purchase counts per user
val userPurchaseCounts = users
    .join(
        purchases,
        keySelector = { user -> user.id },
        otherKeySelector = { purchase -> purchase.userId },
        resultSelector = { user, purchase -> user to purchase }
    )
    .groupBy(
        keySelector = { (user, _) -> user },
        aggregator = { user, purchases -> user to purchases.size }
    )

// Results: Alice -> 3, Bob -> 1, Charlie -> 1
```

### Change Tracking with Traces

```kotlin
// Create a trace to track changes over time
val trace = TraceBuilder.empty<String>()

// Record changes at different timestamps
trace.advance(1)
trace.insert("apple")
trace.insert("banana")

trace.advance(2) 
trace.insert("apple")  // Another apple
trace.remove("banana")

trace.advance(3)
trace.insert("cherry")

// Get current state
val currentState = trace.consolidate()
// Result: {apple=2, cherry=1} (banana removed)

// Get recent changes
val recentChanges = trace.changesFrom(2)
```

### Incremental Updates

```kotlin
// Start with initial data
val purchases = Collection.fromIterable(listOf(
    Purchase(1, "Book"),
    Purchase(2, "Laptop")
))

// Add new purchases incrementally
val newPurchases = Collection.fromIterable(listOf(
    Purchase(1, "Pen"),
    Purchase(3, "Phone")
))

val updated = purchases.concat(newPurchases)
// All operations preserve multiplicities correctly
```

## Design Principles

### Immutability
All collections are immutable. Operations return new collections without modifying the original.

### Multiplicity Preservation
The library correctly handles elements that appear multiple times, preserving counts through all operations.

### Type Safety
Full compile-time type checking with Kotlin's type system.

### Composability  
Operations can be chained naturally using method chaining.

### Performance Considerations
- Copy-on-write semantics for memory efficiency
- Efficient implementations for empty and singleton collections
- Optimized join operations with indexing

## Testing Strategy

The library uses comprehensive testing with Kotest:

### Basic Functionality Tests
- Core data structure behavior
- Operator correctness
- Edge cases and error conditions

### Example Usage Tests
- Real-world scenarios
- Chained operations
- Integration between components

### Algebraic Property Tests
Future property-based tests will verify:
- Associativity: `(a ⋈ b) ⋈ c = a ⋈ (b ⋈ c)`
- Commutativity: `a ⋈ b = b ⋈ a`
- Identity: `a ⋈ ∅ = a`
- Distributivity: `a ⋈ (b ∪ c) = (a ⋈ b) ∪ (a ⋈ c)`

## Next Steps (Planned Phases)

### Phase 2: Advanced Operators
- **iterate**: Fixed-point iteration for recursive queries
- **semijoin/antijoin**: Existence-based joins
- **consolidate**: Merge changes with same data
- **arrange**: Sort and index collections

### Phase 3: Performance Optimization
- Lazy evaluation strategies
- Parallel processing support
- Memory usage optimization
- Benchmarking suite

### Phase 4: Differential Updates
- Efficient incremental computation
- Delta processing
- Batch update handling
- Timestamp management improvements

### Phase 5: Advanced Features
- Complex query optimization
- Streaming data support
- Persistent storage integration
- Monitoring and debugging tools

## Contributing

This project follows standard Kotlin development practices:

1. **Code Style**: Follow Kotlin coding conventions
2. **Testing**: Add tests for all new functionality
3. **Documentation**: Include KDoc for all public APIs
4. **Type Safety**: Maintain full compile-time type checking

## License

See [LICENSE](LICENSE) file for details.

---

**Dedekind** provides a solid foundation for differential dataflow programming in Kotlin, enabling efficient incremental computation with clean, composable APIs.

