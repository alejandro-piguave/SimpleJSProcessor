package syntax

import EntryType
import IdentifierToken
import lexical.LexicalAnalyzer
import SymbolsTable
import Token
import TokenCode
import semantic.*
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
        //println("Current token = $nextToken")
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
            TokenCode.IF -> {
                parse.add(4)
                compare(TokenCode.IF)
                compare(TokenCode.LEFT_PARENTHESIS)
                val EType = E()
                if(EType != EntryType.BOOLEAN) {
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, EntryType.BOOLEAN)
                }
                compare(TokenCode.RIGHT_PARENTHESIS)
                S()
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.LET -> {
                parse.add(5)
                compare(TokenCode.LET)
                val tablePosition = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken).tablePosition
                }
                val type = T()
                symbolsTable.addEntryType(tablePosition,type)
                compare(TokenCode.SEMICOLON)
            }
            in FIRST_S -> {
                parse.add(6)
                S()
            }
            TokenCode.WHILE -> {
                parse.add(7)
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

            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_S + TokenCode.WHILE)
        }
    }

    private fun S() {
        when (nextToken.code) {
            TokenCode.IDENTIFIER -> {
                parse.add(8)
                val idToken = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken)
                }
                val entryType = symbolsTable.getEntryType(idToken) ?: throw UnexpectedIdentifierException(lexicalAnalyzer.fileLine, idToken.name)
                I(idToken, entryType)
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.PRINT -> {
                parse.add(9)
                compare(TokenCode.PRINT)
                val EType = E()
                if(EType != EntryType.INTEGER && EType != EntryType.STRING){
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, listOf(EntryType.INTEGER, EntryType.STRING))
                }
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.INPUT -> {
                parse.add(10)
                compare(TokenCode.INPUT)
                val idToken = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken)
                }
                val entryType = symbolsTable.getEntryType(idToken) ?: throw UnexpectedIdentifierException(lexicalAnalyzer.fileLine, idToken.name)
                if(entryType != EntryType.INTEGER && entryType != EntryType.STRING){
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, listOf(EntryType.INTEGER, EntryType.STRING))
                }
                compare(TokenCode.SEMICOLON)
            }
            TokenCode.RETURN -> {
                parse.add(11)
                if(symbolsTable.isCurrentTableGlobal){
                    throw UnexpectedReturnUseException(lexicalAnalyzer.fileLine)
                }
                compare(TokenCode.RETURN)
                val XType = X()

                compare(TokenCode.SEMICOLON)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, TokenCode.IDENTIFIER)
        }
    }

    private fun I(identifierToken: IdentifierToken, entryType: EntryType) {
        when (nextToken.code) {
            TokenCode.ASSIGNMENT_EQUAL -> {
                parse.add(12)
                compare(TokenCode.ASSIGNMENT_EQUAL)
                val EType = E()
                if(entryType != EType)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, entryType)
            }
            TokenCode.OR_EQUAL -> {
                parse.add(13)
                if(entryType != EntryType.BOOLEAN)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, EntryType.BOOLEAN)

                compare(TokenCode.OR_EQUAL)
                val EType = E()
                if(EType != EntryType.BOOLEAN)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, EntryType.BOOLEAN)
            }
            TokenCode.LEFT_PARENTHESIS -> {
                parse.add(14)
                if(entryType != EntryType.FUNCTION)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, EntryType.FUNCTION)
                compare(TokenCode.LEFT_PARENTHESIS)
                L(identifierToken)
                compare(TokenCode.RIGHT_PARENTHESIS)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.ASSIGNMENT_EQUAL, TokenCode.LEFT_PARENTHESIS, TokenCode.OR_EQUAL))
        }
    }

    private fun L(identifierToken: IdentifierToken) {
        val parameterList = symbolsTable.getFunctionParameters(identifierToken.tablePosition)
        when(nextToken.code){
            in FIRST_E ->{
                parse.add(15)
                val EType = E()

                if(parameterList.isEmpty() && EType != EntryType.VOID){
                    throw TooManyArgumentsException(identifierToken.name)
                } else if( parameterList[0] != EType){
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, parameterList[0])
                }
                Q(identifierToken, parameterList, 1)
            }
            in FOLLOW_L -> {
                parse.add(16)
                if(parameterList.isNotEmpty()){
                    throw MissingParametersException(identifierToken.name)
                }
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_E + FOLLOW_L)
        }
    }

    private fun Q(identifierToken: IdentifierToken, parameterList: List<EntryType>, index: Int) {
        when (nextToken.code) {
            TokenCode.COMMA -> {
                parse.add(17)
                compare(TokenCode.COMMA)
                val EType = E()

                if(index >= parameterList.size && EType != EntryType.VOID ){
                    throw TooManyArgumentsException(identifierToken.name)
                } else if(parameterList[index] != EType){

                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, EType, parameterList[index])
                }
                Q(identifierToken, parameterList, index + 1)
            }
            in FOLLOW_Q -> {
                parse.add(18)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_Q + TokenCode.COMMA)
        }
    }


    private fun X(): EntryType {
        return when (nextToken.code) {
            in FIRST_E -> {
                parse.add(19)
                E()
            }
            in FOLLOW_X -> {
                parse.add(20)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_Q + TokenCode.COMMA)
        }
    }


    private fun E(): EntryType {
        parse.add(21)
        val RType = R()
        val RIType = RI()
        if(RType == RIType || RIType == EntryType.VOID){
            return RType
        } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, RIType, RType)
    }

    private fun RI(): EntryType {
        return when (nextToken.code) {
            TokenCode.LOGICAL_AND -> {
                parse.add(22)
                compare(TokenCode.LOGICAL_AND)
                val RType = R()
                val RIType = RI()

                if(RType == EntryType.BOOLEAN && (RIType == EntryType.BOOLEAN || RIType == EntryType.VOID)){
                    EntryType.BOOLEAN
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, RIType, EntryType.BOOLEAN )
            }
            in FOLLOW_RI -> {
                parse.add(23)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, TokenCode.LOGICAL_AND)
        }
    }

    private fun R(): EntryType {
        parse.add(24)
        val UType = U()
        val UIType = UI()

        if(UType == UIType || UIType == EntryType.VOID){
            return UType
        } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, UIType, UType)
    }

    private fun UI(): EntryType {
        return when (nextToken.code) {
            TokenCode.COMPARISON_EQUAL -> {
                parse.add(25)
                compare(TokenCode.COMPARISON_EQUAL)
                val UType = U()
                val UIType = UI()

                if(UType == EntryType.BOOLEAN && (UIType == EntryType.BOOLEAN || UIType == EntryType.VOID)){
                    EntryType.BOOLEAN
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, UIType, EntryType.BOOLEAN )
            }
            in FOLLOW_UI -> {
                parse.add(26)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_RI + TokenCode.COMPARISON_EQUAL)
        }
    }

    private fun U(): EntryType {
        parse.add(27)
        val VType = V()
        val VIType = VI()

        if(VType == VIType || VIType == EntryType.VOID){
            return VType
        } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, VIType, VType)
    }

    private fun VI(): EntryType{
        return when (nextToken.code) {
            TokenCode.PLUS -> {
                parse.add(28)
                compare(TokenCode.PLUS)
                val VType = V()
                val VIType = VI()

                if(VType == EntryType.INTEGER && (VIType == EntryType.INTEGER || VIType == EntryType.VOID)){
                    EntryType.INTEGER
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, VIType, EntryType.INTEGER )
            }
            TokenCode.MINUS -> {
                parse.add(29)
                compare(TokenCode.MINUS)
                val VType = V()
                val VIType = VI()

                if(VType == EntryType.INTEGER && (VIType == EntryType.INTEGER || VIType == EntryType.VOID)){
                    EntryType.INTEGER
                } else throw UnexpectedTypeException(lexicalAnalyzer.fileLine, VIType, EntryType.INTEGER )
            }
            in FOLLOW_VI -> {
                parse.add(30)
                EntryType.VOID
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_UI + TokenCode.PLUS)
        }
    }

    private fun V(): EntryType {
        return when (nextToken.code) {
            TokenCode.IDENTIFIER -> {
                parse.add(31)
                val idToken = compare(TokenCode.IDENTIFIER){
                    (nextToken as IdentifierToken)
                }
                val entryType = symbolsTable.getEntryType(idToken) ?: throw UnexpectedIdentifierException(lexicalAnalyzer.fileLine, idToken.name)
                W(idToken, entryType)
                entryType
            }
            TokenCode.LEFT_PARENTHESIS -> {
                parse.add(32)
                compare(TokenCode.LEFT_PARENTHESIS)
                val type = E()
                compare(TokenCode.RIGHT_PARENTHESIS)
                type
            }
            TokenCode.INTEGER -> {
                parse.add(33)
                compare(TokenCode.INTEGER)
                EntryType.INTEGER
            }
            TokenCode.STRING -> {
                parse.add(34)
                compare(TokenCode.STRING)
                EntryType.STRING
            }
            TokenCode.TRUE -> {
                parse.add(35)
                compare(TokenCode.TRUE)
                EntryType.BOOLEAN
            }
            TokenCode.FALSE -> {
                parse.add(35)
                compare(TokenCode.FALSE)
                EntryType.BOOLEAN
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_S + TokenCode.LEFT_PARENTHESIS + TokenCode.INTEGER + TokenCode.STRING + TokenCode.TRUE + TokenCode.FALSE)
        }
    }

    private fun W(identifierToken: IdentifierToken, entryType: EntryType) {
        when (nextToken.code) {
            TokenCode.LEFT_PARENTHESIS -> {
                parse.add(36)
                if(entryType != EntryType.FUNCTION)
                    throw UnexpectedTypeException(lexicalAnalyzer.fileLine, entryType, EntryType.FUNCTION)
                compare(TokenCode.LEFT_PARENTHESIS)
                L(identifierToken)
                compare(TokenCode.RIGHT_PARENTHESIS)
            }
            in FOLLOW_W ->{
                parse.add(37)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_S + TokenCode.LEFT_PARENTHESIS + TokenCode.INTEGER + TokenCode.STRING + TokenCode.TRUE + TokenCode.FALSE)
        }
    }

    private fun T(): EntryType {
        return when (nextToken.code) {
            TokenCode.INTEGER_KEYWORD -> {
                parse.add(38)
                compare(TokenCode.INTEGER_KEYWORD)
                EntryType.INTEGER
            }
            TokenCode.BOOLEAN_KEYWORD -> {
                parse.add(39)
                compare(TokenCode.BOOLEAN_KEYWORD)
                EntryType.BOOLEAN
            }
            TokenCode.STRING_KEYWORD -> {
                parse.add(40)
                compare(TokenCode.STRING_KEYWORD)
                EntryType.STRING
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, listOf(TokenCode.INTEGER_KEYWORD, TokenCode.BOOLEAN_KEYWORD,TokenCode.STRING_KEYWORD))
        }
    }

    private fun F() {
        parse.add(41)
        if (!symbolsTable.isCurrentTableGlobal){
            throw UnexpectedFunctionDeclarationException(lexicalAnalyzer.fileLine)
        }

        compare(TokenCode.FUNCTION)
        val idToken = compare(TokenCode.IDENTIFIER){
            (nextToken as IdentifierToken)
        }
        symbolsTable.addEntryType(idToken.tablePosition, EntryType.FUNCTION) // Makes entry of type 'function'
        symbolsTable.addFunctionTag(idToken.tablePosition, idToken.name)
        val returnType = H()
        symbolsTable.addReturnType(idToken.tablePosition, returnType) // Sets entry's 'return type' field
        compare(TokenCode.LEFT_PARENTHESIS)
        symbolsTable.createLocalTable()
        A(idToken.tablePosition) // Adds parameters
        compare(TokenCode.RIGHT_PARENTHESIS)
        compare(TokenCode.LEFT_BRACKET)
        C()
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
                K(parentTablePosition)
            }
            in FOLLOW_A -> {
                parse.add(45)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_T + FOLLOW_A)
        }
    }

    private fun K(parentTablePosition: Int) {
        when (nextToken.code) {
            TokenCode.COMMA -> {
                parse.add(46)
                compare(TokenCode.COMMA)
                val entryType = T()
                symbolsTable.addFunctionParameter(parentTablePosition, entryType)
                val tablePosition = compare(TokenCode.IDENTIFIER){ (nextToken as IdentifierToken).tablePosition}
                symbolsTable.addEntryType(tablePosition, entryType)
                K(parentTablePosition)
            }
            in FOLLOW_K -> {
                parse.add(47)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FOLLOW_K + TokenCode.COMMA )
        }
    }

    private fun C() {
        when(nextToken.code){
            in FIRST_B -> {
                parse.add(48)
                B()
                C()
            }
            in FOLLOW_C -> {
                parse.add(49)
            }
            else -> throw UnexpectedTokenException(lexicalAnalyzer.fileLine, nextToken.code, FIRST_B + FOLLOW_C)
        }
    }

    companion object {
        //FIRST
        val FIRST_B = listOf(
            TokenCode.IF,
            TokenCode.LET,
            TokenCode.IDENTIFIER,
            TokenCode.WHILE,
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN
        )
        val FIRST_F = listOf(TokenCode.FUNCTION)
        val FIRST_T = listOf(
            TokenCode.INTEGER_KEYWORD,
            TokenCode.STRING_KEYWORD,
            TokenCode.BOOLEAN_KEYWORD
        )
        val FIRST_S = listOf(
            TokenCode.IDENTIFIER,
            TokenCode.PRINT,
            TokenCode.INPUT,
            TokenCode.RETURN
        )
        val FIRST_E = listOf(
            TokenCode.LEFT_PARENTHESIS,
            TokenCode.IDENTIFIER,
            TokenCode.INTEGER,
            TokenCode.STRING,
            TokenCode.TRUE,
            TokenCode.FALSE
        )

        //FOLLOW
        val FOLLOW_P = listOf(TokenCode.EOF)
        val FOLLOW_C = listOf(TokenCode.RIGHT_BRACKET)
        val FOLLOW_E = listOf(
            TokenCode.SEMICOLON,
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.COMMA)

        val FOLLOW_A = listOf(TokenCode.RIGHT_PARENTHESIS)
        val FOLLOW_Q = listOf(TokenCode.RIGHT_PARENTHESIS)
        val FOLLOW_H = listOf(TokenCode.LEFT_PARENTHESIS)
        val FOLLOW_X = listOf(TokenCode.SEMICOLON)
        val FOLLOW_RI = listOf(
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.SEMICOLON,
            TokenCode.COMMA)
        val FOLLOW_UI = listOf(
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.SEMICOLON,
            TokenCode.COMMA,
            TokenCode.LOGICAL_AND)
        val FOLLOW_VI = listOf(
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.SEMICOLON,
            TokenCode.COMMA,
            TokenCode.LOGICAL_AND,
            TokenCode.COMPARISON_EQUAL
        )
        val FOLLOW_W = listOf(
            TokenCode.RIGHT_PARENTHESIS,
            TokenCode.SEMICOLON,
            TokenCode.COMMA,
            TokenCode.LOGICAL_AND,
            TokenCode.COMPARISON_EQUAL,
            TokenCode.PLUS,
            TokenCode.MINUS
        )
        val FOLLOW_K = listOf(TokenCode.RIGHT_PARENTHESIS)
        val FOLLOW_L = listOf(TokenCode.RIGHT_PARENTHESIS)
    }
}

