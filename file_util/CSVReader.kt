package file_util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.NoSuchElementException
import kotlin.collections.HashMap

class CSVReader @JvmOverloads constructor(
    filename: String,
    val hasHeader: Boolean,
    val separator: Char = ','
)
{
    val file = File(filename)

    init
    {
        if (hasHeader) getHeader()
    }

    private lateinit var headerWithPositions: HashMap<String, Int>

    val rowCount: Long
        get()
        {
            var rowCount = 0L

            readFileRows { rowCount++ }

            return rowCount
        }

    val columnCount: Int
        get()
        {
            var columnCount = 0

            readFileRows { columnCount = it.size }

            return columnCount
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

    val last get(): List<String> = getRow(rowCount - 1)

    val lastWithNamedColumns get(): Row = Row(getRow(rowCount - 1))

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

            headerWithPositions.forEach { (columnName, position) ->

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
        val rowFromSpecificLine: List<String>

        val specificLine = Files.lines(file.toPath()).use { it.skip(rowNumber + 1).findFirst().get() }

        rowFromSpecificLine = specificLine.split(separator).toList()

        return rowFromSpecificLine
    }

    fun getRowWithNamedColumns(rowNumber: Long): Row
    {
        val specificLine = Files.lines(file.toPath()).use { it.skip(rowNumber + 1).findFirst().get() }

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
            val currentRow = Row(it)

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

    fun groupBy(columnPosition: Int): Map<String, List<List<String>>>
    {
        val groupedRows = mutableMapOf<String, MutableList<List<String>>>()

        readFileRows()
        {
            val currentValueToGroupBy = it[columnPosition]

            if (groupedRows.containsKey(currentValueToGroupBy))
            {
                groupedRows[currentValueToGroupBy]!!.add(it)
            }
            else groupedRows[currentValueToGroupBy] = mutableListOf(it)
        }

        return groupedRows
    }

    fun groupByWithNamedColumns(columnName: String): Map<String, List<Row>>
    {
        val groupedRows = mutableMapOf<String, MutableList<Row>>()

        readFileRows()
        {
            val currentRow = Row(it)
            val currentValueToGroupBy = currentRow[columnName]

            if (!groupedRows.containsKey(currentValueToGroupBy))
            {
                groupedRows[currentValueToGroupBy] = mutableListOf(currentRow)
            }
            else groupedRows[currentValueToGroupBy]!!.add(currentRow)
        }

        return groupedRows
    }

    //endregion

    //region Private methods

    private fun getHeader()
    {
        headerWithPositions = hashMapOf()

        var line: String?

        BufferedReader(FileReader(file)).use { br ->

            if (br.readLine().also { line = it } != null)
            {
                val header = line!!.split(separator.toString())

                for (i in header.indices)
                {
                    headerWithPositions[header[i]] = i
                }
            }
        }
    }

    private fun readFileRows(action: (currentRow: List<String>) -> Unit)
    {
        var line: String?

        BufferedReader(FileReader(file)).use { br ->

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

        BufferedReader(FileReader(file)).use { br ->

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
            if (!headerWithPositions.contains(columnName)) throw ColumnNotFoundException(columnName)

            return columns[headerWithPositions[columnName]!!]
        }

        fun getChar(columnName: String): Char = this[columnName][0]

        fun getByte(columnName: String): Byte = this[columnName].toByte()
        fun getUByte(columnName: String): UByte = this[columnName].toUByte()
        fun getByteOrNull(columnName: String): Byte? = this[columnName].toByteOrNull()
        fun getUByteOrNull(columnName: String): UByte? = this[columnName].toUByteOrNull()

        fun getShort(columnName: String): Short = this[columnName].toShort()
        fun getUShort(columnName: String): UShort = this[columnName].toUShort()
        fun getShortOrNull(columnName: String): Short? = this[columnName].toShortOrNull()
        fun getUShortOrNull(columnName: String): UShort? = this[columnName].toUShortOrNull()

        fun getInt(columnName: String): Int = this[columnName].toInt()
        fun getUInt(columnName: String): UInt = this[columnName].toUInt()
        fun getIntOrNull(columnName: String): Int? = this[columnName].toIntOrNull()
        fun getUIntOrNull(columnName: String): UInt? = this[columnName].toUIntOrNull()

        fun getLong(columnName: String): Long = this[columnName].toLong()
        fun getULong(columnName: String): ULong = this[columnName].toULong()
        fun getLongOrNull(columnName: String): Long? = this[columnName].toLongOrNull()
        fun getULongOrNull(columnName: String): ULong? = this[columnName].toULongOrNull()

        fun getFloat(columnName: String): Float = this[columnName].toFloat()
        fun getFloatOrNull(columnName: String): Float? = this[columnName].toFloatOrNull()
        fun getDouble(columnName: String): Double = this[columnName].toDouble()
        fun getDoubleOrNull(columnName: String): Double? = this[columnName].toDoubleOrNull()

        fun getBoolean(columnName: String): Boolean = this[columnName].toBoolean()
        fun getBooleanOrNull(columnName: String): Boolean? = this[columnName].lowercase().toBooleanStrictOrNull()

        fun getBooleanFromStrings(columnName: String, trueString: String, falseString: String): Boolean
        {
            val columnValue = this[columnName]

            return if (trueString == columnValue) true
            else if (falseString == columnValue) false
            else throw NotABooleanValueException(columnName, columnValue)
        }

        fun getBooleanOrNullFromStrings(columnName: String, trueString: String, falseString: String): Boolean?
        {
            val columnValue = this[columnName]

            return if (trueString == columnValue) true
            else if (falseString == columnValue) false
            else null
        }

        fun getBooleanFromChars(columnName: String, trueChar: Char, falseChar: Char): Boolean
        {
            val columnValue = this[columnName]

            return if (trueChar == columnValue[0]) true
            else if (falseChar == columnValue[0]) false
            else throw NotABooleanValueException(columnName, columnValue)
        }

        fun getBooleanOrNullFromChars(columnName: String, trueChar: Char, falseChar: Char): Boolean?
        {
            val columnValue = this[columnName]

            return if (trueChar == columnValue[0]) true
            else if (falseChar == columnValue[0]) false
            else null
        }

        inline fun <reified T : Enum<T>> getEnum(columnName: String): T = enumValueOf(this[columnName])

        inline fun <reified T : Enum<T>> getEnumOrNull(columnName: String): T? = try
        {
            enumValueOf<T>(this[columnName])
        }
        catch (e: IllegalArgumentException)
        {
            null
        }

        /**
         * Upper-snake-case the toStringValue of the column.
         */
        inline fun <reified T : Enum<T>> getEnumUppersSnakeCase(columnName: String): T
        {
            return enumValueOf(camelRegex.replace(this[columnName].uppercase()) { "_${it.value}" })
        }

        /**
         * Upper-snake-case the toStringValue of the column.
         */
        inline fun <reified T : Enum<T>> getEnumOrNullUpperSnakeCase(columnName: String): T? = try
        {
            enumValueOf<T>(camelRegex.replace(this[columnName].uppercase()) { "_${it.value}" })
        }
        catch (e: IllegalArgumentException)
        {
            null
        }

        // This is to only allow java use this method.
        @SinceKotlin("9999.0")
        fun <T : Enum<T>> getEnum(enumClass: Class<T>, columnName: String): Enum<T>
        {
            return enumClass.enumConstants.first { it.name == this[columnName] }
        }

        @SinceKotlin("9999.0")
        fun <T : Enum<T>> getEnumOrNull(enumClass: Class<T>, columnName: String): T? = try
        {
            enumClass.enumConstants.first { it.name == this[columnName] }
        }
        catch (e: NoSuchElementException)
        {
            null
        }

        /**
         * Upper-snake-case the toStringValue of the column.
         */
        @SinceKotlin("9999.0")
        fun <T : Enum<T>> getEnumUppersSnakeCase(enumClass: Class<T>, columnName: String): T
        {
            return enumClass.enumConstants.first { it.name == camelRegex.replace(this[columnName].uppercase()) { a -> "_${a.value}" } }
        }

        /**
         * Upper-snake-case the toStringValue of the column.
         */
        @SinceKotlin("9999.0")
        fun <T : Enum<T>> getEnumOrNullUpperSnakeCase(enumClass: Class<T>, columnName: String): Enum<T>? = try
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

        override fun toString(): String = columns.toString()
    }
}

class ColumnNotFoundException(columnName: String) : Exception("Column '$columnName' not found")

class NotABooleanValueException(columnName: String, columnValue: String) : Exception("The toStringValue '$columnValue' in '$columnName' column is not a boolean toStringValue")