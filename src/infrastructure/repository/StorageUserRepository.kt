package infrastructure.repository

import domain.model.User
import domain.repository.UserRepository
import domain.service.PasswordHasher
import infrastructure.storage.InMemoryUserStorage
import java.security.MessageDigest

class StorageUserRepository(
  private val storage: InMemoryUserStorage,
  private val hasher: PasswordHasher
) : UserRepository {
  override fun findUser(login: String): User? = storage.findByLogin(login)

  override fun verifyPassword(user: User, password: String): Boolean {
    val actual = hasher.hash(password, user.salt.content())
    return MessageDigest.isEqual(actual, user.passwordHash.toByteArray())
  }
}
