package infrastructure.repository.jdbc

import domain.model.Resource
import domain.repository.ResourceRepository
import java.sql.SQLException

class JdbcResourceRepository : ResourceRepository {

  override fun findResource(path: String): Resource? {
    return null
  }
}
