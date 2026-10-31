import application.dto.AccessResult
import presentation.cli.AccessResultMapper
import presentation.cli.CliRequestMapper
import presentation.cli.ExitCode
import presentation.cli.HelpPrinter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
    HelpPrinter.print()
    exitProcess(ExitCode.HELP.code)
  }

  val mapper = CliRequestMapper()
  val request = mapper.map(args)

  if (request == null) {
    HelpPrinter.print()
    exitProcess(ExitCode.BAD_FORMAT.code)
  }

  val service = ApplicationFactory.create()
  
  val result: AccessResult = service.check(request)

  exitProcess(AccessResultMapper.toExitCode(result).code)
}
