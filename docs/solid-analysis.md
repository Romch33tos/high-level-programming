# Анализ нарушений принципов SOLID

## S — Single Responsibility Principle

> У класса должна быть только одна причина для изменения.

### S1. `Storage` совмещает хранение, поиск и криптографию

**Место:** `Storage.kt` → `object Storage`

`Storage` выполняет три независимые роли:

1. Хранение данных в памяти (`users`, `resources`, `accessRules`).
2. Поиск (`findUser`, `findResource`, `findEffectiveRule`).
3. Криптография (`hashPassword`).

```kotlin
object Storage {
    val users: List<User> = listOf(/* ... */)
    val resources: List<Resource> = listOf(/* ... */)
    val accessRules: List<AccessRule> = listOf(/* ... */)

    fun findUser(login: String): User? = users.firstOrNull { it.login == login }
    fun findResource(path: String): Resource? = /* ... */
    fun findEffectiveRule(login: String, resourcePath: String): AccessRule? = /* ... */

    fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
}
```

Любое изменение одной из ролей (переход на БД, смена алгоритма хеширования, изменение правил поиска) потребует правки одного и того же класса.

**Решение.** Разделить на отдельные компоненты:

- `InMemoryUserStorage`, `InMemoryResourceStorage`, `InMemoryRuleStorage` — только хранение.
- `StorageXxxRepository` (уже существуют) — поиск поверх хранилища.
- `Sha256PasswordHasher` — реализация интерфейса `PasswordHasher`.

```kotlin
// domain/repository/PasswordHasher.kt
interface PasswordHasher {
    fun hash(password: String, salt: ByteArray): ByteArray
}

// infrastructure/security/Sha256PasswordHasher.kt
class Sha256PasswordHasher : PasswordHasher {
    override fun hash(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
}
```

### S2. `AccessService.check` выполняет слишком много обязанностей

**Место:** `AccessService.kt` → `AccessService.check()`

Метод `check()` одновременно:

1. Валидирует формат запроса.
2. Аутентифицирует пользователя.
3. Парсит действие.
4. Ищет ресурс.
5. Ищет эффективное правило.
6. Проверяет права по действию.
7. Проверяет объём.

Семь причин для изменения — явное нарушение SRP.

```kotlin
fun check(request: AccessRequest): Int {
    if (!ResourceValidator.isValidPath(request.resourcePath) ||
        !ResourceValidator.isValidVolume(request.volume)
    ) return EXIT_BAD_FORMAT

    val user = users.findUser(request.login) ?: return EXIT_BAD_LOGIN
    if (!users.verifyPassword(user, request.password)) return EXIT_BAD_PASSWORD

    val action = Action.from(request.action) ?: return EXIT_UNKNOWN_ACTION

    val resource = resources.findResource(request.resourcePath) ?: return EXIT_NO_RESOURCE
    val rule = rules.findEffectiveRule(request.login, request.resourcePath)
        ?: return EXIT_NO_ACCESS

    val permitted = when (action) {
        Action.READ -> rule.canRead
        Action.WRITE -> rule.canWrite
        Action.EXECUTE -> rule.canExecute
    }
    if (!permitted) return EXIT_NO_ACCESS

    if (request.volume > resource.maxVolume) return EXIT_VOLUME_EXCEEDED

    return EXIT_SUCCESS
}
```

**Решение.** Разбить на компоненты с единственной ответственностью:

- `RequestValidator` — валидация запроса.
- `Authenticator` — аутентификация.
- `ResourceFinder` — поиск ресурса.
- `AccessResolver` — определение правила и проверка прав.
- `AccessService` — тонкий оркестратор, вызывающий их по цепочке.

Здесь важно сразу согласовать контракты: бизнесовый результат должен быть внутри приложения, а системный код возврата — только в CLI-слое. Поэтому компоненты возвращают не `Int`/`ExitCode`, а доменный `AccessResult`:

