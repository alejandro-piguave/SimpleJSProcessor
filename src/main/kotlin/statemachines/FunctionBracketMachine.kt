package statemachines

import Token
import TokenCode

class FunctionBracketMachine {
    private var leftBracketCount = 1

    fun reset(){
        leftBracketCount = 1
    }
    fun updateState(token: Token): Int{
        if(leftBracketCount == 0) return 0
        when(token.code){
            TokenCode.LEFT_BRACKET -> leftBracketCount++
            TokenCode.RIGHT_BRACKET -> leftBracketCount--
            else -> {}
        }
        return leftBracketCount
    }

}
