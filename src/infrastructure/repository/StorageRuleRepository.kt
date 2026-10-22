package infrastructure.repository

import domain.model.AccessRule
import domain.model.ResourcePath
import domain.repository.RuleRepository
import domain.service.EffectiveRuleResolver
import infrastructure.storage.InMemoryRuleStorage

class StorageRuleRepository(
  private val storage: InMemoryRuleStorage,
  private val resolver: EffectiveRuleResolver
) : RuleRepository {
  override fun findEffectiveRule(login: String, resourcePath: String): AccessRule? {
    val path = ResourcePath.parse(resourcePath) ?: return null
    return resolver.resolve(storage.findAll(), login, path)
  }
}
