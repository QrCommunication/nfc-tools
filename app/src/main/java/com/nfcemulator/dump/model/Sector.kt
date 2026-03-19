package com.nfcemulator.dump.model

data class Sector(
    val index: Int,
    val blocks: List<Block>
) {
    val keyA: ByteArray?
        get() = blocks.lastOrNull()?.keyA

    val keyB: ByteArray?
        get() = blocks.lastOrNull()?.keyB

    val isDecrypted: Boolean
        get() = keyA != null || keyB != null

    val trailerBlock: Block?
        get() = blocks.lastOrNull()

    val dataBlocks: List<Block>
        get() = blocks.dropLast(1)
}
