package presentation.cli

import application.dto.AccessRequest

class CliRequestMapper {
  fun map(arguments: Map<String, String>): AccessRequest? {
    val volume = arguments[CliArgument.VOLUME.key]?.toIntOrNull() ?: return null
    return AccessRequest(
      login = arguments[CliArgument.LOGIN.key] ?: return null,
      password = arguments[CliArgument.PASSWORD.key] ?: return null,
      action = arguments[CliArgument.ACTION.key] ?: return null,
      resourcePath = arguments[CliArgument.RESOURCE.key] ?: return null,
      volume = volume
    )
  }
}
