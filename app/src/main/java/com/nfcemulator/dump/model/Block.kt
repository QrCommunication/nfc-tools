package com.nfcemulator.dump.model

data class Block(
    val index: Int,
    val data: ByteArray,          // 16 bytes
    val keyA: ByteArray? = null,  // 6 bytes
    val keyB: ByteArray? = null,  // 6 bytes
    val accessBits: ByteArray? = null  // 4 bytes
) {
    val isTrailerBlock: Boolean
        get() = (index + 1) % 4 == 0 || (index >= 128 && (index + 1) % 16 == 0)

    val dataHex: String
        get() = data.joinToString(" ") { "%02X".format(it) }

    val keyAHex: String?
        get() = keyA?.joinToString(":") { "%02X".format(it) }

    val keyBHex: String?
        get() = keyB?.joinToString(":") { "%02X".format(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false
        return index == other.index && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + data.contentHashCode()
        return result
    }
}
