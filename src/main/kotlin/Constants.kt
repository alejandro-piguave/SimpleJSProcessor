val del: List<Char> = listOf(' ', '\n', '\t')
val singleCharSymbols: List<Char> = listOf(';', '(', ')', '{', '}', ',')
val operators: List<Char> = listOf('+', '-', '*',  '=', '>', '<', '!', '%')
val keywords: List<String> = listOf(
    "for",
    "while",
    "if",
    "else",
    "print",
    "input",
    "null",
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
    "for" to TokenCode.FOR,
    "while" to TokenCode.WHILE,
    "if" to TokenCode.IF,
    "else" to TokenCode.ELSE,
    "print" to TokenCode.PRINT,
    "input" to TokenCode.INPUT,
    "null" to TokenCode.NULL,
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
    "*" to TokenCode.TIMES,
    "/" to TokenCode.DIVISION,
    "%" to TokenCode.MODULUS,
    "+=" to TokenCode.PLUS_EQUAL,
    "-=" to TokenCode.MINUS_EQUAL,
    "*=" to TokenCode.TIMES_EQUAL,
    "/=" to TokenCode.DIVIDE_EQUAL,
    "%=" to TokenCode.MODULUS_EQUAL,
    ">=" to TokenCode.GREATER_THAN_OR_EQUAL,
    "<=" to TokenCode.LESS_THAN_OR_EQUAL,
    "==" to TokenCode.COMPARISON_EQUAL,
    ">" to TokenCode.GREATER_THAN,
    "<" to TokenCode.LESS_THAN,
    "!" to TokenCode.NOT,
    "!=" to TokenCode.NOT_EQUAL,
    "&&" to TokenCode.LOGICAL_AND,
    "||" to TokenCode.LOGICAL_OR,
    "|=" to TokenCode.OR_EQUAL
)