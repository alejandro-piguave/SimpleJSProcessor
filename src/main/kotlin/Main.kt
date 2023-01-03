import lexical.LexicalException
import semantic.SemanticException
import syntax.SyntaxAnalyzer
import syntax.SyntaxException
import java.io.File

fun main() {
    val syntaxAnalyzer = SyntaxAnalyzer()
    try {
        syntaxAnalyzer.analyze()
    } catch (e: LexicalException){
        writeError("LexicalException. "+ e.message)
        return
    } catch(e: SyntaxException){
        writeError("SyntaxException. "+ e.message)
        return
    } catch (e: SemanticException){
        writeError("SemanticException. "+ e.message)
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




