package statemachines

import EntryType
import IdentifierToken
import Token

class FunctionStateMachine {
    private var currentState: FunctionMachineState = InitialState
    private var currentFunctionName: String = ""
    private var returnType = EntryType.VOID
    private val parameters: MutableList<FunctionParameter> = mutableListOf()

    fun reset(){
        currentFunctionName = ""
        currentState = InitialState
        parameters.clear()
    }

    fun updateState(token: Token): FunctionMachineState{
        val currentStateCopy = currentState
        currentState = when(currentStateCopy){
            InitialState -> {
                when (token.code) {
                    TokenCode.FUNCTION -> DeclaringFunctionIdentifier
                    else -> InitialState
                }
            }
            is DeclaringFunctionIdentifier -> {
                when(token){
                    is IdentifierToken -> {
                        currentFunctionName = token.name
                        DeclaringFunctionReturnType
                    }
                    else -> DeclarationError
                }
            }
            is DeclaringFunctionReturnType -> {
                when(token.code){
                    TokenCode.STRING_KEYWORD -> {
                        returnType = EntryType.STRING
                        DeclaringFunctionLeftParenthesis
                    }
                    TokenCode.INTEGER_KEYWORD -> {
                        returnType = EntryType.INTEGER
                        DeclaringFunctionLeftParenthesis
                    }
                    TokenCode.BOOLEAN_KEYWORD -> {
                        returnType = EntryType.BOOLEAN
                        DeclaringFunctionLeftParenthesis
                    }
                    TokenCode.LEFT_PARENTHESIS -> DeclaringFunctionParameters
                    else -> DeclarationError
                }
            }
            is DeclaringFunctionLeftParenthesis -> {
                when(token.code){
                    TokenCode.LEFT_PARENTHESIS -> DeclaringFunctionParameters
                    else -> DeclarationError
                }
            }
            is DeclaringFunctionParameters -> {
                when(token.code){
                    TokenCode.STRING_KEYWORD -> DeclaringStringFunctionParameter
                    TokenCode.INTEGER_KEYWORD -> DeclaringIntegerFunctionParameter
                    TokenCode.BOOLEAN_KEYWORD -> DeclaringBooleanFunctionParameter
                    TokenCode.RIGHT_PARENTHESIS -> DeclaringFunctionLeftBracket
                    else -> DeclarationError
                }
            }
            is DeclaringStringFunctionParameter -> {
                if(token is IdentifierToken){
                    parameters.add(FunctionParameter(token.name, EntryType.STRING))
                    AfterDeclaringParameterIdentifier
                } else DeclarationError
            }
            is DeclaringIntegerFunctionParameter -> {
                if(token is IdentifierToken){
                    parameters.add(FunctionParameter(token.name, EntryType.INTEGER))
                    AfterDeclaringParameterIdentifier
                } else DeclarationError
            }
            is DeclaringBooleanFunctionParameter -> {
                if(token is IdentifierToken){
                    parameters.add(FunctionParameter(token.name, EntryType.BOOLEAN))
                    AfterDeclaringParameterIdentifier
                } else DeclarationError
            }
            is AfterDeclaringParameterIdentifier -> {
                when(token.code){
                    TokenCode.COMMA -> DeclaringFunctionParameters
                    TokenCode.RIGHT_PARENTHESIS -> DeclaringFunctionLeftBracket
                    else -> DeclarationError
                }
            }
            is DeclaringFunctionLeftBracket -> {
                when(token.code){
                    TokenCode.LEFT_BRACKET -> AfterDeclaringFunctionLeftBracket(Function(currentFunctionName, parameters, returnType))
                    else -> DeclarationError
                }
            }
            else -> currentState
        }

        return currentState
    }
}

data class FunctionParameter(val name: String, val type: EntryType)
data class Function(val name: String, val parameters: List<FunctionParameter>, val returnType: EntryType)

sealed class FunctionMachineState

object InitialState : FunctionMachineState()
object DeclarationError: FunctionMachineState()

object DeclaringFunctionIdentifier : FunctionMachineState()
object DeclaringFunctionReturnType: FunctionMachineState()
object DeclaringFunctionLeftParenthesis: FunctionMachineState()
object DeclaringFunctionParameters : FunctionMachineState()
object DeclaringStringFunctionParameter: FunctionMachineState()
object DeclaringBooleanFunctionParameter: FunctionMachineState()
object DeclaringIntegerFunctionParameter: FunctionMachineState()
object AfterDeclaringParameterIdentifier: FunctionMachineState()
object DeclaringFunctionLeftBracket: FunctionMachineState()
class AfterDeclaringFunctionLeftBracket(val function: Function): FunctionMachineState()
