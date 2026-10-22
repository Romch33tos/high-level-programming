package presentation.cli

enum class ExitCode(val code: Int) {
  SUCCESS(0),
  HELP(1),
  BAD_PASSWORD(2),
  BAD_LOGIN(3),
  UNKNOWN_ACTION(4),
  NO_ACCESS(5),
  NO_RESOURCE(6),
  BAD_FORMAT(7),
  VOLUME_EXCEEDED(8)
}
