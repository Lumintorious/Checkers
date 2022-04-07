// package wordleexample

// import confinement.{given, *}
// import cats.data.Validated

// type WORD_SIZE = 5
// type Word = String & Alphabetic & SizeOf[WORD_SIZE]
// type Hints = List[Hint] & SizeOf[WORD_SIZE]

// // given IfBroken[Word] = s => s"'${s}' was not a valid word"

// enum Hint {
//   case Miss
//   case Hit
//   case HitDifferent
// }

// final case class Guess(
//   word: Word,
//   hints: Hints
// ) {
//   def show: String = {
//     extension (char: Char) def wrapIn(color: String): String = 
//       s"${color}${Console.BOLD}${char.toUpper}${Console.RESET}"

//     (hints zip word.toCharArray).map {
//       case (Hint.Miss, char) => char.wrapIn(Console.WHITE)
//       case (Hint.Hit,  char) => char.wrapIn(Console.GREEN)
//       case (Hint.HitDifferent, char) => char.wrapIn(Console.YELLOW)
//     }.mkString
//   }
// }

// object Guess {
//   def forDifference(guessed: Word, answer: Word): Guess = {
//     val guessedArr = guessed.toCharArray
//     val answerArr  = answer.toCharArray
//     val hints = (guessedArr zip answerArr).toList.map { (pair) =>
//       if(pair(0) == pair(1)) {
//         Hint.Hit
//       } else if(answer.contains(pair(0))) {
//         Hint.HitDifferent
//       } else {
//         Hint.Miss
//       }
//     }

//     import confinement.unsafe.*
//     Guess(guessed, hints.trustAs[Hints])
//   }
// }

// final case class WordleState(
//   answer: Word,
//   guesses: List[Guess],
//   isWon: Boolean
// ) {
//   def tryGuess(word: Word): WordleState = {
//     if(word == answer) {
//       this.copy(isWon = true)
//     } else {
//       this.copy(guesses = guesses :+ Guess.forDifference(word, answer))
//     }
//   }
// }

// import Hint.*

// @main def wordle() =
//   import confinement.unsafe.*
//   var state = WordleState("genie".trustAs[Word], List(), false)
  
//   while(!state.isWon && state.guesses.length < 6) {
//     println("=======")
//     println("State:")
//     println(state.guesses.map(_.show).mkString("\n"))

//     val guessStr = io.StdIn.readLine("Guess: ")
//     guessStr.validateAs[Word] match
//       case Validated.Valid(w) => state = state.tryGuess(w)
//       case Validated.Invalid(l) => System.err.println(l.toList.mkString("\n"))
//   }
//   if(state.isWon) {
//     println(s"You won! (${state.answer.toUpperCase})")
//   } else {
//     println(state.guesses.map(_.show).mkString("\n"))
//     println(s"You lost, the answer was ${state.answer.toUpperCase}")
//   }