```kotlin
// domain/result/AccessResult.kt
sealed interface AccessResult {
    data object Success : AccessResult
    data object BadFormat : AccessResult
    data object BadLogin : AccessResult
    data object BadPassword : AccessResult
    data object UnknownAction : AccessResult
    data object NoResource : AccessResult
    data object NoAccess : AccessResult
    data object VolumeExceeded : AccessResult
}

// application/AccessService.kt
class AccessService(
    private val validator: RequestValidator,
    private val authenticator: Authenticator,
    private val resourceFinder: ResourceFinder,
    private val accessResolver: AccessResolver
) {
    fun check(request: AccessRequest): AccessResult {
        validator.validate(request)?.let { return it }

        val user = when (val authResult = authenticator.authenticate(request)) {
            is AuthResult.Success -> authResult.user
            AuthResult.BadLogin -> return AccessResult.BadLogin
            AuthResult.BadPassword -> return AccessResult.BadPassword
        }

        val resource = resourceFinder.find(request.resourcePath) ?: return AccessResult.NoResource
        return accessResolver.resolve(user, resource, request)
    }
}
```

Обратите внимание: аутентификатор больше не «схлопывает» отсутствие пользователя и неверный пароль в один `null` — см. исправление в разделе D1.

```kotlin
// application/auth/AuthResult.kt
sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data object BadLogin : AuthResult
    data object BadPassword : AuthResult
}

// application/auth/Authenticator.kt
interface Authenticator {
    fun authenticate(request: AccessRequest): AuthResult
}

class DefaultAuthenticator(
    private val users: UserRepository
) : Authenticator {
    override fun authenticate(request: AccessRequest): AuthResult {
        val user = users.findUser(request.login) ?: return AuthResult.BadLogin
        return if (users.verifyPassword(user, request.password)) {
            AuthResult.Success(user)
        } else {
            AuthResult.BadPassword
        }
    }
}
```

### S3. `Main.main` совмещает парсинг, валидацию и сборку зависимостей

**Место:** `Main.kt` → `fun main()`

Функция `main()` выполняет пять разных задач:

```kotlin
fun main(args: Array<String>) {
    if (args.isEmpty()) { printHelp(); exitProcess(EXIT_HELP) }
    if (args.contains("-h") || args.contains("--help")) { /* ... */ }

    val map = parseKeyValues(args)
    if (map == null) { printHelp(); exitProcess(EXIT_BAD_FORMAT) }

    for (key in REQUIRED_KEYS) {
        if (map[key].isNullOrBlank()) { printHelp(); exitProcess(EXIT_BAD_FORMAT) }
    }
    val volume = map["--volume"]!!.toIntOrNull() ?: run { /* ... */ }

    val service = AccessService(
        users = StorageUserRepository(),
        resources = StorageResourceRepository(),
        rules = StorageRuleRepository()
    )

    val code = service.check(AccessRequest(/* ... */))
    kotlin.system.exitProcess(code)
}
```

**Решение.** Разделить на четыре компонента, оставив `main()` тонким. При этом `main()` получает от сервиса `AccessResult` и только здесь, в CLI-слое, переводит его в системный код возврата:

```kotlin
fun main(args: Array<String>) {
    val parser = CliArgumentParser()
    val validator = CliArgumentValidator()

    val parsed = parser.parse(args) ?: run { HelpPrinter.print(); exitProcess(ExitCode.BAD_FORMAT.code) }
    val request = validator.validate(parsed) ?: run { HelpPrinter.print(); exitProcess(ExitCode.BAD_FORMAT.code) }

    val service = ApplicationFactory.create()
    exitProcess(AccessResultMapper.toExitCode(service.check(request)).code)
}
```

### S4. `Cli.kt` совмещает константы, парсер и справку

**Место:** `Cli.kt`

Файл содержит четыре категории кода:

```kotlin
// 1. Константы кодов возврата
const val EXIT_SUCCESS = 0
const val EXIT_HELP = 1
// ... и ещё 7 констант

// 2. Список обязательных ключей
val REQUIRED_KEYS = listOf("--login", "--password", "--action", "--resource", "--volume")

// 3. Парсер
fun parseKeyValues(args: Array<String>): Map<String, String>? { /* ... */ }

// 4. Вывод справки
fun printHelp() { /* ... */ }
```

