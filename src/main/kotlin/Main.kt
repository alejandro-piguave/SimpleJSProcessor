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
        return
    }

    syntaxAnalyzer.saveParse()
    syntaxAnalyzer.saveTokens()
    syntaxAnalyzer.saveSymbols()
}

fun writeError(message: String?){
    message?.let {
        File(ERRORS_PATHNAME).writeText(it)
    }
}




