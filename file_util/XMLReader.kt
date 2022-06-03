import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.w3c.dom.Text
import java.util.function.Consumer
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class XMLReader(val filename: String)
{
    val document: Document

    private var docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

    init
    {
        document = docBuilder.parse(filename)
    }

    //region Methods

    fun readNodesByName(nodeName: String, nodeConsumer: Consumer<Node>)
    {
        val node = Node()
        val nodeList = document.getElementsByTagName(nodeName)

        for (i in 0 until nodeList.length)
        {
            node.value = nodeList.item(i) as Element
            nodeConsumer.accept(node)
        }
    }

    fun readNodesByName(nodeName: String): List<HashMap<String, String>>
    {
        val objects: MutableList<HashMap<String, String>> = ArrayList()
        val nodeList = document.getElementsByTagName(nodeName)

        for (i in 0 until nodeList.length)
        {
            val children = nodeList.item(i) as NodeList
            val currentObject = HashMap<String, String>()

            for (j in 0 until children.length)
            {
                val child = children.item(j)

                // Excludes the text nodes
                if (Text::class.java.isAssignableFrom(child.javaClass)) continue

                currentObject[child.nodeName] = child.textContent
            }
            objects.add(currentObject)
        }

        return objects
    }

    //endregion

    class Node
    {
        lateinit var value: Element

        fun getString(childName: String?): String = value.getElementsByTagName(childName).item(0).textContent

        fun getChar(columnName: String): Char = this[columnName][0]

        fun getByte(childName: String): Byte = this[childName].toByte()

        fun getUByte(childName: String): UByte = this[childName].toUByte()

        fun getShort(childName: String): Short = this[childName].toShort()

        fun getUShort(childName: String): UShort = this[childName].toUShort()

        fun getInt(childName: String): Int = this[childName].toInt()

        fun getUInt(childName: String): UInt = this[childName].toUInt()

        fun getLong(childName: String): Long = this[childName].toLong()

        fun getULong(childName: String): ULong = this[childName].toULong()

        fun getFloat(childName: String): Float = this[childName].toFloat()

        fun getDouble(childName: String): Double = this[childName].toDouble()

        fun getBoolean(childName: String): Boolean = this[childName].toBoolean()

        fun getAttributeContent(name: String): String = value.getAttribute(name)

        operator fun get(childName: String): String = getString(childName)
    }
}