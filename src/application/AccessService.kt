package application

import application.auth.AuthResult
import application.auth.Authenticator
import application.dto.AccessRequest
import application.dto.AccessResult
import application.validation.RequestValidator
import domain.model.Action
import domain.repository.ResourceRepository
import domain.repository.RuleRepository

class AccessService(
  private val validator: RequestValidator,
  private val authenticator: Authenticator,
  private val resources: ResourceRepository,
  private val rules: RuleRepository
) {
  fun check(request: AccessRequest): AccessResult {
    validator.validate(request)?.let { return it }

    val user = when (val authResult = authenticator.authenticate(request)) {
      is AuthResult.Success -> authResult.user
      AuthResult.BadLogin -> return AccessResult.BadLogin
      AuthResult.BadPassword -> return AccessResult.BadPassword
    }

    val action = Action.from(request.action) ?: return AccessResult.UnknownAction

    val resource = resources.findResource(request.resourcePath) ?: return AccessResult.NoResource
    val rule = rules.findEffectiveRule(request.login, request.resourcePath)
      ?: return AccessResult.NoAccess

    if (!action.isPermittedBy(rule)) return AccessResult.NoAccess
    if (request.volume > resource.maxVolume) return AccessResult.VolumeExceeded

    return AccessResult.Success
  }
}