Изменение формата справки, парсера или кодов возврата — три независимые причины для правки одного файла.

**Решение.** Разделить на три файла. `ExitCode` остаётся в CLI-слое как системное представление результата, а бизнесовый `AccessResult` живёт в домене:

```kotlin
// presentation/cli/ExitCode.kt
enum class ExitCode(val code: Int) {
    SUCCESS(0), HELP(1), BAD_PASSWORD(2), BAD_LOGIN(3),
    UNKNOWN_ACTION(4), NO_ACCESS(5), NO_RESOURCE(6),
    BAD_FORMAT(7), VOLUME_EXCEEDED(8)
}

// presentation/cli/AccessResultMapper.kt
object AccessResultMapper {
    fun toExitCode(result: AccessResult): ExitCode = when (result) {
        AccessResult.Success -> ExitCode.SUCCESS
        AccessResult.BadFormat -> ExitCode.BAD_FORMAT
        AccessResult.BadLogin -> ExitCode.BAD_LOGIN
        AccessResult.BadPassword -> ExitCode.BAD_PASSWORD
        AccessResult.UnknownAction -> ExitCode.UNKNOWN_ACTION
        AccessResult.NoResource -> ExitCode.NO_RESOURCE
        AccessResult.NoAccess -> ExitCode.NO_ACCESS
        AccessResult.VolumeExceeded -> ExitCode.VOLUME_EXCEEDED
    }
}

// presentation/cli/CliParser.kt
class CliParser {
    fun parse(args: Array<String>): Map<String, String>? { /* ... */ }
}

// presentation/cli/HelpPrinter.kt
object HelpPrinter { fun print() { /* ... */ } }
```

### S5. `User` совмещает данные и логику сравнения `ByteArray`

**Место:** `Models.kt` → `class User`

Класс хранит доменные данные (`login`, `salt`, `passwordHash`) и одновременно содержит кастомную реализацию `equals`/`hashCode` исключительно ради корректного сравнения `ByteArray`:

```kotlin
class User(
    val login: String,
    val salt: ByteArray,
    val passwordHash: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return login == other.login &&
            salt.contentEquals(other.salt) &&
            passwordHash.contentEquals(other.passwordHash)
    }

    override fun hashCode(): Int { /* ... */ }
}
```

Это смешение двух ролей: «модель пользователя» и «сравнение бинарных данных».

**Решение.** Ввести обычный (не `value`) класс-обёртку `PasswordHash`, который сам умеет сравниваться. `value class` здесь не подходит: Kotlin не разрешает `value class` переопределять `equals`/`hashCode`, а автоматически сгенерированное сравнение для `ByteArray` сравнивает ссылки, а не содержимое. Кроме того, `private val bytes` в `value class` всё равно доступен снаружи через сгенерированный геттер, поэтому инкапсуляции он не даёт.

```kotlin
// domain/model/PasswordHash.kt
class PasswordHash(private val bytes: ByteArray) {
    fun matches(other: ByteArray): Boolean = bytes.contentEquals(other)
    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean =
        other is PasswordHash && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()
}
```

`User` при этом остаётся обычным классом (или `data class`), но сравнение `salt` по содержимому сохраняется явно — через отдельный `ByteArrayValue`:

```kotlin
// domain/model/ByteArrayValue.kt
class ByteArrayValue(private val bytes: ByteArray) {
    fun content(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean =
        other is ByteArrayValue && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()
}

// domain/model/User.kt
class User(
    val login: String,
    val salt: ByteArrayValue,
    val passwordHash: PasswordHash
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return login == other.login &&
            salt == other.salt &&
            passwordHash == other.passwordHash
    }

    override fun hashCode(): Int {
        var result = login.hashCode()
        result = 31 * result + salt.hashCode()
        result = 31 * result + passwordHash.hashCode()
        return result
    }
}
```

