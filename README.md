## Welcome to Checkers

Checkers is a library that adds type checks to Scala3 using the built-in intersection and union types.
This allows handling checked types as if they were regular types that can be used as parameters or return types, respecting variance.

### Basic idea
- Any type can be given a Checker instance to another type (preferably opaque).
- Checking a type to another means using that instance to return Valid or Invalid (from Cats)
- Functions working with checked types can be called with 'checking(...)' to be used more concretely

### Imports
```Scala
import checkers.{given, *}
```

### Basic idea
Checking if a type `T` is also a type `X` returns `ValidatedNel[FailedCheck, T & X]`
```Scala
val noSymbols = "some string".checkedAs[AlphaNumeric]
// ==> Valid("some string": String & AlphaNumeric)

val manySymbols = "some |+| -- >>=".checkedAs[AlphaNumeric]
// ==> Invalid('some |+| -- >>=' is not AlphaNumeric)
```

### Basic use
```Scala
def safeDiv(numerator: Double, denominator: Double & NonZero): Double =
  numerator / denominator
  
val result1 = safeDiv.checking(12D, 4D)
// ==> Valid(3D)

val result2 = safeDiv.checking(12D, 0D)
// ==> Invalid('0D' not NonZero)
```

### Multiple checks with intersection types
Intersection type checking behaves like and-ing the checks of their parts, adding up all errors if necessary
```Scala
def isDrunk(age: Int & GT[21], drinkAlcohol: Float & Positive & LT[0.6F]): Boolean =
  age - (age * drinkAlcohol) // Some arbitrary logic
  
val youngVsVodka = isDrunk.checking(24, 0.5F)
// ==> runs fine

val babyVsBeer = isDrunk.checking(2, 0.08F)
// ==> Invalid('2' not GT[21]

val babyVsHardVodka = isDrunk.checking(2, 0.64F)
// ==> Invalid('2' is not GT[21], '0.64' is not LT[0.6]) 

val personVsNegative = isDrunk.checking(30, -0.34F)
// ==> Invalid('-0.34' is not Positive) 
```

### Declaring a new Checker
```Scala

object OtherFile {
  opaque type Unitary = Any
  inline given Checker[Float, Unitary] =
    Checker { f => f >= -1 && f <= 1 } 
}

object ClientCode {
  import OtherFile.{given, *}
  
  2F.checkAs[Unitary]
  // ==> Invalid('2' is not Unitary)
  
  0.43F.checkAs[Unitary]
  // ==> Valid(0.43: Float & Unitary)
}

```
