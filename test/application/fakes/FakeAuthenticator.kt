package application.fakes

import application.auth.AuthResult
import application.auth.Authenticator
import application.dto.AccessRequest

class FakeAuthenticator(
  var result: AuthResult = AuthResult.BadLogin
) : Authenticator {
  override fun authenticate(request: AccessRequest): AuthResult = result
}
