package domain.model

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ByteArrayValueTest {

  @Test
  fun `shouldReturnCopyOfContent`() {
    val original = byteArrayOf(1, 2, 3)
    val value = ByteArrayValue(original)
    val copy = value.content()
    assertArrayEquals(original, copy)
    copy[0] = 99
    assertEquals(1, value.content()[0])
  }

  @Test
  fun `shouldBeEqualWhenContentIsEqual`() {
    val a = ByteArrayValue(byteArrayOf(1, 2, 3))
    val b = ByteArrayValue(byteArrayOf(1, 2, 3))
    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun `shouldNotBeEqualWhenContentDiffers`() {
    val a = ByteArrayValue(byteArrayOf(1, 2, 3))
    val b = ByteArrayValue(byteArrayOf(1, 2, 4))
    assertNotEquals(a, b)
  }

  @Test
  fun `shouldNotBeEqualToNullOrOtherType`() {
    val a = ByteArrayValue(byteArrayOf(1))
    assertNotEquals(a, null)
    assertNotEquals<Any>(a, "string")
  }
}
