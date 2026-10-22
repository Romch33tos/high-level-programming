package domain.repository

import domain.model.User

interface UserRepository {
  fun findUser(login: String): User?
  fun verifyPassword(user: User, password: String): Boolean
}
