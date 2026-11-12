import application.dto.AccessResult
import presentation.cli.AccessResultMapper
import presentation.cli.CliArgument
import presentation.cli.CliParser
import presentation.cli.CliRequestMapper
import presentation.cli.ExitCode
import presentation.cli.HelpPrinter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    HelpPrinter.print()
    exitProcess(ExitCode.HELP.code)
  }
  if (args.contains("-h") || args.contains(CliArgument.HELP.key)) {
    HelpPrinter.print()
    exitProcess(ExitCode.HELP.code)
  }

  val parser = CliParser()
  val parsed = parser.parse(args) ?: run {
    HelpPrinter.print()
    exitProcess(ExitCode.BAD_FORMAT.code)
  }

  val hasAllRequired = CliArgument.requiredKeys.all { key -> !parsed[key].isNullOrBlank() }
  if (!hasAllRequired) {
    HelpPrinter.print()
    exitProcess(ExitCode.BAD_FORMAT.code)
  }

  val request = CliRequestMapper().map(parsed) ?: run {
    HelpPrinter.print()
    exitProcess(ExitCode.BAD_FORMAT.code)
  }

  val service = ApplicationFactory.create()
  val result: AccessResult = service.check(request)
  exitProcess(AccessResultMapper.toExitCode(result).code)
}
