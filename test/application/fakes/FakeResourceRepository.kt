package application.fakes

import domain.model.Resource
import domain.repository.ResourceRepository

class FakeResourceRepository(
  private val resources: Map<String, Resource> = emptyMap()
) : ResourceRepository {
  override fun findResource(path: String): Resource? = resources[path]
}
