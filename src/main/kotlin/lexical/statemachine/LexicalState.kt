package lexical.statemachine

import TokenCode

sealed class LexicalState{
    sealed class FinalState(val nextChar: Boolean): LexicalState()
    abstract class GenericFinalState(val code: TokenCode, nextChar: Boolean) : FinalState(nextChar)
    abstract class IdFinalState(val name: String, nextChar: Boolean): FinalState(nextChar)
    abstract class StringFinalState(val value: String, nextChar: Boolean): FinalState(nextChar)
    abstract class IntFinalState(val value: Int, nextChar: Boolean): FinalState(nextChar)

    object Q0: LexicalState()
    object Q1: LexicalState()
    object Q2: LexicalState()
    object Q3: LexicalState()
    class Q4(tokenCode: TokenCode): GenericFinalState(tokenCode,true)
    object Q5: LexicalState()
    object Q6: LexicalState()
    object Q7: LexicalState()
    object Q8: LexicalState()
    class Q10(tokenCode: TokenCode): GenericFinalState(tokenCode, false)
    class Q11(value: String): IdFinalState(value, false)
    class Q12(value: Int): IntFinalState(value, false)
    class Q13(tokenCode: TokenCode): GenericFinalState(tokenCode, true)
    class Q14(tokenCode: TokenCode): GenericFinalState(tokenCode, false)
    object Q15: GenericFinalState(TokenCode.LOGICAL_AND, true)
    object Q16: GenericFinalState(TokenCode.LOGICAL_OR, true)
    class Q17(value: String): StringFinalState(value, true)
    object Q18: GenericFinalState(TokenCode.DIVIDE_EQUAL,true)
    object Q19: LexicalState()
    object Q23: GenericFinalState(TokenCode.DIVISION,false)
    object Q24: GenericFinalState(TokenCode.OR_EQUAL,true)
}