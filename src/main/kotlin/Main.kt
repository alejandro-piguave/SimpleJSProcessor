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

    val parseContent = buildString {
        append("Descendente ")
        syntaxAnalyzer.parse.forEach {
            append("$it ")
        }
    }

    File("src/main/resources/parse.txt").writeText(parseContent)
}

fun writeError(message: String?){
    message?.let {
        File("src/main/resources/errors.txt").writeText(it)
    }
}




