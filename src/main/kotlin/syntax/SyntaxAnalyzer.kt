package syntax

import EntryType
import IdentifierToken
import lexical.LexicalAnalyzer
import SymbolsTable
import Token
import TokenCode
import semantic.UnexpectedFunctionDeclarationException
import semantic.UnexpectedIdentifierException
import semantic.UnexpectedReturnUseException
import semantic.UnexpectedTypeException
import java.io.File

class SyntaxAnalyzer {
    private lateinit var nextToken: Token
    private val symbolsTable = SymbolsTable()
    private val tokens: MutableList<Token> = mutableListOf()
    private val lexicalAnalyzer = LexicalAnalyzer(symbolsTable, tokens)
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
        symbolsTable.save()
    }

    fun analyze() {
        generateNextToken()
        P() //Axiom
        if (nextToken.code != TokenCode.EOF) throw EOFException
    }

    private fun generateNextToken() {
        nextToken = lexicalAnalyzer.getNextToken()
    }

    private fun compare(tokenCode: TokenCode) {
        if (nextToken.code == tokenCode){
            generateNextToken()
        }
        else throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, tokenCode)
    }

    private fun <T> compare(tokenCode: TokenCode, predicate: () -> T): T {
        if (nextToken.code == tokenCode){
            val result = predicate()
            generateNextToken()
            return result
        }
        else throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, tokenCode)
    }

    //Productions

    private fun P() {
        when (nextToken.code) {
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
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_B + FIRST_F)
        }
    }

    private fun B() {
        when (nextToken.code) {
            TokenCode.LET -> {
                parse.add(4)
                compare(TokenCode.LET)
                val tablePosition = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken).tablePosition
                }
                val type = T()
                symbolsTable.addEntryType(tablePosition,type)
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
                val EType = E()
                if(EType != EntryType.BOOLEAN) {
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, EntryType.BOOLEAN)
                }
                compare(TokenCode.RIGHT_PARENTHESIS)
                compare(TokenCode.LEFT_BRACKET)
                C()
                compare(TokenCode.RIGHT_BRACKET)
            }
            TokenCode.IF -> {
                parse.add(7)
                compare(TokenCode.IF)
                compare(TokenCode.LEFT_PARENTHESIS)
                val EType = E()
                if(EType != EntryType.BOOLEAN) {
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, EntryType.BOOLEAN)
                }
                compare(TokenCode.RIGHT_PARENTHESIS)
                G()
                compare(TokenCode.SEMICOLON)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_S + TokenCode.WHILE)
        }
    }

    private fun T(): EntryType {
        return when (nextToken.code) {
            TokenCode.INTEGER_KEYWORD -> {
                parse.add(8)
                compare(TokenCode.INTEGER_KEYWORD)
                EntryType.INTEGER
            }
            TokenCode.BOOLEAN_KEYWORD -> {
                parse.add(9)
                compare(TokenCode.BOOLEAN_KEYWORD)
                EntryType.BOOLEAN
            }
            TokenCode.STRING_KEYWORD -> {
                parse.add(10)
                compare(TokenCode.STRING_KEYWORD)
                EntryType.STRING
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.INTEGER_KEYWORD, TokenCode.BOOLEAN_KEYWORD,TokenCode.STRING_KEYWORD))
        }
    }

    private fun S(): EntryType {
        when (nextToken.code) {
            TokenCode.IDENTIFIER -> {
                parse.add(11)
                val idToken = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken)
                }
                val entryType = symbolsTable.getEntryType(idToken.tablePosition) ?: throw UnexpectedIdentifierException(lexicalAnalyzer.fileLine, idToken.name)

                Z(idToken.tablePosition, entryType)
                compare(TokenCode.SEMICOLON)

                return entryType
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.IDENTIFIER, TokenCode.PRINT,TokenCode.INPUT,TokenCode.RETURN))
        }
    }

    private fun SI(){
        when (nextToken.code) {
            TokenCode.PRINT -> {
                parse.add(12)
                compare(TokenCode.PRINT)
                val EType = E()
                if(EType != EntryType.INTEGER && EType != EntryType.STRING){
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, listOf(EntryType.INTEGER, EntryType.STRING))
                }
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.INPUT -> {
                parse.add(13)
                compare(TokenCode.INPUT)
                val idToken = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken)
                }
                val entryType = symbolsTable.getEntryType(idToken.tablePosition) ?: throw UnexpectedIdentifierException(lexicalAnalyzer.fileLine, idToken.name)
                if(entryType != EntryType.INTEGER && entryType != EntryType.STRING){
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, listOf(EntryType.INTEGER, EntryType.STRING))
                }
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.RETURN -> {
                if(symbolsTable.isCurrentTableGlobal){
                    throw UnexpectedReturnUseException(lexicalAnalyzer.fileLine)
                }
                parse.add(14)
                compare(TokenCode.RETURN)
                X()
                compare(TokenCode.SEMICOLON)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.IDENTIFIER, TokenCode.PRINT,TokenCode.INPUT,TokenCode.RETURN))
        }
    }
    private fun G(){
        when (nextToken.code) {
            in FIRST_S -> {
                parse.add(15)
                S()
                compare(TokenCode.SEMICOLON)
            }
            in FIRST_SI -> {
                parse.add(16)
                SI()
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.IDENTIFIER, TokenCode.PRINT,TokenCode.INPUT,TokenCode.RETURN))
        }
    }

    private fun X() {
        parse.add(17)
        E()
    }

    private fun Z(tablePosition: Int, entryType: EntryType) {
        when (nextToken.code) {
            TokenCode.ASSIGNMENT_EQUAL -> {
                parse.add(18)
                compare(TokenCode.ASSIGNMENT_EQUAL)
                val EType = E()

                if(entryType != EType)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, entryType)
            }
            TokenCode.LEFT_PARENTHESIS -> {
                if(entryType != EntryType.FUNCTION)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, EntryType.FUNCTION)
                val parameterList = symbolsTable.getEntryParameters(tablePosition)
                parse.add(19)
                compare(TokenCode.LEFT_PARENTHESIS)
                L(parameterList)
                compare(TokenCode.RIGHT_PARENTHESIS)
            }
            TokenCode.OR_EQUAL -> {
                if(entryType != EntryType.BOOLEAN)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, EntryType.BOOLEAN)
                parse.add(20)
                compare(TokenCode.OR_EQUAL)
                val EType = E()
                if(EType != EntryType.BOOLEAN)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, EntryType.BOOLEAN)
            }
            in FOLLOW_Z -> {
                parse.add(21)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.ASSIGNMENT_EQUAL, TokenCode.LEFT_PARENTHESIS, TokenCode.OR_EQUAL))
        }
    }

    private fun L(parameterList: List<EntryType>) {
        parse.add(22)
        val EType = E()

        if(parameterList.isEmpty() && EType != EntryType.VOID || parameterList[0] != EType){
            throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, parameterList[0])
        }
        Q(parameterList, 1)
    }

    private fun Q(parameterList: List<EntryType>, index: Int) {
        when (nextToken.code) {
            TokenCode.COMMA -> {
                parse.add(23)
                compare(TokenCode.COMMA)
                val EType = E()

                if(index >= parameterList.size && EType != EntryType.VOID || parameterList[index] != EType){
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, parameterList[index])
                }
                Q(parameterList, index + 1)
            }
            in FOLLOW_Q -> {
                parse.add(24)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_Q + TokenCode.COMMA)
        }
    }

    private fun E(): EntryType {
        return when (nextToken.code) {
            in FIRST_R -> {
                parse.add(25)
                val RType = R()
                val EIType = EI()

                if(RType == EIType || EIType == EntryType.VOID){
                    return RType
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EIType, RType)
            }
            in FOLLOW_E -> {
                parse.add(26)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_R + FOLLOW_E)
        }
    }

    private fun EI(): EntryType {
        return when (nextToken.code) {
            TokenCode.LOGICAL_AND -> {
                parse.add(27)
                compare(TokenCode.LOGICAL_AND)
                val RType = R()
                val EIType = EI()

                if(RType == EntryType.BOOLEAN && (EIType == EntryType.BOOLEAN || EIType == EntryType.VOID)){
                    EntryType.BOOLEAN
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EIType, EntryType.BOOLEAN )
            }
            in FOLLOW_EI -> {
                parse.add(28)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, TokenCode.LOGICAL_AND)
        }
    }

    private fun R(): EntryType {
        parse.add(29)
        val UType = U()
        val RIType = RI()

        if(UType == RIType || RIType == EntryType.VOID){
            return UType
        } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, RIType, UType)
    }

    private fun RI(): EntryType {
        return when (nextToken.code) {
            TokenCode.COMPARISON_EQUAL -> {
                parse.add(30)
                compare(TokenCode.COMPARISON_EQUAL)
                val UType = U()
                val RIType = RI()

                if(UType == EntryType.BOOLEAN && (RIType == EntryType.BOOLEAN || RIType == EntryType.VOID)){
                    EntryType.BOOLEAN
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, RIType, EntryType.BOOLEAN )
            }
            in FOLLOW_RI -> {
                parse.add(31)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_RI + TokenCode.COMPARISON_EQUAL)
        }
    }

    private fun U(): EntryType {
        parse.add(32)
        val VType = V()
        val UIType = UI()

        if(VType == UIType || UIType == EntryType.VOID){
            return VType
        } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, UIType, VType)
    }

    private fun UI(): EntryType{
        return when (nextToken.code) {
            TokenCode.PLUS -> {
                parse.add(33)
                compare(TokenCode.PLUS)
                val VType = V()
                val UIType = UI()

                if(VType == EntryType.INTEGER && (UIType == EntryType.INTEGER || UIType == EntryType.VOID)){
                    EntryType.INTEGER
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, UIType, EntryType.INTEGER )
            }
            TokenCode.MINUS -> {
                parse.add(34)
                compare(TokenCode.MINUS)
                val VType = V()
                val UIType = UI()

                if(VType == EntryType.INTEGER && (UIType == EntryType.INTEGER || UIType == EntryType.VOID)){
                    EntryType.INTEGER
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, UIType, EntryType.INTEGER )
            }
            in FOLLOW_UI -> {
                parse.add(35)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_UI + TokenCode.PLUS)
        }
    }

    private fun V(): EntryType {
        return when (nextToken.code) {
            TokenCode.LEFT_PARENTHESIS -> {
                parse.add(36)
                compare(TokenCode.LEFT_PARENTHESIS)
                val type = E()
                compare(TokenCode.RIGHT_PARENTHESIS)
                type
            }
            TokenCode.INTEGER -> {
                parse.add(37)
                compare(TokenCode.INTEGER)
                EntryType.INTEGER
            }
            TokenCode.STRING -> {
                parse.add(38)
                compare(TokenCode.STRING)
                EntryType.STRING
            }
            in FIRST_S -> {
                parse.add(39)
                S()
            }
            TokenCode.TRUE -> {
                parse.add(40)
                compare(TokenCode.TRUE)
                EntryType.BOOLEAN
            }
            TokenCode.FALSE -> {
                parse.add(40)
                compare(TokenCode.FALSE)
                EntryType.BOOLEAN
            }

            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_S + TokenCode.LEFT_PARENTHESIS + TokenCode.INTEGER + TokenCode.STRING + TokenCode.TRUE + TokenCode.FALSE)
        }
    }

    private fun F() {
        if (!symbolsTable.isCurrentTableGlobal){
            throw UnexpectedFunctionDeclarationException(lexicalAnalyzer.fileLine)
        }
        parse.add(41)
        compare(TokenCode.FUNCTION)
        val tablePosition = compare(TokenCode.IDENTIFIER){
            (nextToken as IdentifierToken).tablePosition
        }
        symbolsTable.addEntryType(tablePosition, EntryType.FUNCTION) // Makes entry of type 'function'
        val returnType = H()
        symbolsTable.addReturnType(tablePosition, returnType) // Sets entry's 'return type' field
        compare(TokenCode.LEFT_PARENTHESIS)
        symbolsTable.createLocalTable()
        A(tablePosition) // Adds parameters
        compare(TokenCode.RIGHT_PARENTHESIS)
        compare(TokenCode.LEFT_BRACKET)
        D()
        compare(TokenCode.RIGHT_BRACKET)
        symbolsTable.destroyCurrentLocalTable()
    }

    private fun H(): EntryType {
        return when (nextToken.code) {
            in FIRST_T -> {
                parse.add(42)
                T()
            }
            in FOLLOW_H -> {
                parse.add(43)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_T + FOLLOW_H)
        }
    }

    private fun A(parentTablePosition: Int) {
        when (nextToken.code) {
            in FIRST_T -> {
                parse.add(44)
                val entryType = T()
                symbolsTable.addFunctionParameter(parentTablePosition, entryType)
                val tablePosition = compare(TokenCode.IDENTIFIER){ (nextToken as IdentifierToken).tablePosition}
                symbolsTable.addEntryType(tablePosition, entryType)
                AI(parentTablePosition)
            }
            in FOLLOW_A -> {
                parse.add(45)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_T + FOLLOW_A)
        }
    }

    private fun AI(parentTablePosition: Int) {
        when (nextToken.code) {
            TokenCode.COMMA -> {
                parse.add(46)
                compare(TokenCode.COMMA)
                val entryType = T()
                symbolsTable.addFunctionParameter(parentTablePosition, entryType)
                val tablePosition = compare(TokenCode.IDENTIFIER){ (nextToken as IdentifierToken).tablePosition}
                symbolsTable.addEntryType(tablePosition, entryType)
                AI(parentTablePosition)
            }
            in FOLLOW_AI -> {
                parse.add(47)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_AI + TokenCode.COMMA )
        }
    }

    private fun D() {
        when (nextToken.code) {
            in FIRST_B -> {
                parse.add(48)
                B()
                D()
            }
            in FOLLOW_D -> {
                parse.add(49)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_B + FOLLOW_D)
        }
    }

    private fun C() {
        parse.add(50)
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
    }
}

