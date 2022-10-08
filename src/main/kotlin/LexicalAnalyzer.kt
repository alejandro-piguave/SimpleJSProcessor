import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

class LexicalAnalyzer {
    private val keywordsAndOperatorsMap: Map<String, Token> = mapOf(
        "for" to Token.KEYWORD,
        "while" to Token.KEYWORD,
        "do" to Token.KEYWORD,
        "if" to Token.KEYWORD,
        "else" to  Token.KEYWORD,
        "print" to Token.KEYWORD, 
        "switch" to Token.KEYWORD,
        "case" to Token.KEYWORD,
        "default" to Token.KEYWORD,
        "null" to Token.KEYWORD,
        "function" to Token.KEYWORD,
        "let" to Token.KEYWORD,
        "+" to Token.PLUS,
        "-" to Token.MINUS,
        "*" to Token.TIMES,
        "/" to Token.DIVIDE,
        "'" to Token.QUOTE,
        "." to Token.DOT,
        "," to Token.COMMA,
        "=" to Token.EQUAL,
        ";" to Token.SEMICOLON,
        "(" to Token.LEFT_PARENTHESIS,
        ")" to Token.RIGHT_PARENTHESIS,
        ">=" to Token.GREATER_OR_EQUALS,
        "<=" to Token.LOWER_OR_EQUALS,
        "==" to Token.EQUALS,
        ">" to Token.GREATER_THAN,
        "<" to Token.LOWER_THAN,
        "!=" to Token.NOT_EQUALS,
        "{" to Token.LEFT_BRACE,
        "}" to Token.RIGHT_BRACE,
        "&&" to Token.LOGICAL_AND,
        "||" to Token.LOGICAL_OR
    )

    fun analyze(){
        val file = File("src/main/source.txt")
        val map = mutableMapOf<Int, String>()
        var i = 1
        file.forEachLine { line ->
            if(!line.trim().startsWith("//")){
                map[i] = line
            }
            i++
        }

        analyzeCode(map)
    }

    private fun analyzeCode(lines: Map<Int, String>): List<Lexeme> {
        val lexemes: MutableList<Lexeme> = ArrayList()
        lines.forEach { (nLine: Int, line: String) ->
            val lexLine = analyzeLine(line.trim())
            lexLine.forEach { (value: String, token: Token) -> lexemes.add(Lexeme(token, value, nLine)) }
        }
        return lexemes
    }

    private fun analyzeLine(line: String): Map<String, Token> {
        val lineTokens: MutableMap<String, Token> = HashMap()
        val automaton = Automaton()
        for (str in line.split(" ".toRegex()).toTypedArray()) {
            val lowerCaseStr = str.lowercase(Locale.getDefault())
            lineTokens[str] = keywordsAndOperatorsMap[lowerCaseStr] ?: automaton.evaluate(str)
        }
        return lineTokens
    }
}