enum class ParsingError(val code: Int, val message: String) {
    INTEGER_OVERFLOW(1, "An integer's value must be between -32768 and 32767."),
    STRING_OVERFLOW(2, "A string can't be longer than 64 characters."),
    BAD_STRING_FORMATTING(3, "Bad string formatting."),
    GENERIC_ERROR(4, "An error occurred reading this lexeme."),
    BAD_COMMENT_FORMATTING(5, "A comment wasn't formatted correctly."),
    BAD_INTEGER_FORMATTING(6, "The integer wasn't correctly formatted."),
    BAD_OPERATOR_FORMATTING(7, "The operator wasn't correctly formatted."),
    BAD_IDENTIFIER_NAMING(8, "The name used for this identifier is not correct."),
    BAD_CHARACTER(9, "This character can't be used.")
}

class ErrorReport(val parsingError: ParsingError, val line: Int)