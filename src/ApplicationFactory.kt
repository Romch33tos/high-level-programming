import application.AccessService
import application.auth.DefaultAuthenticator
import infrastructure.repository.StorageResourceRepository
import infrastructure.repository.StorageRuleRepository
import infrastructure.repository.StorageUserRepository
import infrastructure.security.Sha256PasswordHasher
import infrastructure.storage.InMemoryResourceStorage
import infrastructure.storage.InMemoryRuleStorage
import infrastructure.storage.InMemoryUserStorage
import infrastructure.storage.Storage
import infrastructure.validation.RegexRequestValidator
import infrastructure.validation.ValidationRules
import domain.service.EffectiveRuleResolver

object ApplicationFactory {
  fun create(): AccessService {
    val hasher = Sha256PasswordHasher()
    val userStorage = InMemoryUserStorage(Storage.users)
    val resourceStorage = InMemoryResourceStorage(Storage.resources)
    val ruleStorage = InMemoryRuleStorage(Storage.accessRules)

    return AccessService(
      validator = RegexRequestValidator(ValidationRules()),
      authenticator = DefaultAuthenticator(StorageUserRepository(userStorage, hasher)),
      resources = StorageResourceRepository(resourceStorage),
      rules = StorageRuleRepository(ruleStorage, EffectiveRuleResolver())
    )
  }
}
