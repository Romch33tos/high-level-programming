package domain.model

data class AccessRule(
  val login: String,
  val resourcePath: String,
  val canRead: Boolean,
  val canWrite: Boolean,
  val canExecute: Boolean
)
