package application.validation

import application.dto.AccessRequest
import application.dto.AccessResult

interface RequestValidator {
  fun validate(request: AccessRequest): AccessResult?
}
