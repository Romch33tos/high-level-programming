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
    val path = ResourcePath.parse(resourcePath) ?: return null
    val rules = mutableListOf<AccessRule>()

    val sql = "SELECT login, resource_path, can_read, can_write, can_execute FROM access_rules WHERE login = ?"
    try {
      DatabaseConnection.getConnection().use { conn ->
        conn.prepareStatement(sql).use { stmt ->
          stmt.setString(1, login)
          stmt.executeQuery().use { rs ->
            while (rs.next()) {
              rules.add(
                AccessRule(
                  login = rs.getString("login"),
                  resourcePath = rs.getString("resource_path"),
                  canRead = rs.getBoolean("can_read"),
                  canWrite = rs.getBoolean("can_write"),
                  canExecute = rs.getBoolean("can_execute")
                )
              )
            }
          }
        }
      }
    } catch (e: SQLException) {
      throw RuntimeException("Ошибка SQL-запроса при поиске правил", e)
    }

    return resolver.resolve(rules, login, path)
  }
}
