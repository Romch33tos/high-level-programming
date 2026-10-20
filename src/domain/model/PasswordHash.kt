package domain.model

class PasswordHash(private val bytes: ByteArray) {
  fun matches(other: ByteArray): Boolean = bytes.contentEquals(other)

  fun toByteArray(): ByteArray = bytes.copyOf()

  override fun equals(other: Any?): Boolean =
    other is PasswordHash && bytes.contentEquals(other.bytes)

  override fun hashCode(): Int = bytes.contentHashCode()
}
