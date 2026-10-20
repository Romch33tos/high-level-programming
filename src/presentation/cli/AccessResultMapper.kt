package presentation.cli

import application.dto.AccessResult

object AccessResultMapper {
  fun toExitCode(result: AccessResult): ExitCode = when (result) {
    AccessResult.Success -> ExitCode.SUCCESS
    AccessResult.BadFormat -> ExitCode.BAD_FORMAT
    AccessResult.BadLogin -> ExitCode.BAD_LOGIN
    AccessResult.BadPassword -> ExitCode.BAD_PASSWORD
    AccessResult.UnknownAction -> ExitCode.UNKNOWN_ACTION
    AccessResult.NoResource -> ExitCode.NO_RESOURCE
    AccessResult.NoAccess -> ExitCode.NO_ACCESS
    AccessResult.VolumeExceeded -> ExitCode.VOLUME_EXCEEDED
  }
}
