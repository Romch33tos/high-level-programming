package domain.model

class User(
  val login: String,
  val salt: ByteArrayValue,
  val passwordHash: PasswordHash
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is User) return false
    return login == other.login &&
      salt == other.salt &&
      passwordHash == other.passwordHash
  }

  override fun hashCode(): Int {
    var result = login.hashCode()
    result = 31 * result + salt.hashCode()
    result = 31 * result + passwordHash.hashCode()
    return result
  }
}
