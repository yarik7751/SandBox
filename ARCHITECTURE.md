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
