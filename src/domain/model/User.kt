package domain.model

data class User(
  val login: String,
  val salt: ByteArrayValue,
  val passwordHash: PasswordHash
)
