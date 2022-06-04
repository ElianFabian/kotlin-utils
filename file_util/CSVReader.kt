import com.elian.extension.camelToSnakeCase
import java.io.BufferedReader
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
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

    val rowCount: Int
        get()
        {
            var rowCount = 0

            readFileRows { rowCount++ }

            return rowCount
        }

    val first
        get(): List<String>
        {
            var first = emptyList<String>()

            readFirstFileRow { first = it }

            return first
        }

    val firstWithNamedColumns
        get(): Row
        {
            val firstRow = Row()

            readFirstFileRow { firstRow.columns = it }

            return firstRow
        }

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

    fun getRow(rowNumber: Long): List<String>
    {
        var rowFromSpecificLine = emptyList<String>()

        val specificLine = Files.lines(Paths.get(filename)).use { it.skip(rowNumber + 1).findFirst().get() }

        rowFromSpecificLine = specificLine.split(separator).toList()

        return rowFromSpecificLine
    }

    fun getRowWithNamedColumns(rowNumber: Long): Row
    {
        val specificLine = Files.lines(Paths.get(filename)).use { it.skip(rowNumber + 1).findFirst().get() }

        val columns = specificLine.split(separator).toList()

        return Row(columns)
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

    private fun readFirstFileRow(action: (currentRow: List<String>) -> Unit)
    {
        var line: String?

        BufferedReader(FileReader(filename)).use { br ->

            if (hasHeader) br.readLine()

            if (br.readLine().also { line = it } != null)
            {
                val currentRow = line!!.split(separator.toString())
                action(currentRow)
            }
        }
    }

    //endregion

    inner class Row(var columns: List<String> = listOf())
    {
        @PublishedApi
        internal val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

        fun getString(columnName: String): String
        {
            if (!headersWithPositions.contains(columnName)) throw IllegalArgumentException("Column $columnName does not exist")

            return columns[headersWithPositions[columnName]!!]
        }

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

        inline fun <reified T : Enum<T>> getEnum(columnName: String): T? = try
        {
            enumValueOf<T>(this[columnName])
        }
        catch (e: IllegalArgumentException)
        {
            null
        }

        /**
         * Uppercase with underscores the value of the column.
         */
        inline fun <reified T : Enum<T>> getEnumUsingUpperCase(columnName: String): T? = try
        {
            enumValueOf<T>(camelRegex.replace(this[columnName].uppercase()) { "_${it.value}" })
        }
        catch (e: IllegalArgumentException)
        {
            null
        }

        // This is to only allow java use this method.
        @SinceKotlin("9999.0")
        fun <T : Enum<T>> getEnum(enumClass: Class<T>, columnName: String): Enum<T>? = try
        {
            enumClass.enumConstants.first { it.name == this[columnName] }
        }
        catch (e: NoSuchElementException)
        {
            null
        }

        /**
         * Uppercase with underscores the value of the column.
         */
        @SinceKotlin("9999.0")
        fun <T : Enum<T>> getEnumUsingUpperCase(enumClass: Class<T>, columnName: String): Enum<T>? = try
        {
            enumClass.enumConstants.first { it.name == camelRegex.replace(this[columnName].uppercase()) { a -> "_${a.value}" } }
        }
        catch (e: NoSuchElementException)
        {
            null
        }

        /**
         * Equivalent to getString().
         */
        operator fun get(columnName: String): String = getString(columnName)
    }
}