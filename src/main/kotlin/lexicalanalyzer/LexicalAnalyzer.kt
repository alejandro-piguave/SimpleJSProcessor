package lexicalanalyzer

import SymbolsTableManager
import Token
import statemachines.lexicalstatemachine.LexicalStateMachine
import statemachines.lexicalstatemachine.LexicalStateMachineException
import java.io.File

class LexicalAnalyzer(private val tableManager: SymbolsTableManager) {
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

    /*fun analyze(){
        val tokens: MutableList<Token> = mutableListOf()
        var i = 0
        var lineCount = 1
        while(i < text.length){
            val char = text[i]
            when(val result = stateMachine.executeTransition(char)){
                is StateError -> {
                    errors.add(ErrorReport(result.error, lineCount))
                    stateMachine.reset()
                    i++
                }
                is FinalState -> {
                    tableManager.updateTable(result.token)
                    tokens.add(result.token)
                    if (result.nextChar) i++
                    stateMachine.reset()
                }
                else -> i++
            }

            if( i < text.length && text[i] == '\n') {
                lineCount++
            }
        }

        if(errors.isNotEmpty()){
            val errorsFileContent = buildString {
                errors.forEach { error ->
                    append("Error with code ${error.parsingError.code} in line ${error.line}: ${error.parsingError.message} \n")
                }
            }
            File("src/main/resources/errors.txt").writeText(errorsFileContent)
        } else {
            val tokensFileContent = buildString {
                tokens.forEach { token ->
                    append("$token\n")
                }
            }
            File("src/main/resources/tokens.txt").writeText(tokensFileContent)

            tableManager.saveSymbolsTable()
        }
    }*/

    fun getNextToken(): Token {
        while(currentChar < text.length){
            val char = text[currentChar]
            try {
                val result = stateMachine.executeTransition(char)
                if(result is LexicalStateMachine.State.FinalState){
                    tableManager.updateTable(result.token)
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
