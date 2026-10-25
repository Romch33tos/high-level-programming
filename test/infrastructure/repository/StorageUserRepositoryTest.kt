package infrastructure.repository

import domain.model.ByteArrayValue
import domain.model.PasswordHash
import domain.model.User
import domain.service.PasswordHasher
import infrastructure.storage.InMemoryUserStorage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StorageUserRepositoryTest {

  private val alice = User(
    login = "alice",
    salt = ByteArrayValue(byteArrayOf(1, 2)),
    passwordHash = PasswordHash(byteArrayOf(3, 4))
  )

  private class FakeHasher(private val mapping: Map<String, ByteArray>) : PasswordHasher {
    override fun hash(password: String, salt: ByteArray): ByteArray =
      mapping[password] ?: ByteArray(0)

    override fun verify(password: String, salt: ByteArray, expected: PasswordHash): Boolean =
      hash(password, salt).contentEquals(expected.toByteArray())
  }

  @Test
  fun `shouldFindUserByLogin`() {
    val repo = StorageUserRepository(
      InMemoryUserStorage(listOf(alice)),
      FakeHasher(emptyMap())
    )
    assertSame(alice, repo.findUser("alice"))
  }

  @Test
  fun `shouldReturnNullWhenUserDoesNotExist`() {
    val repo = StorageUserRepository(
      InMemoryUserStorage(listOf(alice)),
      FakeHasher(emptyMap())
    )
    assertNull(repo.findUser("bob"))
  }

  @Test
  fun `shouldVerifyPasswordWhenHashMatches`() {
    val repo = StorageUserRepository(
      InMemoryUserStorage(listOf(alice)),
      FakeHasher(mapOf("secret" to byteArrayOf(3, 4)))
    )
    assertTrue(repo.verifyPassword(alice, "secret"))
  }

  @Test
  fun `shouldNotVerifyPasswordWhenHashDiffers`() {
    val repo = StorageUserRepository(
      InMemoryUserStorage(listOf(alice)),
      FakeHasher(mapOf("wrong" to byteArrayOf(3, 4)))
    )
    assertFalse(repo.verifyPassword(alice, "wrong"))
  }
}
