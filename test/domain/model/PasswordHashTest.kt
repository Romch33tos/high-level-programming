package domain.model

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordHashTest {

  @Test
  fun `shouldMatchWhenBytesEqual`() {
    val hash = PasswordHash(byteArrayOf(1, 2, 3))
    assertTrue(hash.matches(byteArrayOf(1, 2, 3)))
  }

  @Test
  fun `shouldNotMatchWhenBytesDiffer`() {
    val hash = PasswordHash(byteArrayOf(1, 2, 3))
    assertFalse(hash.matches(byteArrayOf(1, 2, 4)))
  }

  @Test
  fun `shouldReturnCopyOfBytes`() {
    val hash = PasswordHash(byteArrayOf(1, 2, 3))
    val copy = hash.toByteArray()
    assertArrayEquals(byteArrayOf(1, 2, 3), copy)
    copy[0] = 99
    assertEquals(1, hash.toByteArray()[0])
  }

  @Test
  fun `shouldBeEqualWhenBytesEqual`() {
    val a = PasswordHash(byteArrayOf(1, 2, 3))
    val b = PasswordHash(byteArrayOf(1, 2, 3))
    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun `shouldNotBeEqualWhenBytesDiffer`() {
    val a = PasswordHash(byteArrayOf(1, 2, 3))
    val b = PasswordHash(byteArrayOf(1, 2, 4))
    assertNotEquals(a, b)
  }
}
