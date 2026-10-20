package application.auth

import domain.model.User

sealed interface AuthResult {
  data class Success(val user: User) : AuthResult
  data object BadLogin : AuthResult
  data object BadPassword : AuthResult
}
