# Анализ тестопригодности проекта

## 1. Общая характеристика проекта

Проект — консольное приложение контроля доступа к ресурсам. Пользователь
передаёт через CLI логин, пароль, действие (`read`/`write`/`execute`),
путь до ресурса и запрашиваемый объём. Приложение проверяет формат запроса,
аутентифицирует пользователя, находит ресурс, вычисляет действующее правило
доступа и возвращает результат в виде кода выхода.

Архитектура разделена на слои:

| Слой | Пакет | Назначение |
|---|---|---|
| Представление | `presentation.cli` | разбор аргументов, маппинг результата в код выхода, справка |
| Приложение | `application`, `application.auth`, `application.validation`, `application.dto` | сценарий проверки доступа, аутентификация, валидация, DTO |
| Домен | `domain.model`, `domain.service`, `domain.repository` | модели, бизнес-правила, интерфейсы репозиториев |
| Инфраструктура | `infrastructure.*` | in-memory хранилища, реализации репозиториев, хеширование, hex-утилиты |

**Ключевое наблюдение:** все зависимости бизнес-логики вынесены за интерфейсы
(`UserRepository`, `ResourceRepository`, `RuleRepository`, `PasswordHasher`,
`RequestValidator`, `Authenticator`) и передаются через конструктор
(constructor injection). Это делает проект **изначально хорошо тестопригодным**:
любую зависимость можно заменить фейком без изменения продакшн-кода.

---

## 2. Классификация классов по тестопригодности

### 2.1. Легко тестируемые классы (чистая логика, без I/O)

| Класс / метод | Что проверять |
|---|---|
| `Action.from(String)` | парсинг `read`/`write`/`execute`, регистронезависимость, `null` на неизвестное значение |
| `Action.isPermittedBy(rule)` | разрешение каждого действия согласно флагам правила |
| `ResourcePath.parse(value)` | валидные/невалидные пути, пустые сегменты, длина сегмента > 20, спецсимволы |
| `ResourcePath.segments`, `parentSegments()` | разбиение пути по точкам |
| `ResourcePath.isPrefixOf(other)` | префикс, равенство, более длинный путь, несвязанные пути |
| `ResourcePath.fromValidated(value)` | создание без проверки формата |
| `Resource.fullPath` | склейка `parentPath.name`, пустой `parentPath` |
| `ByteArrayValue.content()/equals/hashCode` | копирование массива, сравнение по содержимому, хеш |
| `PasswordHash.matches/toByteArray/equals` | сравнение байтов, защита от мутации, хеш |
| `EffectiveRuleResolver.resolve(...)` | выбор наиболее специфичного правила, fallback на родителя, отсутствие правила, чужой логин |
| `RegexRequestValidator.validate(...)` | `BadFormat` по пути и объёму, `null` для валидного запроса |
| `AccessResultMapper.toExitCode(...)` | маппинг всех 8 вариантов `AccessResult` |
| `String.hexToBytes()` | корректный разбор hex-строки, пустая строка |
| `Sha256PasswordHasher.hash/verify` | детерминированность хеша, `verify` true/false |

### 2.2. Тестируемые с использованием фейков (зависимости за интерфейсами)

| Класс | Какие фейки нужны |
|---|---|
| `DefaultAuthenticator` | fake `UserRepository` |
| `AccessService` | fake `RequestValidator`, `Authenticator`, `ResourceRepository`, `RuleRepository` |
| `StorageUserRepository` | `InMemoryUserStorage` + fake/real `PasswordHasher` |
| `StorageResourceRepository` | `InMemoryResourceStorage` |
| `StorageRuleRepository` | `InMemoryRuleStorage` + real `EffectiveRuleResolver` |
| `InMemory*Storage` | тривиальные, проверяются через `findBy*` |

Все перечисленные классы используют constructor injection, поэтому подмена
зависимостей не требует рефакторинга.

### 2.3. Сложно-/нетестируемые классы

| Класс | Причина |
|---|---|
| `Main.kt` | вызывает `exitProcess`, читает `args`, зависит от окружения — интеграционный уровень, unit-тестами не покрывается |
| `CliRequestMapper` | зависит от сторонней библиотеки `kotlinx.cli` и парсинга `System`-аргументов; проверка относится к интеграционному уровню |
| `ApplicationFactory` | чистый wiring зависимостей, тестируется косвенно через сценарии `AccessService` |
| `HelpPrinter.print()` | пишет напрямую в `System.out`; перехват stdout в unit-тестах нежелателен (см. п. 3.2) |
| `Storage` (object) | статическая фикстура данных, отдельного тестирования не требует |

---

## 3. Проблемы тестопригодности и минимальный рефакторинг

Рефакторинг выполняется **без изменения логики работы программы** и только там,
где это повышает тестопригодность.

### 3.1. `User` — приведён к `data class` ✅

**Проблема:** неудобно использовать в тестах-фикстурах: нет `copy()`,
`toString()` неинформативен.

**Решение (выполнено):** класс объявлен как `data class User(...)`.
Сгенерированные `equals`/`hashCode` дают то же поведение, что и ручные,
поскольку `ByteArrayValue` и `PasswordHash` уже реализуют сравнение
по содержимому. Логика не меняется. Дополнительно получены `copy()`
и читаемый `toString()`.

