package syntax

import lexicalanalyzer.LexicalAnalyzer
import SymbolsTableManager
import Token
import TokenCode
import java.io.File

class SyntaxAnalyzer {
    private lateinit var nextTokenCode: TokenCode
    private val symbolsTableManager = SymbolsTableManager()
    private val tokens: MutableList<Token> = mutableListOf()
    private val lexicalAnalyzer = LexicalAnalyzer(symbolsTableManager, tokens)
    private val parse: MutableList<Int> = mutableListOf()

    fun saveParse(){
        val parseContent = buildString {
            append("Descendente ")
            parse.forEach {
                append("$it ")
            }
        }

        File("src/main/resources/parse.txt").writeText(parseContent)
    }

    fun saveTokens(){
        val tokensFileContent = buildString {
            tokens.forEach { token ->
                append("$token\n")
            }
        }
        File("src/main/resources/tokens.txt").writeText(tokensFileContent)
    }

    fun saveSymbols(){
        symbolsTableManager.save()
    }

    fun analyze() {
        generateNextToken()
        P() //Axiom
        if (nextTokenCode != TokenCode.EOF) throw EOFException
    }

    private fun generateNextToken() {
        val token = lexicalAnalyzer.getNextToken()
        nextTokenCode = token.code
    }

    private fun compare(tokenCode: TokenCode) {
        if (nextTokenCode == tokenCode)
            generateNextToken()
        else throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, tokenCode)
    }

    //Productions

    private fun P() {
        when (nextTokenCode) {
            in FIRST_B -> {
                parse.add(1)
                B()
                P()
            }
            in FIRST_F -> {
                parse.add(2)
                F()
                P()
            }
            in FOLLOW_P -> {
                parse.add(3)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_B + FIRST_F)
        }
    }

    private fun B() {
        when (nextTokenCode) {
            TokenCode.LET -> {
                parse.add(4)
                compare(TokenCode.LET)
                compare(TokenCode.IDENTIFIER)
                T()
                compare(TokenCode.SEMICOLON)
            }
            in FIRST_G -> {
                parse.add(5)
                G()
            }
            TokenCode.WHILE -> {
                parse.add(6)
                compare(TokenCode.WHILE)
                compare(TokenCode.LEFT_PARENTHESIS)
                E()
                compare(TokenCode.RIGHT_PARENTHESIS)
                compare(TokenCode.LEFT_BRACKET)
                C()
                compare(TokenCode.RIGHT_BRACKET)
            }
            TokenCode.IF -> {
                parse.add(7)
                compare(TokenCode.IF)
                compare(TokenCode.LEFT_PARENTHESIS)
                E()
                compare(TokenCode.RIGHT_PARENTHESIS)
                G()
                compare(TokenCode.SEMICOLON)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_S + TokenCode.WHILE)
        }
    }

    private fun T() {
        when (nextTokenCode) {
            TokenCode.INTEGER_KEYWORD -> {
                parse.add(8)
                compare(TokenCode.INTEGER_KEYWORD)
            }
            TokenCode.BOOLEAN_KEYWORD -> {
                parse.add(9)
                compare(TokenCode.BOOLEAN_KEYWORD)
            }
            TokenCode.STRING_KEYWORD -> {
                parse.add(10)
                compare(TokenCode.STRING_KEYWORD)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, listOf(TokenCode.INTEGER_KEYWORD, TokenCode.BOOLEAN_KEYWORD,TokenCode.STRING_KEYWORD))
        }
    }

    private fun S() {
        when (nextTokenCode) {
            TokenCode.IDENTIFIER -> {
                parse.add(11)
                compare(TokenCode.IDENTIFIER)
                Z()
                compare(TokenCode.SEMICOLON)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, listOf(TokenCode.IDENTIFIER, TokenCode.PRINT,TokenCode.INPUT,TokenCode.RETURN))
        }
    }

    private fun SI(){
        when (nextTokenCode) {
            TokenCode.PRINT -> {
                parse.add(12)
                compare(TokenCode.PRINT)
                E()
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.INPUT -> {
                parse.add(13)
                compare(TokenCode.INPUT)
                compare(TokenCode.IDENTIFIER)
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.RETURN -> {
                parse.add(14)
                compare(TokenCode.RETURN)
                X()
                compare(TokenCode.SEMICOLON)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, listOf(TokenCode.IDENTIFIER, TokenCode.PRINT,TokenCode.INPUT,TokenCode.RETURN))
        }
    }
    private fun G(){
        when (nextTokenCode) {
            in FIRST_S -> {
                parse.add(15)
                S()
            }
            in FIRST_SI -> {
                parse.add(16)
                SI()
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, listOf(TokenCode.IDENTIFIER, TokenCode.PRINT,TokenCode.INPUT,TokenCode.RETURN))
        }
    }

    private fun X() {
        parse.add(17)
        E()
    }

    private fun Z() {
        when (nextTokenCode) {
            TokenCode.ASSIGNMENT_EQUAL -> {
                parse.add(18)
                compare(TokenCode.ASSIGNMENT_EQUAL)
                E()
            }
            TokenCode.LEFT_PARENTHESIS -> {
                parse.add(19)
                compare(TokenCode.LEFT_PARENTHESIS)
                L()
                compare(TokenCode.RIGHT_PARENTHESIS)
            }
            TokenCode.OR_EQUAL -> {
                parse.add(20)
                compare(TokenCode.OR_EQUAL)
                E()
            }
            in FOLLOW_Z -> {
                parse.add(21)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, listOf(TokenCode.ASSIGNMENT_EQUAL, TokenCode.LEFT_PARENTHESIS, TokenCode.OR_EQUAL))
        }
    }

    private fun L() {
        parse.add(22)
        E()
        Q()
    }

    private fun Q() {
        when (nextTokenCode) {
            TokenCode.COMMA -> {
                parse.add(23)
                compare(TokenCode.COMMA)
                E()
                Q()
            }
            in FOLLOW_Q -> {
                parse.add(24)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FOLLOW_Q + TokenCode.COMMA)
        }
    }

    private fun E() {
        when (nextTokenCode) {
            in FIRST_R -> {
                parse.add(25)
                R()
                EI()
            }
            in FOLLOW_E -> {
                parse.add(26)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_R + FOLLOW_E)
        }
    }

    private fun EI() {
        when (nextTokenCode) {
            TokenCode.LOGICAL_AND -> {
                parse.add(27)
                compare(TokenCode.LOGICAL_AND)
                R()
                EI()
            }
            in FOLLOW_EI -> {
                parse.add(28)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, TokenCode.LOGICAL_AND)
        }
    }

    private fun R() {
        parse.add(29)
        U()
        RI()
    }

    private fun RI() {
        when (nextTokenCode) {
            TokenCode.COMPARISON_EQUAL -> {
                parse.add(30)
                compare(TokenCode.COMPARISON_EQUAL)
                U()
                RI()
            }
            in FOLLOW_RI -> {
                parse.add(31)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FOLLOW_RI + TokenCode.COMPARISON_EQUAL)
        }
    }

    private fun U() {
        parse.add(32)
        M()
        UI()
    }

    private fun UI() {
        when (nextTokenCode) {
            TokenCode.PLUS -> {
                parse.add(33)
                compare(TokenCode.PLUS)
                M()
                UI()
            }
            in FOLLOW_UI -> {
                parse.add(34)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FOLLOW_UI + TokenCode.PLUS)
        }
    }

    private fun M() {
        parse.add(35)
        V()
        MI()
    }

    private fun MI() {
        when (nextTokenCode) {
            TokenCode.MINUS -> {
                parse.add(36)
                compare(TokenCode.MINUS)
                V()
                MI()
            }
            in FOLLOW_MI -> {
                parse.add(37)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FOLLOW_MI + TokenCode.MINUS)
        }
    }

    private fun V() {
        when (nextTokenCode) {
            TokenCode.LEFT_PARENTHESIS -> {
                parse.add(38)
                compare(TokenCode.LEFT_PARENTHESIS)
                E()
                compare(TokenCode.RIGHT_PARENTHESIS)
            }
            TokenCode.INTEGER -> {
                parse.add(39)
                compare(TokenCode.INTEGER)
            }
            TokenCode.STRING -> {
                parse.add(40)
                compare(TokenCode.STRING)
            }
            in FIRST_S -> {
                parse.add(41)
                S()
            }
            TokenCode.TRUE -> {
                parse.add(42)
                compare(TokenCode.TRUE)
            }
            TokenCode.FALSE -> {
                parse.add(42)
                compare(TokenCode.FALSE)
            }

            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_S + TokenCode.LEFT_PARENTHESIS + TokenCode.INTEGER + TokenCode.STRING + TokenCode.TRUE + TokenCode.FALSE)
        }
    }

    private fun F() {
        parse.add(43)
        compare(TokenCode.FUNCTION)
        compare(TokenCode.IDENTIFIER)
        H()
        compare(TokenCode.LEFT_PARENTHESIS)
        A()
        compare(TokenCode.RIGHT_PARENTHESIS)
        compare(TokenCode.LEFT_BRACKET)
        D()
        compare(TokenCode.RIGHT_BRACKET)
    }

    private fun H() {
        when (nextTokenCode) {
            in FIRST_T -> {
                parse.add(44)
                T()
            }
            in FOLLOW_H -> {
                parse.add(45)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_T + FOLLOW_H)
        }
    }

    private fun A() {
        when (nextTokenCode) {
            in FIRST_T -> {
                parse.add(46)
                T()
                compare(TokenCode.IDENTIFIER)
                AI()
            }
            in FOLLOW_A -> {
                parse.add(47)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_T + FOLLOW_A)
        }
    }

    private fun AI() {
        when (nextTokenCode) {
            TokenCode.COMMA -> {
                parse.add(48)
                compare(TokenCode.COMMA)
                T()
                compare(TokenCode.IDENTIFIER)
                AI()
            }
            in FOLLOW_AI -> {
                parse.add(49)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FOLLOW_AI + TokenCode.COMMA )
        }
    }

    private fun D() {
        when (nextTokenCode) {
            in FIRST_B -> {
                parse.add(50)
                B()
                D()
            }
            in FOLLOW_D -> {
                parse.add(51)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextTokenCode, FIRST_B + FOLLOW_D)
        }
    }

    private fun C() {
        parse.add(52)
        B()
        C()
    }

    companion object {
        //FIRST
        val FIRST_B = listOf(
            TokenCode.LET,
            TokenCode.IDENTIFIER,
            TokenCode.WHILE,
            TokenCode.IF,
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN
        )
        val FIRST_F = listOf(TokenCode.FUNCTION)
        val FIRST_S = listOf(TokenCode.IDENTIFIER,
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN)
        val FIRST_R = listOf(
            TokenCode.IDENTIFIER,
            TokenCode.LEFT_BRACKET,
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN,
            TokenCode.INTEGER,
            TokenCode.STRING,
            TokenCode.TRUE,
            TokenCode.FALSE,
        )
        val FIRST_T = listOf(
            TokenCode.INTEGER_KEYWORD,
            TokenCode.STRING_KEYWORD,
            TokenCode.BOOLEAN_KEYWORD
        )
        val FIRST_G = listOf(
            TokenCode.IDENTIFIER,
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN
        )
        val FIRST_SI = listOf(
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN
        )

        //FOLLOW
        val FOLLOW_P = listOf(TokenCode.EOF)
        val FOLLOW_D = listOf(TokenCode.RIGHT_BRACKET)
        val FOLLOW_Z = listOf(TokenCode.SEMICOLON)
        val FOLLOW_E = listOf(
            TokenCode.SEMICOLON,
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.COMMA)
        val FOLLOW_EI = listOf(
            TokenCode.SEMICOLON,
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.COMMA)
        val FOLLOW_A = listOf(TokenCode.RIGHT_PARENTHESIS)
        val FOLLOW_AI = listOf(TokenCode.RIGHT_PARENTHESIS)
        val FOLLOW_Q = listOf(TokenCode.RIGHT_PARENTHESIS)
        val FOLLOW_H = listOf(TokenCode.LEFT_PARENTHESIS)
        val FOLLOW_RI = listOf(
            TokenCode.SEMICOLON,
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.COMMA,
            TokenCode.LOGICAL_AND
        )
        val FOLLOW_UI = listOf(
            TokenCode.SEMICOLON,
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.COMMA,
            TokenCode.LOGICAL_AND,
            TokenCode.COMPARISON_EQUAL
        )
        val FOLLOW_MI = listOf(
            TokenCode.SEMICOLON,
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.COMMA,
            TokenCode.LOGICAL_AND,
            TokenCode.COMPARISON_EQUAL,
            TokenCode.PLUS
        )

    }
}

