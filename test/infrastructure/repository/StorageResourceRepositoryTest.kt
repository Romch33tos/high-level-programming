package infrastructure.repository

import domain.model.Resource
import infrastructure.storage.InMemoryResourceStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class StorageResourceRepositoryTest {

  private val storage = InMemoryResourceStorage(
    listOf(
      Resource("A", "", 100),
      Resource("C", "A.B", 25)
    )
  )
  private val repo = StorageResourceRepository(storage)

  @Test
  fun `shouldFindResourceByFullPath`() {
    assertEquals("A", repo.findResource("A")?.name)
    assertEquals("C", repo.findResource("A.B.C")?.name)
  }

  @Test
  fun `shouldReturnNullWhenResourceDoesNotExist`() {
    assertNull(repo.findResource("X.Y"))
  }
}
