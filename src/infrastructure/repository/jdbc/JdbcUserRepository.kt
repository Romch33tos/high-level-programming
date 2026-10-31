package infrastructure.repository.jdbc

import domain.model.ByteArrayValue
import domain.model.PasswordHash
import domain.model.User
import domain.repository.UserRepository
import domain.service.PasswordHasher
import java.sql.SQLException

class JdbcUserRepository(
  private val hasher: PasswordHasher
) : UserRepository {

  override fun verifyPassword(user: User, password: String): Boolean {
    val actual = hasher.hash(password, user.salt.content())
    return java.security.MessageDigest.isEqual(actual, user.passwordHash.toByteArray())
  }
}
