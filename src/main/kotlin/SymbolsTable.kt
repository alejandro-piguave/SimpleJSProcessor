import java.io.File

class SymbolsTable {
    private val globalTable: MutableList<TableEntry> = mutableListOf()
    private val localTableHistory: MutableList<List<TableEntry>> = mutableListOf()
    private var currentLocalTable: MutableList<TableEntry> = globalTable

    val isCurrentTableGlobal: Boolean
        get() = currentLocalTable === globalTable

    fun getIdToken(name: String): IdentifierToken{
        currentLocalTable.forEachIndexed { index, tableEntry ->
            if(tableEntry.key == name){
                return IdentifierToken(name, index)
            }
        }
        currentLocalTable.add(TableEntry(name))
        return IdentifierToken(name, currentLocalTable.lastIndex)
    }

    fun addEntryType(position: Int, type: EntryType){
        currentLocalTable[position].entryType = type
    }

    fun addReturnType(position: Int, type: EntryType){
        currentLocalTable[position].returnType = type
    }

    fun getEntryType(position: Int): EntryType?{
        return currentLocalTable[position].entryType
    }

    //Only performs additions in the global table
    fun addFunctionParameter(position: Int, parameter: EntryType){
        globalTable[position].parameterTypes.add(parameter)
        globalTable[position].parameterCount = globalTable[position].parameterTypes.size
    }

    fun getEntryParameters(position: Int): List<EntryType> {
        return currentLocalTable[position].parameterTypes
    }

    fun save(){
        val text = buildString {
            append("#0:\n")
            globalTable.forEach { entry ->
                appendEntry(entry)
            }
            append("\n")
            var tableCount = 1
            localTableHistory.forEach { localTable ->
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
        append("+ tipo: '${tableEntry.entryType?.nombre}'\n")
        if (tableEntry.entryType == EntryType.FUNCTION) {
            append("+ numParam: ${tableEntry.parameterCount}\n")
            append("+ TipoRetorno: '${tableEntry.returnType?.nombre}'\n")
            tableEntry.parameterTypes.forEachIndexed {i , e ->
                append("\t+ TipoParam$i: '${e.nombre}'\n")
            }
        }
    }

    fun createLocalTable(){
        currentLocalTable = mutableListOf()
    }

    fun destroyCurrentLocalTable(){
        localTableHistory.add(currentLocalTable)
        currentLocalTable = globalTable
    }

}
class TableEntry(val key: String,
                 var entryType: EntryType? = null,
                 var parameterCount: Int = 0,
                 val parameterTypes: MutableList<EntryType> = mutableListOf(),
                 var returnType: EntryType? = null)

enum class EntryType(val nombre: String){
    STRING("cadena"),
    INTEGER("entero"),
    BOOLEAN("logico"),
    FUNCTION("funcion"),
    VOID("-");
}