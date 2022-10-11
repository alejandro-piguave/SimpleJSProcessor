import statemachines.FinalState
import statemachines.LexicalStateMachine
import statemachines.StateError
import java.io.File

class LexicalAnalyzer {
    fun analyze(){
        val file = File("src/main/resources/source.txt")

        val text = buildString {
            file.forEachLine { line ->
                append(line+"\n")
            }
        }

        val stateMachine = LexicalStateMachine()
        val tableManager = SymbolsTableManager()
        val errors: MutableList<ErrorReport> = mutableListOf()
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

            if( i < text.length && text[i] == '\n') lineCount++
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
    }
}