### 3.2. `HelpPrinter` — текст вынесен в константу ✅

**Проблема:** текст справки невозможно проверить unit-тестом без перехвата stdout.

**Решение (выполнено):** текст вынесен в публичную константу `helpText`,
`print()` реализован как `println(helpText)`. Вывод в stdout не изменился.
Появилась возможность проверить содержимое справки без побочных эффектов.

### 3.3. Дублирование логики валидации пути (осознанно не исправляется)

`RegexRequestValidator.isValidPath` и `ResourcePath.parse` содержат схожую
проверку сегментов пути. **Рефакторинг не выполняется**, поскольку
`ValidationRules.namePattern` настраиваемый, а паттерн в `ResourcePath`
захардкожен. Объединение изменило бы поведение при кастомных правилах
валидации. Классы покрываются тестами по отдельности.

### 3.4. Что осознанно оставлено без изменений

- `AccessService.check` — цепочка из 8 веток `return`. Это не недостаток,
  а удобная структура для покрытия каждой ветки отдельным тестом.
- `StorageUserRepository` использует `MessageDigest.isEqual` — статический
  детерминированный метод, тестируется через фейковый `PasswordHasher`.

  ---

## 4. План тестового покрытия

### 4.1. Unit-тесты без фейков

`ActionTest`, `ResourcePathTest`, `ResourceTest`, `ByteArrayValueTest`,
`PasswordHashTest`, `EffectiveRuleResolverTest`, `RegexRequestValidatorTest`,
`AccessResultMapperTest`, `HexTest`, `Sha256PasswordHasherTest`.

### 4.2. Unit-тесты с фейками

`DefaultAuthenticatorTest` (fake `UserRepository`),
`AccessServiceTest` (fake `RequestValidator`, `Authenticator`,
`ResourceRepository`, `RuleRepository` — покрытие всех 8 исходов `AccessResult`),
`StorageUserRepositoryTest`, `StorageResourceRepositoryTest`,
`StorageRuleRepositoryTest`.

### 4.3. Не покрывается тестами (и почему)

| Компонент | Причина |
|---|---|
| `Main` | точка входа, `exitProcess`, интеграционный уровень |
| `CliRequestMapper` | зависимость от `kotlinx.cli`, интеграционный уровень |
| `ApplicationFactory` | wiring, проверяется косвенно |
| `HelpPrinter.print()` | побочный эффект в stdout; проверяется константа `helpText` |
| `Storage` | статическая фикстура |

---

## 5. Результаты выполнения тестов

### 5.1. Что пришлось изменить в проекте
- Класс `User` приведен к `data class` для удобства использования в тестах-фикстурах (появились `copy()`, читаемый `toString()`). Логика работы `equals/hashCode` не изменилась.
- В `HelpPrinter` текст справки вынесен в публичную константу `helpText`. Метод `print()` теперь просто выводит её, что позволило протестировать содержимое без перехвата `System.out`.
- Скрипты сборки (`build.sh`, `run.sh`, `test.sh`) перенесены в директорию `scripts/` согласно офф-топ требованиям. Добавлены новые скрипты для компиляции и запуска JUnit-тестов (`build_tests.sh`, `run_unit_tests.sh`).

### 5.2. Примеры проверяемых сценариев

| Тест | Краткое описание |
|---|---|
| `shouldReturnBadFormatWhenPathIsEmpty` | Валидатор возвращает `BadFormat`, если путь пустой |
| `shouldFallbackToParentRule` | `EffectiveRuleResolver` находит правило для родительского пути, если точного нет |
| `shouldReturnVolumeExceededWhenVolumeIsGreaterThanMax` | `AccessService` возвращает `VolumeExceeded`, если объем больше максимального |
| `shouldMapSuccessToSuccessExitCode` | Маппер корректно переводит `AccessResult.Success` в `ExitCode.SUCCESS` |
| `shouldVerifyWhenPasswordMatches` | Хешер паролей успешно верифицирует правильный пароль |
| `shouldNotReportPrefixWhenPathsAreUnrelated` | Проверка метода `isPrefixOf` для несвязанных путей |

### 5.3. Классы, не покрытые тестами, и причины
- `Main.kt` — точка входа, использует `exitProcess`, тестируется на интеграционном уровне.
- `CliRequestMapper` — зависит от сторонней библиотеки `kotlinx.cli`, оставлен для интеграционных тестов.
- `ApplicationFactory` — только связывает зависимости (wiring), проверяется косвенно через сценарии `AccessService`.
- `Storage` — статическая фикстура данных.

### 5.4. Итоги запуска
- Всего тестов: **99**
- Успешно пройдено: **99**
- Провалено: **0**

<p align="center">
  <img src="https://github.com/user-attachments/assets/1dc6bffb-902f-406b-bf77-b4289e27c955" height="150" alt="Статистика сборки тестов" style="margin-right: 15px;" />
  <img src="https://github.com/user-attachments/assets/a6a760ea-17a1-4bb8-a025-b1b9dd29f47f" height="150" alt="Отчет JUnit о прохождении тестов" />
</p>
<p align="center"><i>Скриншоты успешного выполнения компиляции и запуска unit-тестов</i></p>
