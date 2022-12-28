package lexical.statemachine

class LexicalStateMachine {

    companion object{
        private const val INTEGER_UPPER_BOUND = 32767
        private const val STRING_SIZE_LIMIT = 64
    }

    private var currentState: LexicalState = LexicalState.Q0
    private var stringValue = ""
    private var integerValue = 0

    fun reset(){
        stringValue = ""
        integerValue = 0
        currentState = LexicalState.Q0
    }

    fun executeTransition(entry: Char): LexicalState {
        currentState = when (currentState) {
            LexicalState.Q0 -> {
                when (entry) {
                    in del -> LexicalState.Q0
                    '_', in 'A'..'Z', in 'a'..'z' -> {
                        stringValue += entry
                        LexicalState.Q1
                    }
                    in '0'..'9' -> {
                        integerValue = entry.digitToInt()
                        LexicalState.Q2
                    }
                    in operators -> {
                        stringValue += entry
                        LexicalState.Q3
                    }
                    in singleCharSymbols -> {
                        val code = codesMap[entry.toString()]!!
                        LexicalState.Q4(code)
                    }
                    '&' -> LexicalState.Q5
                    '|' -> LexicalState.Q6
                    '\'' -> LexicalState.Q7
                    '/' -> LexicalState.Q8
                    else -> throw IllegalCharacterException(entry)
                }
            }
            LexicalState.Q1 -> {
                when(entry){
                    '_', in 'A'..'Z', in 'a'..'z', in '0'..'9' -> {
                        stringValue += entry
                        LexicalState.Q1
                    }
                    in del, in singleCharSymbols, in operators, '&', '|' -> {
                        if(stringValue in keywords){
                            val code = codesMap[stringValue]!!
                            LexicalState.Q10(code)
                        } else LexicalState.Q11(stringValue)
                    }
                    else -> throw IdentifierFormatException(entry)
                }
            }
            LexicalState.Q2 -> {
                when (entry) {
                    in '0'..'9' -> {
                        integerValue = integerValue * 10 + entry.digitToInt()
                        if(integerValue in 0..INTEGER_UPPER_BOUND)
                            LexicalState.Q2
                        else throw IntegerOverflowException
                    }
                    in del, in singleCharSymbols, in operators, '&', '|' -> LexicalState.Q12(integerValue)
                    else -> throw IntegerFormatException(entry)
                }
            }
            LexicalState.Q3 -> {
                when(entry){
                    '=' -> {
                        stringValue += entry
                        val code = codesMap[stringValue]!!
                        LexicalState.Q13(code)
                    }
                    else -> {
                        val code = codesMap[stringValue]!!
                        LexicalState.Q14(code)
                    }
                }
            }
            LexicalState.Q5 -> {
                when(entry){
                    '&' -> LexicalState.Q15
                    else -> throw OperatorFormatException(entry)
                }
            }
            LexicalState.Q6 -> {
                when(entry){
                    '|' -> LexicalState.Q16
                    '=' -> LexicalState.Q24
                    else -> throw OperatorFormatException(entry)
                }
            }
            LexicalState.Q7 -> {
                if (entry == '\'') LexicalState.Q17(stringValue)
                else {
                    stringValue += entry
                    if(stringValue.length <= STRING_SIZE_LIMIT) LexicalState.Q7
                    else throw StringOverflowException
                }
            }
            LexicalState.Q8 -> {
                when(entry){
                    '=' -> LexicalState.Q18
                    '/' -> LexicalState.Q19
                    else -> LexicalState.Q23
                }
            }
            LexicalState.Q19 -> {
                when(entry){
                    '\n' -> LexicalState.Q0
                    else -> LexicalState.Q19
                }
            }
            is LexicalState.FinalState -> {currentState}
        }
        return currentState
    }
}