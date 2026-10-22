package domain.service

import domain.model.PasswordHash

interface PasswordHasher {
  fun hash(password: String, salt: ByteArray): ByteArray

  fun verify(password: String, salt: ByteArray, expected: PasswordHash): Boolean
}
