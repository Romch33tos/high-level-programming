package infrastructure.repository.jdbc

import domain.model.AccessRule
import domain.model.ResourcePath
import domain.repository.RuleRepository
import domain.service.EffectiveRuleResolver
import java.sql.SQLException

class JdbcRuleRepository(
  private val resolver: EffectiveRuleResolver
) : RuleRepository {

  override fun findEffectiveRule(login: String, resourcePath: String): AccessRule? {
    return null
  }
}
