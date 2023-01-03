sealed class Token (val code: TokenCode){
    abstract fun getAttribute(): String
    override fun toString(): String = "<${code.code}, ${getAttribute()}>"
}

class IdentifierToken(val name: String, val tablePosition: Int): Token(TokenCode.IDENTIFIER){
    override fun getAttribute(): String = "$tablePosition"
}
class StringToken(val value: String): Token(TokenCode.STRING){
    override fun getAttribute(): String = "\"$value\""
}
class IntegerToken(val value: Int): Token(TokenCode.INTEGER){
    override fun getAttribute(): String = "$value"
}

class GenericToken(id: TokenCode): Token(id){
    override fun getAttribute(): String = ""
}

enum class TokenCode(val code: Int){
    IDENTIFIER(100),
    STRING(101),
    INTEGER(102),
    //Reserved words
    WHILE( 201),
    IF(203),
    PRINT(  205),
    INPUT (206),
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
    DIVISION(403),
    DIVIDE_EQUAL(408),
    COMPARISON_EQUAL(412),
    LOGICAL_AND(417),
    LOGICAL_OR(418),
    OR_EQUAL(419),
    //EOF
    EOF(500)
}