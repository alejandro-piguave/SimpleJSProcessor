import statemachines.*
import java.io.File

class SymbolsTableManager {
    private val globalTable: MutableList<TableEntry> = mutableListOf()
    private val localTables: MutableList<List<TableEntry>> = mutableListOf()
    private var currentLocalTable: MutableList<TableEntry> = mutableListOf()

    private val bracketMachine: FunctionBracketMachine = FunctionBracketMachine()
    private val letIdentifiersMachine: LetIdentifiersMachine = LetIdentifiersMachine()
    private val functionDeclarationMachine: FunctionStateMachine = FunctionStateMachine()

    private var state: SymbolsTableManagerState = SymbolsTableManagerState.INITIAL
    fun updateTable(token: Token){
        state = when(state){
            SymbolsTableManagerState.INITIAL -> {
                when(token.code){
                    TokenCode.LET -> {
                        letIdentifiersMachine.updateState(token)
                        SymbolsTableManagerState.ANALYZING_LET
                    }
                    TokenCode.FUNCTION -> {
                        functionDeclarationMachine.updateState(token)
                        SymbolsTableManagerState.ANALYZING_FUNCTION
                    }
                    else -> SymbolsTableManagerState.INITIAL
                }
            }
            SymbolsTableManagerState.ANALYZING_LET -> {
                val result = letIdentifiersMachine.updateState(token)
                if(result is AfterDeclaringLetType){
                    result.tokens.forEach{ resultToken ->
                        globalTable.add(VariableEntry(resultToken.name, result.type))
                        resultToken.tablePosition = globalTable.lastIndex
                    }
                    letIdentifiersMachine.reset()
                    SymbolsTableManagerState.INITIAL
                } else SymbolsTableManagerState.ANALYZING_LET
            }
            SymbolsTableManagerState.ANALYZING_FUNCTION -> {
                val result = functionDeclarationMachine.updateState(token)
                if(result is AfterDeclaringFunctionLeftBracket){
                    globalTable.add(FunctionEntry(
                        result.function.token.name,
                        result.function.parameters.size,
                        result.function.parameters.map { it.type },
                        result.function.returnType
                    ))
                    result.function.token.tablePosition = globalTable.lastIndex

                    result.function.parameters.forEach {
                        currentLocalTable.add(VariableEntry(it.token.name, it.type))
                        it.token.tablePosition = currentLocalTable.lastIndex
                    }
                    functionDeclarationMachine.reset()
                    SymbolsTableManagerState.INSIDE_FUNCTION
                } else SymbolsTableManagerState.ANALYZING_FUNCTION
            }
            SymbolsTableManagerState.INSIDE_FUNCTION -> {
                val count = bracketMachine.updateState(token)
                if(count == 0) {
                    bracketMachine.reset()
                    destroyCurrentLocalTable()
                    SymbolsTableManagerState.INITIAL
                } else {
                    when(token.code){
                        TokenCode.LET -> {
                            letIdentifiersMachine.updateState(token)
                            SymbolsTableManagerState.ANALYZING_LET
                        }
                        else -> SymbolsTableManagerState.INSIDE_FUNCTION
                    }
                }
            }
            SymbolsTableManagerState.ANALYZING_LET_INSIDE_FUNCTION -> {
                val count = bracketMachine.updateState(token)
                if(count == 0) {
                    bracketMachine.reset()
                    destroyCurrentLocalTable()
                    SymbolsTableManagerState.INITIAL
                } else {
                    val result = letIdentifiersMachine.updateState(token)
                    if(result is AfterDeclaringLetType){
                        result.tokens.forEach{ resultToken ->
                            currentLocalTable.add(VariableEntry(resultToken.name, result.type))
                            resultToken.tablePosition = currentLocalTable.lastIndex
                        }
                        letIdentifiersMachine.reset()
                        SymbolsTableManagerState.INITIAL
                    } else SymbolsTableManagerState.ANALYZING_LET_INSIDE_FUNCTION
                }
            }
        }
    }

    fun save(){
        val text = buildString {
            append("#0:\n")
            globalTable.forEach { entry ->
                appendEntry(entry)
            }
            append("\n")
            var tableCount = 1
            localTables.forEach { localTable ->
                append("#$tableCount:\n")
                localTable.forEach { entry ->
                    appendEntry(entry)
                }
                tableCount++
                append("\n")
            }
        }
        File("src/main/resources/symbols_table.txt").writeText(text)
    }

    private fun StringBuilder.appendEntry(tableEntry: TableEntry){
        append("* '${tableEntry.key}'\n")
        append("+ tipo: '${tableEntry.entryType.nombre}'\n")
        if (tableEntry is FunctionEntry) {
            append("+ numParam: ${tableEntry.parameterCount}\n")
            append("+ TipoRetorno: '${tableEntry.returnType.nombre}'\n")
            tableEntry.parameterTypes.forEachIndexed {i , e ->
                append("\t+ TipoParam$i: '${e.nombre}'\n")
            }
        }
    }

    private fun destroyCurrentLocalTable(){
        localTables.add(currentLocalTable)
        currentLocalTable = mutableListOf()
    }
}

enum class SymbolsTableManagerState{
    INITIAL, ANALYZING_LET, ANALYZING_FUNCTION, INSIDE_FUNCTION, ANALYZING_LET_INSIDE_FUNCTION
}


sealed class TableEntry(val key: String, val entryType: EntryType)
class VariableEntry(key: String, entryType: EntryType): TableEntry(key, entryType)
class FunctionEntry(key: String, var parameterCount: Int, val parameterTypes: List<EntryType>, val returnType: EntryType): TableEntry(key, EntryType.FUNCTION)

enum class EntryType(val nombre: String){
    STRING("cadena"), INTEGER("entero"), BOOLEAN("logico"), FUNCTION("funcion"), VOID("-");
}