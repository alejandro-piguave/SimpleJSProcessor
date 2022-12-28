package lexical

import lexical.statemachine.LexicalStateMachineException

sealed class LexicalException(message: String?): Exception(message)

class StateMachineException(line: Int, lexicalStateMachineException: LexicalStateMachineException): LexicalException("Exception in line $line: ${lexicalStateMachineException.message}")
object NoNextTokenException: LexicalException("The end of the file was reached. There are no more tokens.")