Теперь сравнение `ByteArray` по содержимому инкапсулировано в обёртках, а `User` не содержит низкоуровневой логики сравнения массивов.

## O — Open/Closed Principle

> Классы должны быть открыты для расширения, но закрыты для изменения.

### O1. `when (action)` в `AccessService.check`

**Место:** `AccessService.kt` → `AccessService.check()`

```kotlin
val permitted = when (action) {
    Action.READ -> rule.canRead
    Action.WRITE -> rule.canWrite
    Action.EXECUTE -> rule.canExecute
}
```

При добавлении нового действия (например, `DELETE`) придётся:

1. Добавить `DELETE` в `enum Action`.
2. Дописать ветку `when` в `AccessService`.
3. Добавить поле `canDelete` в `AccessRule`.

Три изменения в трёх классах ради одного нового действия — класс не закрыт для модификации.

**Решение.** Вынести проверку в полиморфизм — каждая `Action` знает, как проверить себя:

```kotlin
// domain/model/Action.kt
enum class Action(private val check: (AccessRule) -> Boolean) {
    READ({ it.canRead }),
    WRITE({ it.canWrite }),
    EXECUTE({ it.canExecute });

    fun isPermittedBy(rule: AccessRule): Boolean = check(rule)

    companion object {
        fun from(value: String): Action? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

// AccessService
if (!action.isPermittedBy(rule)) return AccessResult.NoAccess
```

**Уточнение эффекта.** Формулировка «одно новое действие — одна строка» была неточной. На самом деле после рефакторинга добавление действия требует:

- одной строки в `enum Action` (новый элемент с лямбдой проверки);
- поля в `AccessRule` (например, `canDelete`) и его заполнения в данных;
- при необходимости — обновления источника данных/фикстур.

То есть `AccessService` и логика оркестрации действительно **не меняются** — это и есть выигрыш по OCP. Но полностью «одной строкой» добавление действия не станет, потому что модель правила должна получить новое разрешение. Если требуется именно нулевое изменение модели правила, альтернатива — хранить разрешения как `Set<Action>` внутри `AccessRule`:

```kotlin
data class AccessRule(
    val login: String,
    val resourcePath: String,
    val allowedActions: Set<Action>
)
```

Тогда новое действие = одна строка в `enum Action` плюс данные, без правки полей `AccessRule`. Такой вариант лучше соответствует обещанию OCP, но меняет структуру данных правила — это нужно явно проговорить.

### O2. `REQUIRED_KEYS` и строковые ключи CLI продублированы

**Место:** `Cli.kt` → `REQUIRED_KEYS`; `Main.kt` → `map["--login"]` и т.д.

Список обязательных ключей живёт в `Cli.kt`:

```kotlin
val REQUIRED_KEYS = listOf("--login", "--password", "--action", "--resource", "--volume")
```

А сами строковые литералы используются в `Main.kt`:

```kotlin
val code = service.check(
    AccessRequest(
        login = map["--login"]!!,
        password = map["--password"]!!,
        action = map["--action"]!!,
        resourcePath = map["--resource"]!!,
        volume = volume
    )
)
```

Добавление нового обязательного аргумента требует правок в двух местах.

**Решение.** Единый источник правды — `enum class`:

```kotlin
// presentation/cli/CliArgument.kt
enum class CliArgument(val key: String, val required: Boolean) {
    LOGIN("--login", true),
    PASSWORD("--password", true),
    ACTION("--action", true),
    RESOURCE("--resource", true),
    VOLUME("--volume", true),
    HELP("--help", false);

    companion object {
        val requiredKeys = entries.filter { it.required }.map { it.key }
    }
}
```

Теперь `Main.kt` обращается к `CliArgument.LOGIN.key`, а список обязательных ключей генерируется автоматически.

### O3. Regex в `ResourceValidator` захардкожен

**Место:** `Validator.kt` → `object ResourceValidator`

