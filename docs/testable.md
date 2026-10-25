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
