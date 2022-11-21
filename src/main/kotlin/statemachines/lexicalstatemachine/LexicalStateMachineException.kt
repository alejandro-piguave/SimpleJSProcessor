package statemachines.lexicalstatemachine

sealed class LexicalStateMachineException(val code: Int, message: String): Exception(message)

object IntegerOverflowException : LexicalStateMachineException(1, "An integer's value must be between 0 and 32767.")
object StringOverflowException : LexicalStateMachineException(2, "A string can't be longer than 64 characters.")
class IllegalCharacterException(char: Char) : LexicalStateMachineException(3, "The character '$char' is not recognized by the analyzer.")
class IdentifierFormatException(char: Char): LexicalStateMachineException(4, "Unexpected character '$char' found while reading an identifier.")
class IntegerFormatException(char: Char): LexicalStateMachineException(5, "Unexpected character '$char' found while reading an integer value.")
class OperatorFormatException(char: Char): LexicalStateMachineException(6, "Unexpected character '$char' found while reading an operator.")
