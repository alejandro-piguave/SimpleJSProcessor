package lexical

import GenericToken
import IntegerToken
import SOURCE_PATHNAME
import StringToken
import SymbolsTable
import Token
import lexical.statemachine.LexicalState
import lexical.statemachine.LexicalStateMachine
import lexical.statemachine.LexicalStateMachineException
import java.io.File

class LexicalAnalyzer(private val tableManager: SymbolsTable,
                      private val tokens: MutableList<Token>,
                      sourcePath: String? = null
) {
    private val stateMachine = LexicalStateMachine()
    var fileLine = 1
        private set
    private var currentChar: Int = 0
    private val text: String

    init {
        val file = File(sourcePath ?: SOURCE_PATHNAME)
        text = buildString {
            file.forEachLine { line ->
                append(line+"\n")
            }
            append("$")//EOF
        }
    }

    fun reset(){
        fileLine = 1
        currentChar = 0
        stateMachine.reset()
    }

    fun getNextToken(): Token {
        while(currentChar < text.length){
            val char = text[currentChar]
            if(char == '\n') {
                fileLine++
            }
            try {
                val result = stateMachine.executeTransition(char)
                if(result is LexicalState.FinalState){
                    val token = convertToToken(result)
                    tokens.add(token)
                    if (result.nextChar) currentChar++
                    stateMachine.reset()
                    return token
                } else currentChar++

            }catch (e: LexicalStateMachineException){
                stateMachine.reset()
                currentChar++
                throw StateMachineException(fileLine, e)
            }


        }
        throw NoNextTokenException
    }


    private fun convertToToken(result: LexicalState.FinalState): Token{
        return when(result){
            is LexicalState.GenericFinalState -> GenericToken(result.code)
            is LexicalState.IdFinalState -> tableManager.getIdToken(fileLine, result.name)
            is LexicalState.IntFinalState -> IntegerToken(result.value)
            is LexicalState.StringFinalState -> StringToken(result.value)
        }
    }
}
