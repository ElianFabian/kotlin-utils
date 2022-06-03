


fun String.toBoolean(charArray: String): Boolean?
{
    if (charArray.isEmpty()) throw IllegalArgumentException("CharArray is empty")
    if (charArray.length != 2) throw IllegalArgumentException("CharArray length must be 2")

    return if (this == charArray[0].toString()) true
    else if (this == charArray[1].toString()) false
    else null
}