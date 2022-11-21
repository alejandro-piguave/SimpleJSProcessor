package syntax

import TokenCode

sealed class SyntaxException(message: String?): Exception(message)

object EOFException: SyntaxException("EOF character was reached and syntax analysis wasn't finished yet.")
class UnexpectedTokenException private constructor(message: String?): SyntaxException(message){
    constructor(line: Int, actualTokenCode: TokenCode, expectedTokenCode: TokenCode) : this("Unexpected token found in line $line: '${actualTokenCode.name}' found but was expecting '${expectedTokenCode.name}'")
    constructor(line: Int, actualTokenCode: TokenCode, expectedTokenCodes: List<TokenCode>) : this("Unexpected token found in line $line: '${actualTokenCode.name}' found but was expecting one of the following $expectedTokenCodes")
}