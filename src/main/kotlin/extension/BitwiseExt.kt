package extension

infix fun Int.joinAsLong(other: Int): Long = (this.toLong() shl Int.SIZE_BITS) + (other.toLong() and 0xFFFF_FFFFL)
fun Long.toIntPair(): Pair<Int, Int> = component1() to component0()
fun Long.toIntArray(): IntArray = intArrayOf(component1(), component0())
operator fun Long.component0(): Int = (this shr Int.SIZE_BITS).toInt()
operator fun Long.component1(): Int = this.toInt()

fun Long.byte0(): Byte = this.toByte()
fun Long.byte1(): Byte = (this shr Byte.SIZE_BITS).toByte()
fun Long.byte2(): Byte = (this shr Byte.SIZE_BITS * 2).toByte()
fun Long.byte3(): Byte = (this shr Byte.SIZE_BITS * 3).toByte()
fun Long.toByteArray(): ByteArray = byteArrayOf(byte3(), byte2(), byte1(), byte0())
fun joinAsLong(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte): Long
{
    return (byte3.toLong() shl Byte.SIZE_BITS * 3) + (byte2.toLong() shl Byte.SIZE_BITS * 2) + (byte1.toLong() shl Byte.SIZE_BITS) + (byte0.toLong() and 0xFFFF)
}

infix fun Byte.joinAsInt(other: Byte): Int = (this.toInt() shl Byte.SIZE_BITS) + (other.toInt() and 0xFFFF)
fun Int.toBytePair(): Pair<Byte, Byte> = component1() to component0()
fun Int.toByteArray(): ByteArray = byteArrayOf(component1(), component0())
operator fun Int.component0(): Byte = (this shr Byte.SIZE_BITS).toByte()
operator fun Int.component1(): Byte = this.toByte()
