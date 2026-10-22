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

Используется опубликованная сборка `7.2.0.5079-linux-x64`. Имя архива и каталог после распаковки содержат суффикс платформы `-linux-x64`, а не просто `-linux`:

```bash
wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-7.2.0.5079-linux-x64.zip
unzip sonar-scanner-cli-7.2.0.5079-linux-x64.zip
export PATH=$PATH:$(pwd)/sonar-scanner-7.2.0.5079-linux-x64/bin
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

Важно различать два разных исхода:

- **Ошибка анализа (Analysis failed)** — сканер не смог загрузить или обработать отчёт: неверный токен, недоступен `sonarcloud.io`, синтаксическая ошибка в `sonar-project.properties`, отсутствуют исходники. В этом случае Quality Gate вообще не вычисляется, и в SonarCloud отображается **Not computed** (или статус отсутствует) . Это проблема инфраструктуры или конфигурации, а не качества кода.
- **Проваленный Quality Gate (Gate failed)** — анализ прошёл успешно, отчёт загружен, но хотя бы одно условие порога не выполнено (покрытие ниже 80%, дублирование выше 3%, есть критические проблемы в новом коде и т.д.). SonarCloud явно показывает **Failed** . Это проблема качества кода.

## Шаг 6. Автоматизация через GitHub Actions

Файл `.github/workflows/sonarcloud.yml`:

```yaml
name: SonarCloud Analysis

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
    types: [opened, synchronize, reopened]

jobs:
  sonarcloud:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Build project
        run: |
          chmod +x build.sh
          ./build.sh

      - name: SonarCloud Scan
        uses: SonarSource/sonarqube-scan-action@v5
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

      - name: Check Quality Gate
        uses: SonarSource/sonarqube-quality-gate-action@v1
        timeout-minutes: 5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

Что важно в этом workflow:

- `fetch-depth: 0` — SonarCloud нужна полная история для инкрементального анализа.
- `build.sh` выполняется до сканирования, чтобы появились артефакты.
- `SONAR_TOKEN` читается из GitHub Secrets (шаг 2).
- Шаг `Check Quality Gate` завершится с ошибкой, если Gate провален, но **сам по себе не блокирует слияние PR** — он лишь выставляет статус проверки. Блокировка обеспечивается правилами защиты ветки (см. шаг 7).

## Шаг 7. Quality Gate и блокировка слияния PR

По умолчанию SonarCloud использует **Sonar way**:

| Метрика | Порог по умолчанию |
|---|---|
| Покрытие нового кода | ≥ 80% |
| Дублирование нового кода | ≤ 3% |
| Критические проблемы в новом коде | 0 |
| Проверка security hotspots | 100% |
| Рейтинг поддерживаемости нового кода | A |

Кастомный Quality Gate:

1. Страница проекта → **Administration** → **Quality Gate**.
2. **«Create»** — создать свой порог.
3. Задать значения и привязать к проекту.

**Чтобы Gate действительно блокировал слияние PR**, нужно сделать статус проверки обязательным в настройках ветки GitHub:

1. Репозиторий → **Settings** → **Branches** (или **Rules** → **Rulesets**).
2. Для ветки `main` включить **Require status checks to pass before merging** .
3. В списке обязательных проверок найти и добавить статус, который публикует workflow `SonarCloud Analysis` (обычно это имя job или шага, например `sonarcloud` или `SonarCloud Analysis / sonarcloud`). Точное имя появится в списке после первого запуска workflow .
4. Сохранить правило.

После этого, если Gate провален (или шаг `Check Quality Gate` упал), GitHub пометит соответствующий check как failed, и кнопка merge будет заблокирована до исправления.

## Шаг 8. Проверка интеграции

1. Запушить код, чтобы сработал workflow:

```bash
git add .
git commit -m "Add SonarCloud integration"
git push origin lab3-analysis
```

2. Проверить статус GitHub Actions:
   - репозиторий → вкладка **Actions**;
   - должен появиться workflow `SonarCloud Analysis`.

3. Проверить результаты в SonarCloud:
   - открыть [sonarcloud.io](https://sonarcloud.io);
   - проект → Issues, Coverage, Duplications.

4. Проверить блокировку слияния:
   - создать PR с заведомо провальным Gate (например, без тестов на новый код);
   - убедиться, что кнопка merge заблокирована и в разделе checks отображается failed-статус SonarCloud;
   - исправить проблемы, дождаться зелёного Gate и убедиться, что merge разблокирован.

5. Убедиться, что ошибка анализа и провал Gate различимы:
   - при ошибке анализа (например, временно указать неверный токен) в SonarCloud не будет вердикта Gate, а workflow упадёт на шаге сканирования;
   - при провале Gate анализ завершится успешно, но шаг `Check Quality Gate` вернёт ошибку, а в SonarCloud будет явный статус **Failed**.
