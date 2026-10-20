package infrastructure.security

import domain.model.PasswordHash
import domain.service.PasswordHasher
import java.security.MessageDigest

class Sha256PasswordHasher : PasswordHasher {
  override fun hash(password: String, salt: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(salt)
    return digest.digest(password.toByteArray(Charsets.UTF_8))
  }

  override fun verify(password: String, salt: ByteArray, expected: PasswordHash): Boolean {
    val actual = hash(password, salt)
    return MessageDigest.isEqual(actual, expected.toByteArray())
  }
}
