package application.auth

import application.dto.AccessRequest
import application.fakes.FakeUserRepository
import domain.model.ByteArrayValue
import domain.model.PasswordHash
import domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DefaultAuthenticatorTest {

  private val alice = User(
    login = "alice",
    salt = ByteArrayValue(byteArrayOf(1, 2, 3)),
    passwordHash = PasswordHash(byteArrayOf(4, 5, 6))
  )

  private fun request(login: String = "alice", password: String = "secret") =
    AccessRequest(login, password, "read", "A", 1)

  @Test
  fun `shouldReturnBadLoginWhenUserDoesNotExist`() {
    val repo = FakeUserRepository(users = emptyMap())
    val auth = DefaultAuthenticator(repo)
    assertEquals(AuthResult.BadLogin, auth.authenticate(request()))
  }

  @Test
  fun `shouldReturnBadPasswordWhenPasswordIsIncorrect`() {
    val repo = FakeUserRepository(
      users = mapOf("alice" to alice),
      passwords = mapOf("alice" to "correct")
    )
    val auth = DefaultAuthenticator(repo)
    assertEquals(AuthResult.BadPassword, auth.authenticate(request(password = "wrong")))
  }

  @Test
  fun `shouldReturnSuccessWithUserWhenPasswordIsCorrect`() {
    val repo = FakeUserRepository(
      users = mapOf("alice" to alice),
      passwords = mapOf("alice" to "correct")
    )
    val auth = DefaultAuthenticator(repo)
    val result = auth.authenticate(request(password = "correct"))
    assertTrue(result is AuthResult.Success)
    assertEquals(alice, (result as AuthResult.Success).user)
  }
}
