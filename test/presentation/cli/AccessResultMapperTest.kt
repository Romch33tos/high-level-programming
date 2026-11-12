package presentation.cli

import application.dto.AccessResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccessResultMapperTest {

  @Test
  fun `shouldMapSuccessToSuccessExitCode`() {
    assertEquals(ExitCode.SUCCESS, AccessResultMapper.toExitCode(AccessResult.Success))
  }

  @Test
  fun `shouldMapBadFormatToBadFormatExitCode`() {
    assertEquals(ExitCode.BAD_FORMAT, AccessResultMapper.toExitCode(AccessResult.BadFormat))
  }

  @Test
  fun `shouldMapBadLoginToBadLoginExitCode`() {
    assertEquals(ExitCode.BAD_LOGIN, AccessResultMapper.toExitCode(AccessResult.BadLogin))
  }

  @Test
  fun `shouldMapBadPasswordToBadPasswordExitCode`() {
    assertEquals(ExitCode.BAD_PASSWORD, AccessResultMapper.toExitCode(AccessResult.BadPassword))
  }

  @Test
  fun `shouldMapUnknownActionToUnknownActionExitCode`() {
    assertEquals(ExitCode.UNKNOWN_ACTION, AccessResultMapper.toExitCode(AccessResult.UnknownAction))
  }

  @Test
  fun `shouldMapNoResourceToNoResourceExitCode`() {
    assertEquals(ExitCode.NO_RESOURCE, AccessResultMapper.toExitCode(AccessResult.NoResource))
  }

  @Test
  fun `shouldMapNoAccessToNoAccessExitCode`() {
    assertEquals(ExitCode.NO_ACCESS, AccessResultMapper.toExitCode(AccessResult.NoAccess))
  }

  @Test
  fun `shouldMapVolumeExceededToVolumeExceededExitCode`() {
    assertEquals(ExitCode.VOLUME_EXCEEDED, AccessResultMapper.toExitCode(AccessResult.VolumeExceeded))
  }
}