```kotlin
object ResourceValidator {
    private val namePattern = Regex("^[A-Za-z0-9_]{1,20}$")

    fun isValidName(name: String): Boolean = namePattern.matches(name)
    fun isValidPath(path: String): Boolean { /* ... */ }
    fun isValidVolume(volume: Int): Boolean = volume > 0
}
```

Правила валидации (допустимые символы, длина имени, минимальный объём) жёстко вшиты в код. Изменение правил = правка класса. Класс не закрыт для расширения.

**Решение.** Вынести правила в конфигурацию, внедряемую через конструктор:

```kotlin
// domain/validation/ValidationRules.kt
data class ValidationRules(
    val namePattern: Regex = Regex("^[A-Za-z0-9_]{1,20}$"),
    val minVolume: Int = 1
)

// application/validation/RequestValidator.kt
interface RequestValidator {
    fun validate(request: AccessRequest): AccessResult?
}

// infrastructure/validation/RegexRequestValidator.kt
class RegexRequestValidator(
    private val rules: ValidationRules
) : RequestValidator {
    override fun validate(request: AccessRequest): AccessResult? { /* ... */ }
}
```

Теперь правила валидации можно менять без правки классов, а результат валидации остаётся бизнесовым (`AccessResult`), а не CLI-кодом.

## L — Liskov Substitution Principle

> Наследники должны быть заменяемы своими базовыми типами без изменения корректности.

**Нарушений не выявлено.**

Обоснование:

- Иерархий наследования в проекте нет — все модели объявлены как `data class`, `class` или `enum class` без родителей (кроме неявного `Any`).
- Интерфейсы `UserRepository`, `ResourceRepository`, `RuleRepository` реализуются одним классом каждый. Поведение полностью соответствует контракту: методы возвращают `null` при отсутствии данных, не бросают неожиданных исключений, не нарушают постусловий.
- После введения `sealed interface AccessResult` и `sealed interface AuthResult` контракты становятся явными: реализации обязаны вернуть один из вариантов, и замена реализации не меняет корректность вызывающего кода.

## I — Interface Segregation Principle

> Клиенты не должны зависеть от методов, которые они не используют.

**Нарушений не выявлено.**

Обоснование:

- Каждый интерфейс содержит ровно один-два метода:

```kotlin
interface UserRepository {
    fun findUser(login: String): User?
    fun verifyPassword(user: User, password: String): Boolean
}

interface ResourceRepository {
    fun findResource(path: String): Resource?
}

interface RuleRepository {
    fun findEffectiveRule(login: String, resourcePath: String): AccessRule?
}
```

- «Толстых» интерфейсов нет, ни один клиент не вынужден реализовывать или вызывать методы, которые ему не нужны.
- `AccessService` использует **все** методы внедрённых интерфейсов.

## D — Dependency Inversion Principle

> Модули верхнего уровня не должны зависеть от модулей нижнего уровня; оба должны зависеть от абстракций.

### D1. Репозитории зависят от синглтона `Storage`

**Место:** `AccessService.kt` → `StorageUserRepository`, `StorageResourceRepository`, `StorageRuleRepository`

```kotlin
class StorageUserRepository : UserRepository {
    override fun findUser(login: String): User? = Storage.findUser(login)
    override fun verifyPassword(user: User, password: String): Boolean =
        MessageDigest.isEqual(user.passwordHash, Storage.hashPassword(password, user.salt))
}
```

Все три класса жёстко привязаны к глобальному `object Storage`. Это значит:

- нельзя подменить хранилище в тестах;
- нельзя использовать разные хранилища для разных сценариев;
- `StorageUserRepository` дополнительно зависит от `MessageDigest` и `Storage.hashPassword`, то есть от инфраструктуры криптографии.

**Решение.** Внедрять зависимости через конструктор:

```kotlin
class StorageUserRepository(
    private val storage: UserStorage,
    private val hasher: PasswordHasher
) : UserRepository {
    override fun findUser(login: String): User? = storage.findByLogin(login)

    override fun verifyPassword(user: User, password: String): Boolean {
        val actual = hasher.hash(password, user.salt.content())
        return MessageDigest.isEqual(actual, user.passwordHash.toByteArray())
    }
}
```

