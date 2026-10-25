package domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResourcePathTest {

  @Test
  fun `shouldParseSingleSegmentPath`() {
    val path = ResourcePath.parse("A")
    assertNotNull(path)
    assertEquals("A", path!!.value)
  }

  @Test
  fun `shouldParseMultiSegmentPath`() {
    val path = ResourcePath.parse("A.B.C")
    assertNotNull(path)
    assertEquals(listOf("A", "B", "C"), path!!.segments)
  }

  @Test
  fun `shouldReturnNullWhenPathIsEmpty`() {
    assertNull(ResourcePath.parse(""))
  }

  @Test
  fun `shouldReturnNullWhenSegmentIsEmpty`() {
    assertNull(ResourcePath.parse("A..B"))
    assertNull(ResourcePath.parse(".A"))
    assertNull(ResourcePath.parse("A."))
  }

  @Test
  fun `shouldReturnNullWhenSegmentContainsInvalidCharacters`() {
    assertNull(ResourcePath.parse("A-B"))
    assertNull(ResourcePath.parse("A B"))
    assertNull(ResourcePath.parse("A/B"))
    assertNull(ResourcePath.parse("Кириллица"))
  }

  @Test
  fun `shouldReturnNullWhenSegmentIsTooLong`() {
    val longSegment = "a".repeat(21)
    assertNull(ResourcePath.parse(longSegment))
  }

  @Test
  fun `shouldParseSegmentWithMaxAllowedLength`() {
    val segment = "a".repeat(20)
    assertNotNull(ResourcePath.parse(segment))
  }

  @Test
  fun `shouldReturnParentSegments`() {
    val path = ResourcePath.parse("A.B.C")!!
    assertEquals(listOf("A", "B"), path.parentSegments())
  }

  @Test
  fun `shouldReturnEmptyParentSegmentsForSingleSegment`() {
    val path = ResourcePath.parse("A")!!
    assertTrue(path.parentSegments().isEmpty())
  }

  @Test
  fun `shouldReportPrefixWhenPathIsPrefixOfOther`() {
    val prefix = ResourcePath.parse("A.B")!!
    val other = ResourcePath.parse("A.B.C")!!
    assertTrue(prefix.isPrefixOf(other))
  }

  @Test
  fun `shouldReportPrefixWhenPathsAreEqual`() {
    val a = ResourcePath.parse("A.B")!!
    val b = ResourcePath.parse("A.B")!!
    assertTrue(a.isPrefixOf(b))
  }

  @Test
  fun `shouldNotReportPrefixWhenPathIsLonger`() {
    val longer = ResourcePath.parse("A.B.C")!!
    val shorter = ResourcePath.parse("A.B")!!
    assertFalse(longer.isPrefixOf(shorter))
  }

  @Test
  fun `shouldNotReportPrefixWhenPathsAreUnrelated`() {
    val a = ResourcePath.parse("A.B")!!
    val b = ResourcePath.parse("A.D")!!
    assertFalse(a.isPrefixOf(b))
  }

  @Test
  fun `shouldCompareByValue`() {
    val a = ResourcePath.parse("A.B")!!
    val b = ResourcePath.parse("A.B")!!
    val c = ResourcePath.parse("A.C")!!
    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
    assertNotEquals(a, c)
  }

  @Test
  fun `shouldReturnValueAsToString`() {
    assertEquals("A.B.C", ResourcePath.parse("A.B.C")!!.toString())
  }

  @Test
  fun `shouldCreatePathFromValidatedValue`() {
    val path = ResourcePath.fromValidated("A.B")
    assertEquals("A.B", path.value)
  }
}
