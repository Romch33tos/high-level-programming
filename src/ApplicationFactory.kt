import application.AccessService
import application.auth.DefaultAuthenticator
import domain.service.EffectiveRuleResolver
import infrastructure.repository.jdbc.JdbcResourceRepository
import infrastructure.repository.jdbc.JdbcRuleRepository
import infrastructure.repository.jdbc.JdbcUserRepository
import infrastructure.security.Sha256PasswordHasher
import infrastructure.validation.RegexRequestValidator
import infrastructure.validation.ValidationRules

object ApplicationFactory {
  fun create(): AccessService {
    val hasher = Sha256PasswordHasher()
    
    return AccessService(
      validator = RegexRequestValidator(ValidationRules()),
      authenticator = DefaultAuthenticator(JdbcUserRepository(hasher)),
      resources = JdbcResourceRepository(),
      rules = JdbcRuleRepository(EffectiveRuleResolver())
    )
  }
}
