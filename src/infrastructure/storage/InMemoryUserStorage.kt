package infrastructure.storage

import domain.model.User

class InMemoryUserStorage(private val users: List<User>) {
  fun findAll(): List<User> = users

  fun findByLogin(login: String): User? = users.firstOrNull { it.login == login }
}
