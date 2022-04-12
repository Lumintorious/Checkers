package checkers


opaque type Capitalized = Any
inline given Checker[String, Capitalized] =
  Checker[String, Capitalized] { s =>
    s.length == 0 || s.charAt(0).isUpper
  }

opaque type Uppercase = Any
inline given Checker[String, Uppercase] =
  Checker[String, Uppercase] { s =>
    s == s.toUpperCase
  }

opaque type Lowercase = Any
inline given Checker[String, Lowercase] =
  Checker { s =>
    s == s.toLowerCase
  }

opaque type Matches[Regex] = Any
inline given [Regex <: String](using V: ValueOf[Regex]): Checker[String, Matches[Regex]] =
  Checker[String, Matches[Regex]] { 
    _.matches(V.value)
  }

opaque type AlphaNumeric = Any
inline given Checker[String, AlphaNumeric] =
  Checker[String, AlphaNumeric] {
    _.matches("^[a-zA-Z0-9_]*$")
  }

opaque type Alphabetic = Any
inline given Checker[String, Alphabetic] =
  Checker[String, AlphaNumeric] {
    _.matches("^[a-zA-Z\\ ]*$")
  }

opaque type NoSpaces = Any
inline given Checker[String, NoSpaces] = 
  Checker { !_.contains(" ") }

opaque type PhoneNumberLike = Any
inline given Checker[String, PhoneNumberLike] = 
  Checker { _.matches("^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]\\d{3}[\\s.-]\\d{4}$") }

opaque type EmailLike = Any
inline given Checker[String, EmailLike] = 
  Checker { str =>
    val parts = str.split("\\@")
    if(parts.length < 2) {
      false
    } else {
      val partsAfterAt = parts(1).split("\\.")
      if(partsAfterAt.length < 2) {
        false
      } else {
        true
      }
    }
  }
  
type PhoneNumber = String & PhoneNumberLike
type Email = String & EmailLike
