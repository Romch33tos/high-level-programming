package infrastructure.utils

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class HexTest {

  @Test
  fun `shouldParseHexStringToBytes`() {
    assertArrayEquals(byteArrayOf(0x01, 0xAB.toByte(), 0xFF.toByte()), "01ABFF".hexToBytes())
  }

  @Test
  fun `shouldParseEmptyStringToEmptyArray`() {
    assertArrayEquals(ByteArray(0), "".hexToBytes())
  }

  @Test
  fun `shouldParseLowercaseHex`() {
    assertArrayEquals(byteArrayOf(0x0A, 0x0B), "0a0b".hexToBytes())
  }

  @Test
  fun `shouldParseSingleByte`() {
    assertArrayEquals(byteArrayOf(0x7F), "7f".hexToBytes())
  }
}
