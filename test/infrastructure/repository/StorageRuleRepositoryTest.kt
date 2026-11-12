package infrastructure.repository

import domain.model.AccessRule
import domain.service.EffectiveRuleResolver
import infrastructure.storage.InMemoryRuleStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class StorageRuleRepositoryTest {

  private val rules = listOf(
    AccessRule("alice", "A", true, false, false),
    AccessRule("alice", "A.B", false, true, false)
  )
  private val repo = StorageRuleRepository(InMemoryRuleStorage(rules), EffectiveRuleResolver())

  @Test
  fun `shouldReturnMostSpecificRule`() {
    assertEquals("A.B", repo.findEffectiveRule("alice", "A.B")?.resourcePath)
  }

  @Test
  fun `shouldFallbackToParentRule`() {
    assertEquals("A.B", repo.findEffectiveRule("alice", "A.B.C")?.resourcePath)
  }

  @Test
  fun `shouldReturnNullWhenPathIsInvalid`() {
    assertNull(repo.findEffectiveRule("alice", "A..B"))
  }

  @Test
  fun `shouldReturnNullWhenNoRuleMatches`() {
    assertNull(repo.findEffectiveRule("bob", "A"))
  }
}
