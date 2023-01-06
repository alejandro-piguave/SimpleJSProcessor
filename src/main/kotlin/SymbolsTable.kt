import java.io.File

class SymbolsTable {
    private val globalTable: MutableList<TableEntry> = mutableListOf()
    private val localTableHistory: MutableList<List<TableEntry>> = mutableListOf()
    private val currentLocalTable: MutableList<TableEntry> = mutableListOf()

    var isCurrentTableGlobal = true

    private val currentWorkingTable: MutableList<TableEntry>
        get() = if(isCurrentTableGlobal) globalTable else currentLocalTable

    fun getIdToken(name: String): IdentifierToken{
        currentWorkingTable.forEachIndexed { index, tableEntry ->
            if(tableEntry.key == name){
                //println("Retrieving token '$name' from the current table...")
                return IdentifierToken(name, index, isCurrentTableGlobal)
            }
        }

        //If it hasn't found it in the current table and the current table is not global,
        //then search in the global table too
        if(!isCurrentTableGlobal){
            globalTable.forEachIndexed { index, tableEntry ->
                if(tableEntry.key == name){
                    //println("Retrieving token '$name' from the global table...")
                    return IdentifierToken(name, index, true)
                }
            }

        }

        //If none of the searches are successful, add the token in the current table and return it
        //println("Adding token '$name' to the current table...")
        currentWorkingTable.add(TableEntry(name))
        return IdentifierToken(name, currentWorkingTable.lastIndex, isCurrentTableGlobal)
    }

    fun addEntryType(position: Int, type: EntryType){
        currentWorkingTable[position].entryType = type
        if(type != EntryType.FUNCTION){
            val varCount = currentWorkingTable.count { it.entryType != EntryType.FUNCTION }
            addDisplacement(position, (varCount-1)*2)
        }
    }

    fun addFunctionTag(position: Int, name: String){
        val functionCount = currentWorkingTable.count { it.entryType == EntryType.FUNCTION }
        currentWorkingTable[position].functionTag = "Et${functionCount}_${name}"
    }

    private fun addDisplacement(position: Int, displacement: Int){
        currentWorkingTable[position].displacement = displacement
    }

    fun addReturnType(position: Int, type: EntryType){
        globalTable[position].returnType = type
    }

    fun getEntryType(identifierToken: IdentifierToken): EntryType?{
        return if(identifierToken.isInGlobalTable){
            globalTable[identifierToken.tablePosition].entryType
        } else currentWorkingTable[identifierToken.tablePosition].entryType
    }

    //Only performs additions in the global table
    fun addFunctionParameter(position: Int, parameter: EntryType){
        globalTable[position].parameterTypes.add(parameter)
        globalTable[position].parameterCount = globalTable[position].parameterTypes.size
    }

    fun getFunctionParameters(position: Int): List<EntryType> {
        return globalTable[position].parameterTypes //Functions can only be declared at a global level
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
            append("+ EtiqFuncion: '${tableEntry.functionTag}'\n")
            tableEntry.parameterTypes.forEachIndexed {i , e ->
                append("\t+ TipoParam${i+1}: '${e.nombre}'\n")
            }
        } else {
            append("+ Despl: ${tableEntry.displacement}\n")
        }
    }

    fun createLocalTable(){
        isCurrentTableGlobal = false
    }

    fun destroyCurrentLocalTable(){
        localTableHistory.add(currentLocalTable.toList())
        currentLocalTable.clear()
        isCurrentTableGlobal = true
    }

}
class TableEntry(val key: String,
                 var entryType: EntryType? = null,
                 var parameterCount: Int = 0,
                 val parameterTypes: MutableList<EntryType> = mutableListOf(),
                 var returnType: EntryType? = null,
                 var functionTag: String = "",
                 var displacement: Int = 0
)

enum class EntryType(val nombre: String){
    STRING("cadena"),
    INTEGER("entero"),
    BOOLEAN("logico"),
    FUNCTION("funcion"),
    VOID("-");
}