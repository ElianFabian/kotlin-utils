import java.io.BufferedReader
import java.io.FileReader
import java.util.function.Consumer
import java.util.function.Predicate

class CSVReader @JvmOverloads constructor(
    val filename: String,
    val hasHeader: Boolean,
    val separator: Char = ','
)
{
    init
    {
        if (hasHeader) getHeader()
    }

    private lateinit var headersWithPositions: HashMap<String, Int>

    //region Public methods

    fun read(row: Consumer<List<String>>) = readFileRows { row.accept(it) }

    fun read(): List<List<String>>
    {
        val rows = mutableListOf<List<String>>()

        readFileRows { rows.add(it) }

        return rows
    }

    fun readWithNamedColumns(row: Consumer<Row>)
    {
        val currentRow = Row()

        readFileRows()
        {
            currentRow.columns = it
            row.accept(currentRow)
        }
    }

    fun readWithNamedColumns(): List<HashMap<String, String>>
    {
        val rows: MutableList<HashMap<String, String>> = mutableListOf()

        readFileRows()
        {
            val namedRow = hashMapOf<String, String>()

            headersWithPositions.forEach { (columnName, position) ->

                namedRow[columnName] = it[position]
            }
            rows.add(namedRow)
        }

        return rows
    }

    fun getAllColumnValues(columnPosition: Int): List<String>
    {
        val values = mutableListOf<String>()

        read()
        {
            val currentValue = it[columnPosition]

            values.add(currentValue)
        }

        return values
    }

    fun getAllColumnValues(columnName: String): List<String>
    {
        val values = mutableListOf<String>()

        readWithNamedColumns()
        {
            val currentValue = it[columnName]

            values.add(currentValue)
        }

        return values
    }
    
    fun getUniqueValues(columnPosition: Int): List<String>
    {
        val values = mutableListOf<String>()

        read()
        {
            val currentValue = it[columnPosition]

            if (!values.contains(currentValue)) values.add(currentValue)
        }

        return values.apply { remove("") }
    }
    
    fun getUniqueValues(columnName: String): List<String>
    {
        val values = mutableListOf<String>()

        readWithNamedColumns()
        {
            val currentValue = it[columnName]

            if (!values.contains(currentValue)) values.add(currentValue)
        }

        return values.apply { remove("") }
    }

    fun findFirst(row: Predicate<List<String>>): List<String>
    {
        var foundRow: List<String> = emptyList()

        readFileRows()
        {
            val hasBeenFound = row.test(it)
            if (hasBeenFound) foundRow = it
        }

        return foundRow
    }

    fun findAll(row: Predicate<List<String>>): List<List<String>>
    {
        val foundRows = mutableListOf<List<String>>()

        readFileRows()
        {
            val hasBeenFound = row.test(it)
            if (hasBeenFound) foundRows.add(it)
        }

        return foundRows
    }

    fun findFirstWithNamedColumns(row: Predicate<Row>): Row
    {
        var foundRow = Row()

        readFileRows()
        {
            val currentRow = Row(columns = it)

            val hasBeenFound = row.test(currentRow)
            if (hasBeenFound) foundRow = currentRow
        }

        return foundRow
    }

    fun findAllWithNamedColumns(row: Predicate<Row>): List<Row>
    {
        val foundRows = mutableListOf<Row>()

        readFileRows()
        {
            val currentRow = Row(columns = it)

            val hasBeenFound = row.test(currentRow)
            if (hasBeenFound) foundRows.add(currentRow)
        }

        return foundRows
    }

    //endregion

    //region Private methods

    private fun getHeader()
    {
        headersWithPositions = hashMapOf()

        var line: String?

        BufferedReader(FileReader(filename)).use { br ->

            if (br.readLine().also { line = it } != null)
            {
                val header = line!!.split(separator.toString())

                for (i in header.indices)
                {
                    headersWithPositions[header[i]] = i
                }
            }
        }
    }

    private fun readFileRows(action: (currentRow: List<String>) -> Unit)
    {
        var line: String?

        BufferedReader(FileReader(filename)).use { br ->

            if (hasHeader) br.readLine()

            while (br.readLine().also { line = it } != null)
            {
                val currentRow = line!!.split(separator.toString())
                action(currentRow)
            }
        }
    }

    //endregion

    inner class Row(var columns: List<String> = listOf())
    {
        fun getString(columnName: String): String = columns[headersWithPositions[columnName]!!]

        fun getChar(columnName: String): Char = this[columnName][0]

        fun getByte(columnName: String): Byte = this[columnName].toByte()

        fun getUByte(columnName: String): UByte = this[columnName].toUByte()

        fun getShort(columnName: String): Short = this[columnName].toShort()

        fun getUShort(columnName: String): UShort = this[columnName].toUShort()

        fun getInt(columnName: String): Int = this[columnName].toInt()

        fun getUInt(columnName: String): UInt = this[columnName].toUInt()

        fun getLong(columnName: String): Long = this[columnName].toLong()

        fun getULong(columnName: String): ULong = this[columnName].toULong()

        fun getFloat(columnName: String): Float = this[columnName].toFloat()

        fun getDouble(columnName: String): Double = this[columnName].toDouble()

        fun getBoolean(columnName: String): Boolean = this[columnName].toBoolean()

        operator fun get(columnName: String): String = getString(columnName)
    }
}