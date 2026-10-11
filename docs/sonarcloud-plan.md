# План интеграции SonarCloud

## Предварительные требования

| Пункт | Требование |
|---|---|
| Java | JDK 11+ (SonarScanner CLI 7.2+ умеет подтягивать JDK автоматически) |
| Аккаунт SonarCloud | Регистрация через GitHub |
| Репозиторий GitHub | Код запушен в удалённый репозиторий |
| Сеть | Доступ к `sonarcloud.io` |

## Шаг 1. Аккаунт SonarCloud и импорт репозитория

1. Открыть [sonarcloud.io](https://sonarcloud.io).
2. Войти через **GitHub**.
3. Разрешить SonarCloud доступ к организации/репозиториям.
4. **«+»** → **«Analyze new project»**.
5. Выбрать репозиторий → **«Set Up»**.

После импорта записать:

- **Organization Key** — например, `your-org`;
- **Project Key** — например, `your-org_your-repo`.
