package semantic

import EntryType

sealed class SemanticException(message: String?): Exception(message)

class UnexpectedTypeException private constructor(message: String): SemanticException(message){
    constructor(line: Int, actualType: EntryType, expectedType: EntryType) : this("Unexpected type found in line $line: '${actualType.name}' found but was expecting '${expectedType.name}'")
    constructor(line: Int, actualType: EntryType, expectedTypes: List<EntryType>) : this("Unexpected type found in line $line: '${actualType.name}' found but was expecting one of the following '${expectedTypes.map { it.name }}'")

}

class UnexpectedIdentifierException private constructor(message: String): SemanticException(message){
    constructor(line: Int, name: String): this("Unexpected identifier found in line $line: '$name' was used but not declared.")
}

class UnexpectedFunctionDeclarationException private constructor(message: String): SemanticException(message){
    constructor(line: Int): this("Unexpected function declaration in line $line: nested function declaration is not allowed.")
}

class UnexpectedReturnUseException private constructor(message: String): SemanticException(message){
    constructor(line: Int): this("Unexpected 'return' use in line $line: the 'return' is only allowed inside a function declaration.")
}

class TooManyArgumentsException( functionName: String): SemanticException("Too many arguments for function '$functionName'")

class MissingParametersException(functionName: String): SemanticException("Parameters missing for function '$functionName'")