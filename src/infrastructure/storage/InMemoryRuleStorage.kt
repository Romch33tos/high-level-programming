package infrastructure.storage

import domain.model.AccessRule

class InMemoryRuleStorage(private val rules: List<AccessRule>) {
  fun findAll(): List<AccessRule> = rules
}
