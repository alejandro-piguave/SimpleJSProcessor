package lexical.statemachine

import TokenCode

//Constants

val del: List<Char> = listOf(' ', '\n', '\t')
val singleCharSymbols: List<Char> = listOf(';', '(', ')', '{', '}', ',', '$')
val operators: List<Char> = listOf('+', '-', '*',  '=', '>', '<', '!')
val keywords: List<String> = listOf(
    "while",
    "if",
    "print",
    "input",
    "function",
    "let",
    "true",
    "false",
    "return",
    "string",
    "int",
    "boolean"
)

val codesMap: Map<String, TokenCode> = mapOf(
    //Reserved words
    "while" to TokenCode.WHILE,
    "print" to TokenCode.PRINT,
    "if" to TokenCode.IF,
    "input" to TokenCode.INPUT,
    "function" to TokenCode.FUNCTION,
    "let" to TokenCode.LET,
    "true" to TokenCode.TRUE,
    "false" to TokenCode.FALSE,
    "return" to TokenCode.RETURN,
    "string" to TokenCode.STRING_KEYWORD,
    "int" to TokenCode.INTEGER_KEYWORD,
    "boolean" to TokenCode.BOOLEAN_KEYWORD,
    //Symbols
    "=" to TokenCode.ASSIGNMENT_EQUAL,
    ";" to TokenCode.SEMICOLON,
    "(" to TokenCode.LEFT_PARENTHESIS,
    ")" to TokenCode.RIGHT_PARENTHESIS,
    "{" to TokenCode.LEFT_BRACKET,
    "}" to TokenCode.RIGHT_BRACKET,
    "," to TokenCode.COMMA,
    //Operators
    "+" to TokenCode.PLUS,
    "-" to TokenCode.MINUS,
    "==" to TokenCode.COMPARISON_EQUAL,
    "&&" to TokenCode.LOGICAL_AND,
    "|=" to TokenCode.OR_EQUAL,
    //EOF
    "$" to TokenCode.EOF
)


