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
            currentRow.value = it
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
            val currentValue = it.getString(columnName)

            values.add(currentValue)
        }

        return values
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

    fun findFirstWithNamedColumns(row: Predicate<Row>): List<String>
    {
        val currentRow = Row()
        var foundRow = emptyList<String>()

        readFileRows()
        {
            val hasBeenFound = row.test(currentRow.apply { value = it })
            if (hasBeenFound) foundRow = currentRow.value
        }

        return foundRow
    }

    fun findAllWithNamedColumns(row: Predicate<Row>): List<List<String>>
    {
        val currentRow = Row()
        val foundRows = mutableListOf<List<String>>()

        readFileRows()
        {
            val hasBeenFound = row.test(currentRow.apply { value = it })
            if (hasBeenFound) foundRows.add(it)
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

    private fun readFileRows(lambda: (currentRow: List<String>) -> Unit)
    {
        var line: String?

        BufferedReader(FileReader(filename)).use { br ->

            if (hasHeader) br.readLine()

            while (br.readLine().also { line = it } != null)
            {
                val currentRow = line!!.split(separator.toString())
                lambda(currentRow)
            }
        }
    }

    //endregion

    inner class Row
    {
        lateinit var value: List<String>

        fun getString(columnName: String): String = value[headersWithPositions[columnName]!!]

        fun getChar(columnName: String): Char = getString(columnName)[0]

        fun getByte(columnName: String): Byte = getString(columnName).toByte()

        fun getUByte(columnName: String): UByte = getString(columnName).toUByte()

        fun getShort(columnName: String): Short = getString(columnName).toShort()

        fun getUShort(columnName: String): UShort = getString(columnName).toUShort()

        fun getInt(columnName: String): Int = getString(columnName).toInt()

        fun getUInt(columnName: String): UInt = getString(columnName).toUInt()

        fun getLong(columnName: String): Long = getString(columnName).toLong()

        fun getULong(columnName: String): ULong = getString(columnName).toULong()

        fun getFloat(columnName: String): Float = getString(columnName).toFloat()

        fun getDouble(columnName: String): Double = getString(columnName).toDouble()

        fun getBoolean(columnName: String): Boolean = getString(columnName).toBoolean()
    }
}