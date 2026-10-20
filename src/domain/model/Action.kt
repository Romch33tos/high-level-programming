package domain.model

enum class Action(private val check: (AccessRule) -> Boolean) {
  READ({ it.canRead }),
  WRITE({ it.canWrite }),
  EXECUTE({ it.canExecute });

  fun isPermittedBy(rule: AccessRule): Boolean = check(rule)

  companion object {
    fun from(value: String): Action? =
      entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
  }
}
