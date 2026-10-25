package domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResourceTest {

  @Test
  fun `shouldReturnNameAsFullPathWhenParentIsEmpty`() {
    val resource = Resource("A", "", 100)
    assertEquals("A", resource.fullPath)
  }

  @Test
  fun `shouldJoinParentAndNameWithDot`() {
    val resource = Resource("C", "A.B", 25)
    assertEquals("A.B.C", resource.fullPath)
  }
}
