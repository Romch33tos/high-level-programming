package infrastructure.utils

fun String.hexToBytes(): ByteArray =
  chunked(2).map { it.toInt(16).toByte() }.toByteArray()
