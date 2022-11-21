import lexicalanalyzer.LexicalException
import syntax.SyntaxAnalyzer
import syntax.SyntaxException
import java.io.File

fun main() {
    val syntaxAnalyzer = SyntaxAnalyzer()
    try {
        syntaxAnalyzer.analyze()
    } catch (e: LexicalException){
        writeError(e.message)
        return
    } catch(e: SyntaxException){
        writeError(e.message)
        return
    }

    syntaxAnalyzer.saveParse()
    syntaxAnalyzer.saveTokens()
    syntaxAnalyzer.saveSymbols()
}

fun writeError(message: String?){
    message?.let {
        File("src/main/resources/errors.txt").writeText(it)
    }
}




