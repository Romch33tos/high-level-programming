package domain.repository

import domain.model.AccessRule

interface RuleRepository {
  fun findEffectiveRule(login: String, resourcePath: String): AccessRule?
}