`UserStorage` и `PasswordHasher` — интерфейсы в доменном слое. Конкретные `InMemoryUserStorage` и `Sha256PasswordHasher` создаются в composition root (`ApplicationFactory`).

**Отдельно про аутентификацию.** Исходный пример «любой `null` → `EXIT_BAD_LOGIN`» терял ошибку неверного пароля. Теперь это исправлено на двух уровнях:

- `UserRepository.findUser` возвращает `null` только при отсутствии пользователя;
- `Authenticator` различает `AuthResult.BadLogin` и `AuthResult.BadPassword`;
- `AccessService` пробрасывает их в `AccessResult.BadLogin` / `AccessResult.BadPassword`;
- CLI-слой через `AccessResultMapper` выдаёт `ExitCode.BAD_LOGIN` / `ExitCode.BAD_PASSWORD`.

### D2. `AccessService` зависит от `ResourceValidator` напрямую

**Место:** `AccessService.kt` → `AccessService.check()`

```kotlin
fun check(request: AccessRequest): Int {
    if (!ResourceValidator.isValidPath(request.resourcePath) ||
        !ResourceValidator.isValidVolume(request.volume)
    ) {
        return EXIT_BAD_FORMAT
    }
    // ...
}
```

`ResourceValidator` — это `object` (глобальный синглтон). `AccessService` жёстко привязан к конкретной реализации валидатора:

- нельзя заменить правила валидации без правки `Validator.kt`;
- нельзя подменить валидатор в тестах (например, на заглушку);
- `AccessService` зависит от модуля нижнего уровня напрямую, что нарушает DIP.

**Решение.** Ввести интерфейс `RequestValidator` в слое приложения и внедрять его через конструктор. Контракт возвращает бизнесовый `AccessResult?`, а не CLI-тип `ExitCode`:

```kotlin
// application/validation/RequestValidator.kt
interface RequestValidator {
    fun validate(request: AccessRequest): AccessResult?
}

// infrastructure/validation/RegexRequestValidator.kt
class RegexRequestValidator(
    private val rules: ValidationRules
) : RequestValidator {
    override fun validate(request: AccessRequest): AccessResult? { /* ... */ }
}

// application/AccessService.kt
class AccessService(
    private val validator: RequestValidator,
    // ...
) {
    fun check(request: AccessRequest): AccessResult {
        validator.validate(request)?.let { return it }
        // ...
    }
}
```

### D3. `Main` знает о конкретных реализациях

**Место:** `Main.kt` → `fun main()`

```kotlin
val service = AccessService(
    users = StorageUserRepository(),
    resources = StorageResourceRepository(),
    rules = StorageRuleRepository()
)
```

`main()` создаёт конкретные реализации репозиториев. Формально это **допустимо** — `main` является composition root, и кто-то должен собирать граф зависимостей. Однако:

- логика сборки смешана с парсингом и валидацией (см. нарушение S3);
- при росте проекта `main` превратится в свалку из `new`.

**Решение.** Вынести сборку в отдельный класс `ApplicationFactory`:

```kotlin
object ApplicationFactory {
    fun create(): AccessService {
        val hasher = Sha256PasswordHasher()
        val userStorage = InMemoryUserStorage(Storage.users)
        val resourceStorage = InMemoryResourceStorage(Storage.resources)
        val ruleStorage = InMemoryRuleStorage(Storage.accessRules)
        val validator = RegexRequestValidator(ValidationRules())

        return AccessService(
            validator = validator,
            authenticator = DefaultAuthenticator(
                StorageUserRepository(userStorage, hasher)
            ),
            resourceFinder = StorageResourceFinder(
                StorageResourceRepository(resourceStorage)
            ),
            accessResolver = DefaultAccessResolver(
                StorageRuleRepository(ruleStorage)
            )
        )
    }
}
```

Тогда `main()` становится тривиальным:

