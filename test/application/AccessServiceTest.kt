package application

import application.auth.AuthResult
import application.dto.AccessRequest
import application.dto.AccessResult
import application.fakes.FakeAuthenticator
import application.fakes.FakeRequestValidator
import application.fakes.FakeResourceRepository
import application.fakes.FakeRuleRepository
import domain.model.AccessRule
import domain.model.ByteArrayValue
import domain.model.PasswordHash
import domain.model.Resource
import domain.model.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccessServiceTest {

  private val alice = User(
    login = "alice",
    salt = ByteArrayValue(byteArrayOf(1)),
    passwordHash = PasswordHash(byteArrayOf(2))
  )

  private val resourceA = Resource("A", "", 100)
  private val ruleAllAllow = AccessRule("alice", "A", true, true, true)

  private fun request(
    login: String = "alice",
    action: String = "read",
    path: String = "A",
    volume: Int = 10
  ) = AccessRequest(login, "secret", action, path, volume)

  private fun service(
    validatorResult: AccessResult? = null,
    authResult: AuthResult = AuthResult.Success(alice),
    resources: Map<String, Resource> = mapOf("A" to resourceA),
    rules: Map<Pair<String, String>, AccessRule> = mapOf(("alice" to "A") to ruleAllAllow)
  ) = AccessService(
    validator = FakeRequestValidator(validatorResult),
    authenticator = FakeAuthenticator(authResult),
    resources = FakeResourceRepository(resources),
    rules = FakeRuleRepository(rules)
  )

  @Test
  fun `shouldReturnValidatorResultWhenValidationFails`() {
    val service = service(validatorResult = AccessResult.BadFormat)
    assertEquals(AccessResult.BadFormat, service.check(request()))
  }

  @Test
  fun `shouldReturnBadLoginWhenAuthenticationReturnsBadLogin`() {
    val service = service(authResult = AuthResult.BadLogin)
    assertEquals(AccessResult.BadLogin, service.check(request()))
  }

  @Test
  fun `shouldReturnBadPasswordWhenAuthenticationReturnsBadPassword`() {
    val service = service(authResult = AuthResult.BadPassword)
    assertEquals(AccessResult.BadPassword, service.check(request()))
  }

  @Test
  fun `shouldReturnUnknownActionWhenActionIsInvalid`() {
    val service = service()
    assertEquals(AccessResult.UnknownAction, service.check(request(action = "delete")))
  }

  @Test
  fun `shouldReturnNoResourceWhenResourceDoesNotExist`() {
    val service = service(resources = emptyMap())
    assertEquals(AccessResult.NoResource, service.check(request()))
  }

  @Test
  fun `shouldReturnNoAccessWhenNoRuleExists`() {
    val service = service(rules = emptyMap())
    assertEquals(AccessResult.NoAccess, service.check(request()))
  }

  @Test
  fun `shouldReturnNoAccessWhenActionIsNotPermitted`() {
    val readOnlyRule = AccessRule("alice", "A", canRead = true, canWrite = false, canExecute = false)
    val service = service(rules = mapOf(("alice" to "A") to readOnlyRule))
    assertEquals(AccessResult.NoAccess, service.check(request(action = "write")))
  }

  @Test
  fun `shouldReturnVolumeExceededWhenVolumeIsGreaterThanMax`() {
    val service = service()
    assertEquals(AccessResult.VolumeExceeded, service.check(request(volume = 101)))
  }

  @Test
  fun `shouldReturnSuccessWhenAllChecksPass`() {
    val service = service()
    assertEquals(AccessResult.Success, service.check(request(volume = 50)))
  }

  @Test
  fun `shouldReturnVolumeExceededWhenVolumeEqualsMaxPlusOne`() {
    val service = service()
    assertEquals(AccessResult.VolumeExceeded, service.check(request(volume = 101)))
  }

  @Test
  fun `shouldReturnSuccessWhenVolumeEqualsMax`() {
    val service = service()
    assertEquals(AccessResult.Success, service.check(request(volume = 100)))
  }
}
