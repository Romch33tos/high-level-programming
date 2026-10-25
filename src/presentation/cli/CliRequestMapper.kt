package presentation.cli

import application.dto.AccessRequest
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required

class CliRequestMapper {
  fun map(args: Array<String>): AccessRequest? {
    val parser = ArgParser("app", prefixStyle = kotlinx.cli.PrefixStyle.Long)
    parser.allowStructuredArgs = false

    val login by parser.arg(ArgType.String, fullName = "login", shortName = "l", description = "Логин пользователя").required()
    val password by parser.arg(ArgType.String, fullName = "password", shortName = "p", description = "Пароль").required()
    val action by parser.arg(ArgType.String, fullName = "action", shortName = "a", description = "Действие").required()
    val resource by parser.arg(ArgType.String, fullName = "resource", shortName = "r", description = "Путь до ресурса").required()
    val volume by parser.arg(ArgType.Int, fullName = "volume", shortName = "v", description = "Объём").required()

    val help by parser.arg(ArgType.Boolean, fullName = "help", shortName = "h", description = "Показать справку").default(false)

    try {
      parser.parse(args)
    } catch (e: Exception) {
      return null
    }

    if (help) {
      return null
    }

    return AccessRequest(
      login = login,
      password = password,
      action = action,
      resourcePath = resource,
      volume = volume
    )
  }
}
