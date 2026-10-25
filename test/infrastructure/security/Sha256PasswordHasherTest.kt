package infrastructure.security

import domain.model.PasswordHash
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Sha256PasswordHasherTest {

  private val hasher = Sha256PasswordHasher()
  private val salt = byteArrayOf(1, 2, 3, 4)

  @Test
  fun `shouldProduceDeterministicHash`() {
    val first = hasher.hash("secret", salt)
    val second = hasher.hash("secret", salt)
    assertArrayEquals(first, second)
  }

  @Test
  fun `shouldProduceSameLengthAsSha256`() {
    assertArrayEquals(ByteArray(32), hasher.hash("", ByteArray(0)).copyOf())
    // фактическая проверка длины:
    val hash = hasher.hash("secret", salt)
    assertTrue(hash.size == 32)
  }

  @Test
  fun `shouldProduceDifferentHashForDifferentPasswords`() {
    val a = hasher.hash("secret", salt)
    val b = hasher.hash("secret2", salt)
    assertFalse(a.contentEquals(b))
  }

  @Test
  fun `shouldProduceDifferentHashForDifferentSalts`() {
    val a = hasher.hash("secret", byteArrayOf(1, 2, 3, 4))
    val b = hasher.hash("secret", byteArrayOf(4, 3, 2, 1))
    assertFalse(a.contentEquals(b))
  }

  @Test
  fun `shouldVerifyWhenPasswordMatches`() {
    val expected = PasswordHash(hasher.hash("secret", salt))
    assertTrue(hasher.verify("secret", salt, expected))
  }

  @Test
  fun `shouldNotVerifyWhenPasswordDiffers`() {
    val expected = PasswordHash(hasher.hash("secret", salt))
    assertFalse(hasher.verify("wrong", salt, expected))
  }

  @Test
  fun `shouldNotVerifyWhenSaltDiffers`() {
    val expected = PasswordHash(hasher.hash("secret", salt))
    assertFalse(hasher.verify("secret", byteArrayOf(9, 9, 9, 9), expected))
  }
}
