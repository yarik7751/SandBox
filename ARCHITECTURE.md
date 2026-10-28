# Architecture rules

## Слои и пакеты

| Слой | Пакет | Содержимое |
|---|---|---|
| presentation | `presentation` | Compose-экраны, ViewModel |
| domain | `domain` | interactors (интерфейс + impl), useCases, интерфейсы репозиториев |
| data | `data` | реализации репозиториев |
| dataSources | `localdatasource`, `remotedatasource` | локальные (БД) и сетевые источники данных |

- В каждом пакете допускаются подпакеты `model`, `mapper` и другие по необходимости.
- Зависимости направлены внутрь: presentation → domain → data → dataSources.
- Слои общаются через интерфейсы.
- Интерфейсы создаём только для компонентов domain (interactor, useCase) и data (repository). Для всех остальных компонентов — только если этого требует задание.
- dataSources не импортируются напрямую из presentation/domain — только через data layer.

## Модели и маппинг

| Слой | Именование модели |
|---|---|
| presentation | `XxxUiModel` |
| domain | `Xxx` |
| data | `XxxDataModel` |
| localdatasource | `XxxEntity` (Room) или `XxxDto` |
| remotedatasource | `XxxDto` |

`Xxx` — название сущности.

- Каждый слой работает только со своей моделью.
- Переход модели между слоями — через мапперы в пакете `mapper` принимающего слоя.

## Как создать новый экран

### 1. API-модуль фичи (`:feature:<feature>:api`)
- Объект-экран: `XxxJoyScreen : JoyScreen<NoArgs>` с константой `ROUTE = "xxx"`.
- Экран с аргументами — через `JoyScreenWithArgs` (в графе поддерживаются Long-аргументы).

### 2. Impl-модуль, пакет `presentation/xxx/`
- **`XxxScreen`** — Compose-функция:
  - параметры `viewModelFactory: ViewModelFactory`, `router: JoyRouter`;
  - ViewModel: `viewModel(factory = viewModelFactory)`;
  - стейт: `collectAsStateWithLifecycle()`;
  - `Scaffold` + `SandBoxToolbar` (title через `stringResource`, `onBackClick = { router.exit() }`);
  - навигация — только через `router`, по командам из ViewModel.
- **`XxxViewModel`** — наследник `BaseViewModel`, `@Inject constructor`:
  - `stateFlow = MutableStateFlow(State())`, `commandsFlow = CommandFlow<Commands>(viewModelScope)`;
  - `data class State` + `sealed interface Commands` внутри ViewModel;
  - доменные данные — только через interactor'ы;
  - выход на UI — через `XxxUiModel` (`presentation/model`) и мапперы (`presentation/mapper`).
- **`XxxScreenProvider`** — реализация `ScreenContentProvider`.

### 3. Ресурсы
- Все строки — в `strings.xml` фичи, без хардкода в коде.

### 4. DI
- В Dagger-модуле фичи:
  - `@Provides @IntoMap @ViewModelKey(XxxViewModel::class)` → ViewModel;
  - `@Provides @IntoMap @ScreenKey(XxxJoyScreen.ROUTE)` → `XxxScreenProvider`.
- Новая фича — подключить модуль в `AppComponent` (`:app` править не нужно, граф собирается из Dagger-мапы).

### 5. Переходы между экранами
- ViewModel эмитит команду в `commandsFlow`; экран подписывается через `commandsFlow.observe { ... router.navigateTo(XxxJoyScreen) }`.
- Результаты между экранами — через `router.sendResult/resultFlow` (при необходимости).

### 6. Проверка
- Сборка `:app:assembleDebug` без ошибок.
