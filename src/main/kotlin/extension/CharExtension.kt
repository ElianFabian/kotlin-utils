fun Char.toBoolean(trueChar: Char, falseChar: Char): Boolean?
{
    return if (this == trueChar) true
    else if (this == falseChar) false
    else null
}
