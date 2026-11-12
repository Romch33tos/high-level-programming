package application.fakes

import domain.model.AccessRule
import domain.repository.RuleRepository

class FakeRuleRepository(
  private val rules: Map<Pair<String, String>, AccessRule> = emptyMap()
) : RuleRepository {
  override fun findEffectiveRule(login: String, resourcePath: String): AccessRule? =
    rules[login to resourcePath]
}
