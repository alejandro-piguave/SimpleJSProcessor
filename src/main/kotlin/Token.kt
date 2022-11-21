sealed class Token (val code: TokenCode, private val attribute: String){
    override fun toString(): String = "<${code.code}, $attribute>"
}

class IdentifierToken(val name: String): Token(TokenCode.IDENTIFIER, "23")
class StringToken(val value: String): Token(TokenCode.STRING, "\"$value\"")
class IntegerToken(val value: Int): Token(TokenCode.INTEGER, "$value")

class GenericToken(id: TokenCode): Token(id, "")


enum class TokenCode(val code: Int){
    IDENTIFIER(100),
    STRING(101),
    INTEGER(102),
    //Reserved words
    WHILE( 201),
    PRINT(  205),
    INPUT (206),
    NULL( 210),
    FUNCTION(211),
    LET(212),
    TRUE(213),
    FALSE(214),
    RETURN(215),
    STRING_KEYWORD(216),
    INTEGER_KEYWORD( 217),
    BOOLEAN_KEYWORD(218),
    //Symbols
    ASSIGNMENT_EQUAL(304),
    SEMICOLON(305),
    LEFT_PARENTHESIS(306),
    RIGHT_PARENTHESIS(307),
    LEFT_BRACKET(308),
    RIGHT_BRACKET(309),
    COMMA(310),
    //Operators
    PLUS(400),
    MINUS(401),
    TIMES(402),
    DIVISION(403),
    MODULUS(404),
    PLUS_EQUAL(405),
    MINUS_EQUAL(406),
    TIMES_EQUAL(407),
    DIVIDE_EQUAL(408),
    MODULUS_EQUAL(409),
    GREATER_THAN_OR_EQUAL(410),
    LESS_THAN_OR_EQUAL(411),
    COMPARISON_EQUAL(412),
    GREATER_THAN(413),
    LESS_THAN(414),
    NOT(415),
    NOT_EQUAL(416),
    LOGICAL_AND(417),
    LOGICAL_OR(418),
    OR_EQUAL(419),
    //EOF
    EOF(500)
}