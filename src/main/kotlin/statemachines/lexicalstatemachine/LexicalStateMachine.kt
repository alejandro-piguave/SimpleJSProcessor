package statemachines.lexicalstatemachine

import GenericToken
import IdentifierToken
import IntegerToken
import StringToken
import Token
import TokenCode
import codesMap
import del
import keywords
import operators
import singleCharSymbols

class LexicalStateMachine {

    companion object{
        private const val INTEGER_LOWER_BOUND = -32768
        private const val INTEGER_UPPER_BOUND = 32767
        private const val STRING_SIZE_LIMIT = 64
    }
    private var currentState: State = State.Q0
    private var stringValue = ""
    private var integerValue = 0

    fun reset(){
        stringValue = ""
        integerValue = 0
        currentState = State.Q0
    }

    fun executeTransition(entry: Char): State {
        currentState = when (currentState) {
            State.Q0 -> {
                when (entry) {
                    in del -> State.Q0
                    '_', in 'A'..'Z', in 'a'..'z' -> {
                        stringValue += entry
                        State.Q1
                    }
                    in '0'..'9' -> {
                        integerValue = entry.digitToInt()
                        State.Q2
                    }
                    in operators -> {
                        stringValue += entry
                        State.Q3
                    }
                    in singleCharSymbols -> {
                        val code = codesMap[entry.toString()]!!
                        State.Q4(GenericToken(code))
                    }
                    '&' -> State.Q5
                    '|' -> State.Q6
                    '\'' -> State.Q7
                    '/' -> State.Q8
                    else -> throw IllegalCharacterException(entry)
                }
            }
            State.Q1 -> {
                when(entry){
                    '_', in 'A'..'Z', in 'a'..'z', in '0'..'9' -> {
                        stringValue += entry
                        State.Q1
                    }
                    in del, in singleCharSymbols, in operators, '&', '|' -> {
                        if(stringValue in keywords){
                            val code = codesMap[stringValue]!!
                            State.Q10(GenericToken(code))
                        } else State.Q11(IdentifierToken(stringValue))
                    }
                    else -> throw IdentifierFormatException(entry)
                }
            }
            State.Q2 -> {
                when (entry) {
                    in '0'..'9' -> {
                        integerValue = integerValue * 10 + entry.digitToInt()
                        if(integerValue in INTEGER_LOWER_BOUND..INTEGER_UPPER_BOUND)
                            State.Q2
                        else throw IntegerOverflowException
                    }
                    in del, in singleCharSymbols, in operators, '&', '|' -> State.Q12(IntegerToken(integerValue))
                    else -> throw IntegerFormatException(entry)
                }
            }
            State.Q3 -> {
                when(entry){
                    '=' -> {
                        stringValue += entry
                        val code = codesMap[stringValue]!!
                        State.Q13(GenericToken(code))
                    }
                    else -> {
                        val code = codesMap[stringValue]!!
                        State.Q14(GenericToken(code))
                    }
                }
            }
            State.Q5 -> {
                when(entry){
                    '&' -> State.Q15
                    else -> throw OperatorFormatException(entry)
                }
            }
            State.Q6 -> {
                when(entry){
                    '|' -> State.Q16
                    '=' -> State.Q24
                    else -> throw OperatorFormatException(entry)
                }
            }
            State.Q7 -> {
                if (entry == '\'') State.Q17(StringToken(stringValue))
                else {
                    stringValue += entry
                    if(stringValue.length <= STRING_SIZE_LIMIT) State.Q7
                    else throw StringOverflowException
                }
            }
            State.Q8 -> {
                when(entry){
                    '=' -> State.Q18
                    '/' -> State.Q19
                    else -> State.Q23
                }
            }
            State.Q19 -> {
                when(entry){
                    '\n' -> State.Q0
                    else -> State.Q19
                }
            }
            is State.FinalState -> {currentState}
        }
        return currentState
    }

    sealed class State{
        abstract class FinalState(val token: Token, val nextChar: Boolean): State()
        object Q0: State()
        object Q1: State()
        object Q2: State()
        object Q3: State()
        class Q4(token: GenericToken): FinalState(token,true)
        object Q5: State()
        object Q6: State()
        object Q7: State()
        object Q8: State()
        class Q10(token: GenericToken): FinalState(token, false)
        class Q11(token: IdentifierToken): FinalState(token, false)
        class Q12(token: IntegerToken): FinalState(token, false)
        class Q13(token: GenericToken): FinalState(token, true)
        class Q14(token: GenericToken): FinalState(token, false)
        object Q15: FinalState(GenericToken(TokenCode.LOGICAL_AND), true)
        object Q16: FinalState(GenericToken(TokenCode.LOGICAL_OR), true)
        class Q17(token: StringToken): FinalState(token, true)
        object Q18: FinalState(GenericToken(TokenCode.DIVIDE_EQUAL),true)
        object Q19: State()
        object Q23: FinalState(GenericToken(TokenCode.DIVISION),false)
        object Q24: FinalState(GenericToken(TokenCode.OR_EQUAL),true)
    }

}










