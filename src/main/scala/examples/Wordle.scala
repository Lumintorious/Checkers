package checkers
package examples

import checkers.{given, *}
import checkers.unsafe.*
import checkers.macros.TypeShow
import Guarantee.automatically

import checkers.eng.given
import scala.annotation.tailrec

type Word = String & LettersOnly & SizeOf[5]

sealed trait WordleEnding {
  def display: String
}

final case class WordleWin(answer: String, triesLeft: Int) extends WordleEnding {
  def display =
    s"You won. The answer was ${answer}. Tries left: ${triesLeft}."
}
final case class WordleLoss(correctAnswer: String) extends WordleEnding {
  def display =
    s"You lost. The answer was ${correctAnswer}."
}

object WordleState {
  def withAnswer(answer: Word) =
    WordleState(answer, 6, List.empty)
}

final case class WordleState private(
  answer: Word,
  tries: Int & Positive,
  guesses: List[Word]
) {
  def tryGuess(guess: Word): Either[WordleState, WordleEnding] = this match {
    case _ if answer == guess =>
      Right(WordleWin(answer, guesses.length - tries))
    case _ if tries == guesses.length =>
      Right(WordleLoss(answer))
    case _ =>
      Left(this.failed(guess))
  }

  private inline def failed(newGuess: Word): WordleState =
    this.copy(
      guesses = newGuess :: guesses
    )

  def showGuesses: String = {
    guesses.map(displayGuess(_, answer)).reverse.mkString("\n")
  }
}

def displayGuess(guess: Word, answer: Word): String = {
  import Console.*
  (guess.toCharArray zip answer.toCharArray).toList.map {
    case (g, a: Char) if g == a => s"${BOLD}${GREEN}${g.toUpper}${RESET}"
    case (g, _) if answer.toList.contains(g) => s"${BOLD}${YELLOW}${g.toUpper}${RESET}"
    case (g, _) => s"${BOLD}${g.toUpper}${RESET}"
  }.map(_ + " ").mkString
}

def untilRight[E, A](current: E)(reduce: E => Either[E, A]): A = {
  reduce(current).fold(untilRight(_)(reduce), identity)
}

extension [A] (self: A) {
  inline def tighten[B]: A & B =
    compiletime.summonInline[Guarantee[self.type, B]].convert(self)
}

@main def runWordle() = {
  val start = WordleState.withAnswer("horse")
  val outcome = untilRight(start) { state =>
    println("=" * 20)
    println(state.showGuesses)
    val response = {
      Console.out.print("Guess: ")
      Console.in.readLine()
    }
    state.tryGuess.checking(response) match {
      case Left(failures) =>
        println("=" * 20)
        println(failures.mkString("\n"))
        Left(state)
      case Right(either) =>
        either
    }
  }

  // while(state.checkIs[OngoingState]) {
  //   println("=" * 20)
  //   println(state.display)
  //   val response = {
  //     Console.out.print("Guess: ")
  //     Console.in.readLine()
  //   }
  //   state.tryGuess.checking(response) match {
  //     case Right(newState) =>
  //       state = newState
  //     case Left(failedChecks) =>
  //       Console.err.println(failedChecks.mkString("\n"))
  //       Console.err.println("Please try again")
  //   }
  // }
  // println(state.display)
}
