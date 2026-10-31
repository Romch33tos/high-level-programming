import application.dto.AccessResult
import infrastructure.repository.jdbc.DatabaseConnectionException
import infrastructure.repository.jdbc.DatabaseQueryException
import presentation.cli.AccessResultMapper
import presentation.cli.CliParser
import presentation.cli.CliRequestMapper
import presentation.cli.ExitCode
import presentation.cli.HelpPrinter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
    HelpPrinter.print()
    exitProcess(ExitCode.HELP.code)
  }

  val parser = CliParser()
  val argsMap = parser.parse(args)

  if (argsMap == null) {
    HelpPrinter.print()
    exitProcess(ExitCode.BAD_FORMAT.code)
  }

  val mapper = CliRequestMapper()
  val request = mapper.map(argsMap)

  if (request == null) {
    HelpPrinter.print()
    exitProcess(ExitCode.BAD_FORMAT.code)
  }

  val service = ApplicationFactory.create()

  val result: AccessResult = try {
    service.check(request)
  } catch (e: DatabaseConnectionException) {
    exitProcess(9)
  } catch (e: DatabaseQueryException) {
    exitProcess(10)
  }

  exitProcess(AccessResultMapper.toExitCode(result).code)
}
