package domain.model

class ByteArrayValue(private val bytes: ByteArray) {
  fun content(): ByteArray = bytes.copyOf()

  override fun equals(other: Any?): Boolean =
    other is ByteArrayValue && bytes.contentEquals(other.bytes)

  override fun hashCode(): Int = bytes.contentHashCode()
}
