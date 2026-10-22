package presentation.cli

class CliParser {
  fun parse(args: Array<String>): Map<String, String>? {
    val result = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
      val key = args[i]
      if (!key.startsWith("--")) return null
      if (i + 1 >= args.size) return null
      val value = args[i + 1]
      if (value.startsWith("--")) return null
      result[key] = value
      i += 2
    }
    return result
  }
}
