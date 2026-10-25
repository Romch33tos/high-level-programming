package domain.service

import domain.model.AccessRule
import domain.model.ResourcePath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EffectiveRuleResolverTest {

  private val resolver = EffectiveRuleResolver()

  private fun rule(login: String, path: String, read: Boolean = true) =
    AccessRule(login, path, read, false, false)

  @Test
  fun `shouldReturnNullWhenRulesAreEmpty`() {
    val path = ResourcePath.parse("A.B.C")!!
    assertNull(resolver.resolve(emptyList(), "alice", path))
  }

  @Test
  fun `shouldReturnNullWhenNoRuleMatchesLogin`() {
    val rules = listOf(rule("bob", "A"))
    val path = ResourcePath.parse("A")!!
    assertNull(resolver.resolve(rules, "alice", path))
  }

  @Test
  fun `shouldReturnNullWhenNoRuleMatchesAnyPrefix`() {
    val rules = listOf(rule("alice", "B"))
    val path = ResourcePath.parse("A.B.C")!!
    assertNull(resolver.resolve(rules, "alice", path))
  }

  @Test
  fun `shouldReturnMostSpecificRule`() {
    val rules = listOf(
      rule("alice", "A", read = true),
      rule("alice", "A.B", read = false),
      rule("alice", "A.B.C", read = true)
    )
    val path = ResourcePath.parse("A.B.C")!!
    val result = resolver.resolve(rules, "alice", path)
    assertEquals("A.B.C", result?.resourcePath)
  }

  @Test
  fun `shouldFallbackToParentRuleWhenExactMissing`() {
    val rules = listOf(
      rule("alice", "A", read = true),
      rule("alice", "A.B", read = false)
    )
    val path = ResourcePath.parse("A.B.C")!!
    val result = resolver.resolve(rules, "alice", path)
    assertEquals("A.B", result?.resourcePath)
  }

  @Test
  fun `shouldFallbackToRootRuleWhenOnlyRootExists`() {
    val rules = listOf(rule("alice", "A", read = true))
    val path = ResourcePath.parse("A.B.C")!!
    val result = resolver.resolve(rules, "alice", path)
    assertEquals("A", result?.resourcePath)
  }

  @Test
  fun `shouldMatchSingleSegmentPathExactly`() {
    val rules = listOf(rule("alice", "A", read = false))
    val path = ResourcePath.parse("A")!!
    val result = resolver.resolve(rules, "alice", path)
    assertEquals("A", result?.resourcePath)
  }

  @Test
  fun `shouldIgnoreRulesOfOtherUsers`() {
    val rules = listOf(
      rule("bob", "A.B.C", read = true),
      rule("alice", "A", read = false)
    )
    val path = ResourcePath.parse("A.B.C")!!
    val result = resolver.resolve(rules, "alice", path)
    assertEquals("A", result?.resourcePath)
  }
}
