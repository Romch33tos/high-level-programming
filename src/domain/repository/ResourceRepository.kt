package domain.repository

import domain.model.Resource

interface ResourceRepository {
  fun findResource(path: String): Resource?
}
