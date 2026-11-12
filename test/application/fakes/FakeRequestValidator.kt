package application.fakes

import application.dto.AccessRequest
import application.dto.AccessResult
import application.validation.RequestValidator

class FakeRequestValidator(
  var result: AccessResult? = null
) : RequestValidator {
  override fun validate(request: AccessRequest): AccessResult? = result
}
