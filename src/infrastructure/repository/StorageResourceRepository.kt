package infrastructure.repository

import domain.model.Resource
import domain.repository.ResourceRepository
import infrastructure.storage.InMemoryResourceStorage

class StorageResourceRepository(
  private val storage: InMemoryResourceStorage
) : ResourceRepository {
  override fun findResource(path: String): Resource? = storage.findByPath(path)
}
