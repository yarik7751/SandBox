# SandBox

Песочница с фичами на Compose: «Буквы», «WebSocket», «Подземелье» и др.

## Сборка

```
./gradlew build          # полная сборка со всеми проверками
./gradlew :app:assembleDebug
./gradlew detekt         # статический анализ (вне `check`, гоняется явно)
```

## Тесты

```
./gradlew :core:dungeon:test
```

## Структура модулей

| Модуль | Назначение |
|---|---|
| `:app` | приложение: граф навигации из Dagger-мапы экранов, Dagger-компонент, тема |
| `:core:navigation` | `JoyScreen`/`JoyRouter` — навигация между фичами |
| `:core:ui` | `BaseViewModel`, `CommandFlow`, `SandBoxToolbar`, `ViewModelFactory` |
| `:core:dungeon` | чистое JVM-ядро головоломки «Подземелье»: состояние, ход, BFS врага, 5 уровней. Детерминировано, без побочных эффектов и зависимостей от Android |
| `:feature:<feature>:api` | объекты экранов фичи (route-константы) |
| `:feature:<feature>:impl` | `presentation` (Compose + ViewModel), `domain` (interactor'ы, интерфейсы репозиториев), `data` (реализации репозиториев), `localdatasource` (DataStore/SharedPreferences/Room) |

### Фича «Подземелье»

Порт браузерной пошаговой головоломки по мотивам финального уровня
«Башни знаний» (1996): выведите пленников из подземелья, заперев
Повелителя Тьмы закрытыми дверями.

- Ядро — `:core:dungeon` (порт `js/rules.js`/`js/levels.js` прототипа,
  семантика 1:1; эталонные решения из `test/solutions.json` проигрываются
  в юнит-тестах до победы ровно за пар).
- Сохранения — DataStore Preferences (`podzemelie-save-v1`): разблокированные
  уровни, рекорды ходов, настройки звука и вибрации.
- Звук — синтез через `ToneGenerator`, без аудиофайлов.
