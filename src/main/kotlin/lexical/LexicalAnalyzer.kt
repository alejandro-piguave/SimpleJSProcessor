package lexical

import GenericToken
import IntegerToken
import StringToken
import SymbolsTable
import Token
import lexical.statemachine.LexicalState
import lexical.statemachine.LexicalStateMachine
import lexical.statemachine.LexicalStateMachineException
import java.io.File

class LexicalAnalyzer(private val tableManager: SymbolsTable,
                      private val tokens: MutableList<Token>
) {
    private val stateMachine = LexicalStateMachine()
    var fileLine = 1
        private set
    private var currentChar: Int = 0
    private val text: String

    companion object{
        const val sourcePathname = "src/main/resources/source.txt"
    }

    init {
        val file = File(sourcePathname)
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

            if( currentChar < text.length && text[currentChar] == '\n') {
                fileLine++
            }
        }
        throw NoNextTokenException
    }


    private fun convertToToken(result: LexicalState.FinalState): Token{
        return when(result){
            is LexicalState.GenericFinalState -> GenericToken(result.code)
            is LexicalState.IdFinalState -> tableManager.getIdToken(result.name)
            is LexicalState.IntFinalState -> IntegerToken(result.value)
            is LexicalState.StringFinalState -> StringToken(result.value)
        }
    }
}
