package statemachines

import GenericToken
import IdentifierToken
import IntegerToken
import ParsingError
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
    private var currentState: State = Q0
    private var stringValue = ""
    private var integerValue = 0

    fun reset(){
        stringValue = ""
        integerValue = 0
        currentState = Q0
    }
    fun executeTransition(entry: Char): State {
        currentState = when (currentState) {
            Q0 -> {
                when (entry) {
                    in del -> Q0
                    in 'A'..'Z', in 'a'..'z' -> {
                        stringValue += entry
                        Q1
                    }
                    in '0'..'9' -> {
                        integerValue = entry.digitToInt()
                        Q2
                    }
                    in operators -> {
                        stringValue += entry
                        Q3
                    }
                    in singleCharSymbols -> {
                        val code = codesMap[entry.toString()]!!
                        Q4(GenericToken(code))
                    }
                    '&' -> Q5
                    '|' -> Q6
                    '\'' -> Q7
                    '/' -> Q8
                    else -> StateError(ParsingError.BAD_CHARACTER)
                }
            }
            Q1 -> {
                when(entry){
                    in 'A'..'Z', in 'a'..'z', in '0'..'9' -> {
                        stringValue += entry
                        Q1
                    }
                    in del, in singleCharSymbols, in operators, '&', '|' -> {
                        if(stringValue in keywords){
                            val code = codesMap[stringValue]!!
                            Q10(GenericToken(code))
                        } else Q11(IdentifierToken(stringValue))
                    }
                    else -> StateError(ParsingError.BAD_IDENTIFIER_NAMING)
                }
            }
            Q2 -> {
                when (entry) {
                    in '0'..'9' -> {
                        integerValue = integerValue * 10 + entry.digitToInt()
                        if(integerValue in INTEGER_LOWER_BOUND..INTEGER_UPPER_BOUND)
                            Q2
                        else StateError(ParsingError.INTEGER_OVERFLOW)
                    }
                    in del, in singleCharSymbols, in operators, '&', '|' -> Q12(IntegerToken(integerValue))
                    else -> StateError(ParsingError.BAD_INTEGER_FORMATTING)
                }
            }
            Q3 -> {
                when(entry){
                    '=' -> {
                        stringValue += entry
                        val code = codesMap[stringValue]!!
                        Q13(GenericToken(code))
                    }
                    else -> {
                        val code = codesMap[stringValue]!!
                        Q14(GenericToken(code))
                    }
                }
            }
            Q5 -> {
                when(entry){
                    '&' ->  Q15
                    else -> StateError(ParsingError.BAD_OPERATOR_FORMATTING)
                }
            }
            Q6 -> {
                when(entry){
                    '|' -> Q16
                    else -> StateError(ParsingError.BAD_OPERATOR_FORMATTING)
                }
            }
            Q7 -> {
                if (entry == '\'') Q17(StringToken(stringValue))
                else {
                    stringValue += entry
                    if(stringValue.length <= STRING_SIZE_LIMIT) Q7
                    else StateError(ParsingError.STRING_OVERFLOW)
                }
            }
            Q8 -> {
                when(entry){
                    '=' -> Q18
                    '/' ->  Q19
                    else -> Q23
                }
            }
            Q19 -> {
                when(entry){
                    '\n' -> Q0
                    else -> Q19
                }
            }
            else -> StateError(ParsingError.GENERIC_ERROR)
        }
        return currentState
    }

}

sealed class State

class StateError(val error: ParsingError): State()
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
class Q14(token: GenericToken): FinalState(token, true)
object Q15: FinalState(GenericToken(TokenCode.LOGICAL_AND), true)
object Q16: FinalState(GenericToken(TokenCode.LOGICAL_OR), true)
class Q17(token: StringToken): FinalState(token, true)
object Q18: FinalState(GenericToken(TokenCode.DIVIDE_EQUAL),true)
object Q19: State()
object Q23: FinalState(GenericToken(TokenCode.DIVISION),false)








