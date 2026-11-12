package infrastructure.validation

import application.dto.AccessRequest
import application.dto.AccessResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RegexRequestValidatorTest {

  private val validator = RegexRequestValidator(ValidationRules())

  private fun request(
    path: String = "A.B",
    volume: Int = 10
  ) = AccessRequest(
    login = "alice",
    password = "secret",
    action = "read",
    resourcePath = path,
    volume = volume
  )

  @Test
  fun `shouldReturnNullWhenRequestIsValid`() {
    assertNull(validator.validate(request()))
  }

  @Test
  fun `shouldReturnBadFormatWhenPathIsEmpty`() {
    assertEquals(AccessResult.BadFormat, validator.validate(request(path = "")))
  }

  @Test
  fun `shouldReturnBadFormatWhenPathHasEmptySegment`() {
    assertEquals(AccessResult.BadFormat, validator.validate(request(path = "A..B")))
  }

  @Test
  fun `shouldReturnBadFormatWhenPathHasInvalidCharacters`() {
    assertEquals(AccessResult.BadFormat, validator.validate(request(path = "A-B")))
    assertEquals(AccessResult.BadFormat, validator.validate(request(path = "A B")))
  }

  @Test
  fun `shouldReturnBadFormatWhenPathSegmentTooLong`() {
    val path = "a".repeat(21)
    assertEquals(AccessResult.BadFormat, validator.validate(request(path = path)))
  }

  @Test
  fun `shouldReturnBadFormatWhenVolumeIsBelowMinimum`() {
    assertEquals(AccessResult.BadFormat, validator.validate(request(volume = 0)))
    assertEquals(AccessResult.BadFormat, validator.validate(request(volume = -5)))
  }

  @Test
  fun `shouldReturnNullWhenVolumeEqualsMinimum`() {
    assertNull(validator.validate(request(volume = 1)))
  }

  @Test
  fun `shouldRespectCustomNamePattern`() {
    val customRules = ValidationRules(namePattern = Regex("^[a-z]{1,5}$"))
    val customValidator = RegexRequestValidator(customRules)
    assertNull(customValidator.validate(request(path = "abc")))
    assertEquals(AccessResult.BadFormat, customValidator.validate(request(path = "ABC")))
  }
}
