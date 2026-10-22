package domain.model

class ResourcePath private constructor(val value: String) {
  val segments: List<String>
    get() = value.split(".")

  fun parentSegments(): List<String> = segments.dropLast(1)

  fun isPrefixOf(other: ResourcePath): Boolean {
    val otherSegments = other.segments
    return segments.size <= otherSegments.size &&
      segments.indices.all { segments[it] == otherSegments[it] }
  }

  override fun equals(other: Any?): Boolean =
    other is ResourcePath && value == other.value

  override fun hashCode(): Int = value.hashCode()

  override fun toString(): String = value

  companion object {
    private val namePattern = Regex("^[A-Za-z0-9_]{1,20}$")

    fun parse(value: String): ResourcePath? {
      if (value.isEmpty()) return null
      val segments = value.split(".")
      if (segments.isEmpty()) return null
      if (!segments.all { namePattern.matches(it) }) return null
      return ResourcePath(value)
    }

    fun fromValidated(value: String): ResourcePath = ResourcePath(value)
  }
}