```kotlin
fun main(args: Array<String>) {
    val service = ApplicationFactory.create()
    val result = service.check(parseAndValidate(args))
    exitProcess(AccessResultMapper.toExitCode(result).code)
}
```

## Сводная таблица нарушений

| №  | Принцип | Место                              | Краткое описание                              |
|----|---------|------------------------------------|-----------------------------------------------|
| S1 | SRP     | `Storage.kt` → `Storage`           | Хранение + поиск + криптография               |
| S2 | SRP     | `AccessService.kt` → `check()`     | 7 обязанностей в одном методе                 |
| S3 | SRP     | `Main.kt` → `main()`               | Парсинг + валидация + сборка + exit           |
| S4 | SRP     | `Cli.kt`                           | Константы + парсер + help в одном файле       |
| S5 | SRP     | `Models.kt` → `User`               | Данные + equals/hashCode для ByteArray        |
| O1 | OCP     | `AccessService.kt` → `when(action)`| Новое действие → правка кода                  |
| O2 | OCP     | `Cli.kt` + `Main.kt`               | `REQUIRED_KEYS` и ключи продублированы        |
| O3 | OCP     | `Validator.kt` → `Regex`           | Regex захардкожен                             |
| D1 | DIP     | `StorageXxxRepository`             | Зависимость от синглтона `Storage`            |
| D2 | DIP     | `AccessService` → `ResourceValidator` | Зависимость от `object` вместо абстракции  |
| D3 | DIP     | `Main.kt`                          | Сборка графа смешана с парсингом              |

## План рефакторинга

Порядок шагов выбран так, чтобы после каждого этапа проект оставался работоспособным.

1. **Выделить доменные value-обёртки и результаты:**
   - `ResourcePath` — инкапсулирует парсинг и валидацию пути.
   - `PasswordHash` и `ByteArrayValue` — обычные классы-обёртки (не `value class`), инкапсулирующие сравнение `ByteArray` по содержимому; `User` сохраняет сравнение `salt` по содержимому.
   - `AccessResult` и `AuthResult` — `sealed interface` с отдельными вариантами для `BadLogin` и `BadPassword`.
   - `Action` — перевести на полиморфную проверку прав.

2. **Разделить `Storage`:**
   - `InMemoryUserStorage`, `InMemoryResourceStorage`, `InMemoryRuleStorage`.
   - `Sha256PasswordHasher` — отдельная реализация `PasswordHasher`.

3. **Ввести абстракции в слое приложения:**
   - `PasswordHasher`, `RequestValidator` (возвращает `AccessResult?`), `Authenticator` (возвращает `AuthResult`), `AccessResolver`, `EffectiveRuleResolver`.

4. **Разбить `AccessService.check()` на цепочку use case'ов:**
   - `CheckAccessUseCase`, который последовательно вызывает `RequestValidator`, `Authenticator`, `AccessResolver`.
   - Аутентификатор различает отсутствие пользователя и неверный пароль; сервис пробрасывает это в `AccessResult.BadLogin` / `AccessResult.BadPassword`.

5. **Уточнить OCP-исправление для `Action`:**
   - либо `enum Action` с лямбдой проверки + поле в `AccessRule` (оркестратор не меняется, но модель правила — да);
   - либо `AccessRule.allowedActions: Set<Action>` — тогда новое действие требует только строки в enum и данных, без правки полей правила. Выбор зафиксировать явно.

6. **Централизовать описание CLI-аргументов:**
   - `enum class CliArgument(key, required, description)`.

7. **Разделить `Cli.kt`:**
   - `ExitCode.kt`, `AccessResultMapper.kt`, `CliParser.kt`, `HelpPrinter.kt`.

8. **Пересобрать `Main.kt`:**
   - `CliArgumentParser`, `CliArgumentValidator`, `ApplicationFactory`, тонкий `main()`; маппинг `AccessResult → ExitCode` только в CLI-слое.

9. **Внедрить зависимости через конструкторы** вместо синглтонов.
