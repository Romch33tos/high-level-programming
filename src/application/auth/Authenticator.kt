package application.auth

import application.dto.AccessRequest
import domain.repository.UserRepository

interface Authenticator {
  fun authenticate(request: AccessRequest): AuthResult
}

class DefaultAuthenticator(
  private val users: UserRepository
) : Authenticator {
  override fun authenticate(request: AccessRequest): AuthResult {
    val user = users.findUser(request.login) ?: return AuthResult.BadLogin
    return if (users.verifyPassword(user, request.password)) {
      AuthResult.Success(user)
    } else {
      AuthResult.BadPassword
    }
  }
}
