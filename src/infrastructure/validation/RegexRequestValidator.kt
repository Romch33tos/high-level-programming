package infrastructure.validation

import application.dto.AccessRequest
import application.dto.AccessResult
import application.validation.RequestValidator

class RegexRequestValidator(
  private val rules: ValidationRules
) : RequestValidator {
  override fun validate(request: AccessRequest): AccessResult? {
    if (!isValidPath(request.resourcePath)) return AccessResult.BadFormat
    if (!isValidVolume(request.volume)) return AccessResult.BadFormat
    return null
  }

  private fun isValidPath(path: String): Boolean {
    if (path.isEmpty()) return false
    val segments = path.split(".")
    if (segments.isEmpty()) return false
    return segments.all { rules.namePattern.matches(it) }
  }

  private fun isValidVolume(volume: Int): Boolean = volume >= rules.minVolume
}
