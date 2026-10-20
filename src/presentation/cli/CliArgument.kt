package presentation.cli

enum class CliArgument(val key: String, val required: Boolean) {
  LOGIN("--login", true),
  PASSWORD("--password", true),
  ACTION("--action", true),
  RESOURCE("--resource", true),
  VOLUME("--volume", true),
  HELP("--help", false);

  companion object {
    val requiredKeys: List<String> = entries.filter { it.required }.map { it.key }
  }
}
