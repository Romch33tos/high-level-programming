package infrastructure.repository.jdbc

import domain.model.Resource
import domain.repository.ResourceRepository
import java.sql.SQLException

class JdbcResourceRepository : ResourceRepository {

  override fun findResource(path: String): Resource? {
    val sql = "SELECT name, parent_path, max_volume FROM resources"
    try {
      DatabaseConnection.getConnection().use { conn ->
        conn.createStatement().use { stmt ->
          stmt.executeQuery(sql).use { rs ->
            while (rs.next()) {
              val resource = Resource(
                name = rs.getString("name"),
                parentPath = rs.getString("parent_path"),
                maxVolume = rs.getInt("max_volume")
              )
              if (resource.fullPath == path) {
                return resource
              }
            }
          }
        }
      }
    } catch (e: SQLException) {
      throw RuntimeException("Ошибка SQL-запроса при поиске ресурса", e)
    }
    return null
  }
}
