package lexicalanalyzer

import SymbolsTableManager
import Token
import statemachines.lexicalstatemachine.LexicalStateMachine
import statemachines.lexicalstatemachine.LexicalStateMachineException
import java.io.File

class LexicalAnalyzer(private val tableManager: SymbolsTableManager,
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
                if(result is LexicalStateMachine.State.FinalState){
                    tableManager.updateTable(result.token)
                    tokens.add(result.token)
                    if (result.nextChar) currentChar++
                    stateMachine.reset()
                    return result.token
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
}
