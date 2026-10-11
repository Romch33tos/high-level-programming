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

## Шаг 2. Токен аутентификации

1. SonarCloud → аватар → **«My Account»**.
2. Вкладка **«Security»**.
3. Ввести имя токена (например, `github-actions-token`).
4. **«Generate»**.
5. Скопировать токен сразу — показывается один раз.

Добавить токен в GitHub Secrets:

- Репозиторий → **Settings** → **Secrets and variables** → **Actions**.
- **«New repository secret»**.
- Name: `SONAR_TOKEN`.
- Value: сохранённый токен.

## Шаг 3. Файл `sonar-project.properties`

Создать в корне проекта:

```properties
# Идентификаторы проекта (получить после создания проекта в SonarCloud)
sonar.projectKey=your-org_your-repo
sonar.organization=your-org

# Метаданные (опционально)
sonar.projectName=Access Control CLI
sonar.projectVersion=1.0

# Исходники относительно этого файла
sonar.sources=src

# Исключения
sonar.exclusions=**/build/**,**/lib/**,**/*.jar

# Кодировка
sonar.sourceEncoding=UTF-8

# Отчёт Detekt (удалить, если Detekt не используется)
sonar.kotlin.detekt.reportPaths=build/reports/detekt.xml
```

## Шаг 4. Установка SonarScanner CLI

### Вариант A. Прямая загрузка (для локального запуска)

```bash
wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-7.2.0.1092-linux.zip
unzip sonar-scanner-cli-7.2.0.1092-linux.zip
export PATH=$PATH:$(pwd)/sonar-scanner-7.2.0.1092-linux/bin
```

Проверка:

```bash
sonar-scanner -h
```

### Вариант B. Docker

```bash
docker run --rm \
  -e SONAR_HOST_URL="https://sonarcloud.io" \
  -e SONAR_TOKEN="your-token" \
  -v "$(pwd):/usr/src" \
  sonarsource/sonar-scanner-cli
```

## Шаг 5. Локальный запуск анализа

```bash
export SONAR_TOKEN="your-token-here"

sonar-scanner \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN
```

При успехе в терминале появится `ANALYSIS SUCCESSFUL`.
