package application.fakes

import domain.model.User
import domain.repository.UserRepository

class FakeUserRepository(
  private val users: Map<String, User> = emptyMap(),
  private val passwords: Map<String, String> = emptyMap()
) : UserRepository {
  override fun findUser(login: String): User? = users[login]

  override fun verifyPassword(user: User, password: String): Boolean =
    passwords[user.login] == password
}
