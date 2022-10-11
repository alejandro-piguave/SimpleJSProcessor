import statemachines.*
import java.io.File

class SymbolsTableManager {
    private val globalTable: LinkedHashMap<String, TableEntry> = LinkedHashMap()
    private val localTables: MutableList<LinkedHashMap<String, TableEntry>> = mutableListOf()
    private var currentLocalTable: LinkedHashMap<String, TableEntry> = LinkedHashMap()

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
                    result.identifiers.forEach{ identifier ->
                        globalTable[identifier] = TableEntry.fromType(result.type)!!
                    }
                    letIdentifiersMachine.reset()
                    SymbolsTableManagerState.INITIAL
                } else SymbolsTableManagerState.ANALYZING_LET
            }
            SymbolsTableManagerState.ANALYZING_FUNCTION -> {
                val result = functionDeclarationMachine.updateState(token)
                if(result is AfterDeclaringFunctionLeftBracket){
                    globalTable[result.function.name] =
                        FunctionEntry(
                            result.function.parameters.size,
                            result.function.parameters.map { it.type },
                            result.function.returnType
                        )
                    result.function.parameters.forEach {
                        currentLocalTable[it.name] = TableEntry.fromType(it.type)!!
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
                        result.identifiers.forEach{ identifier ->
                            currentLocalTable[identifier] = TableEntry.fromType(result.type)!!
                        }
                        letIdentifiersMachine.reset()
                        SymbolsTableManagerState.INITIAL
                    } else SymbolsTableManagerState.ANALYZING_LET_INSIDE_FUNCTION
                }
            }
        }
    }

    fun saveSymbolsTable(){
        val text = buildString {
            append("#0:\n")
            globalTable.forEach { (t, u) ->
                append(t,u)
            }
            append("\n")
            var tableCount = 1
            localTables.forEach { localTable ->
                append("#$tableCount:\n")
                localTable.forEach { (t, u) ->
                    append(t,u)
                }
                tableCount++
                append("\n")
            }
        }
        File("src/main/resources/symbols_table.txt").writeText(text)
    }

    private fun StringBuilder.append(name: String, tableEntry: TableEntry){
        append("* '$name'\n")
        append("+ tipo: '${tableEntry.entryType.nombre}'\n")
        if (tableEntry is FunctionEntry) {
            append("+ numParam: ${tableEntry.parameterCount}\n")
            append("+ tipoDev: ${tableEntry.returnType}\n")
            tableEntry.parameterTypes.forEachIndexed {i , e ->
                append("\t+ TipoParam$i: '${e.nombre}'\n")
            }
        }
    }

    private fun destroyCurrentLocalTable(){
        localTables.add(currentLocalTable)
        currentLocalTable = LinkedHashMap()
    }
}

enum class SymbolsTableManagerState{
    INITIAL, ANALYZING_LET, ANALYZING_FUNCTION, INSIDE_FUNCTION, ANALYZING_LET_INSIDE_FUNCTION
}


sealed class TableEntry(val entryType: EntryType){
    companion object{
        fun fromType(entryType: EntryType): TableEntry?{
            return when(entryType){
                EntryType.INTEGER -> IntegerEntry
                EntryType.STRING -> StringEntry
                EntryType.BOOLEAN -> BooleanEntry
                else -> null
            }
        }
    }
}

object StringEntry: TableEntry(EntryType.STRING)
object IntegerEntry: TableEntry(EntryType.INTEGER)
object BooleanEntry: TableEntry(EntryType.BOOLEAN)

class FunctionEntry(var parameterCount: Int, val parameterTypes: List<EntryType>, val returnType: EntryType): TableEntry(EntryType.FUNCTION)

enum class EntryType(val nombre: String){
    STRING("string"), INTEGER("entero"), BOOLEAN("logico"), FUNCTION("funcion"), VOID("void");
}