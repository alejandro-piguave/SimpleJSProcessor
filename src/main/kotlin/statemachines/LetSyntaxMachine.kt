package statemachines

import EntryType
import IdentifierToken
import Token
import TokenCode

class LetIdentifiersMachine {

    private var currentState: LetIdentifiersMachineState = LetInitialState
    private val currentLetIdentifiers: MutableList<IdentifierToken> = mutableListOf()

    fun reset(){
        currentState = LetInitialState
        currentLetIdentifiers.clear()
    }

    fun updateState(token: Token): LetIdentifiersMachineState {
        currentState = when(currentState){
            is LetInitialState -> {
                when(token.code){
                    TokenCode.LET -> DeclaringLetIdentifier
                    else -> LetInitialState
                }
            }
            is DeclaringLetIdentifier -> {
                when(token){
                    is IdentifierToken -> {
                        currentLetIdentifiers.add(token)
                        AfterDeclaringFirstLetIdentifier
                    }
                    else -> LetDeclarationError
                }
            }
            is AfterDeclaringFirstLetIdentifier -> {
                when(token.code){
                    TokenCode.COMMA -> DeclaringLetIdentifier
                    TokenCode.STRING_KEYWORD -> AfterDeclaringLetType(EntryType.STRING, currentLetIdentifiers)
                    TokenCode.INTEGER_KEYWORD -> AfterDeclaringLetType(EntryType.INTEGER, currentLetIdentifiers)
                    TokenCode.BOOLEAN_KEYWORD -> AfterDeclaringLetType(EntryType.BOOLEAN, currentLetIdentifiers)
                    else -> LetDeclarationError
                }
            }
            else -> currentState
        }
        return currentState
    }
}

sealed class LetIdentifiersMachineState
object LetDeclarationError: LetIdentifiersMachineState()
object LetInitialState : LetIdentifiersMachineState()
object DeclaringLetIdentifier : LetIdentifiersMachineState()
object AfterDeclaringFirstLetIdentifier : LetIdentifiersMachineState()
class AfterDeclaringLetType(val type: EntryType, val tokens: List<IdentifierToken>): LetIdentifiersMachineState()
