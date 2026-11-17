# MinequestCommandsHide

Velocity-плагин, который скрывает команды от игроков по группам **LuckPerms**:
убирает их из клиентского дерева команд, из таб-комплита, запрещает выполнение
и перехватывает `/help`, показывая только реально доступные игроку команды.

Работает только через официальный Velocity API, без Protocolize и packet-хаков.

## Возможности

- Режимы `whitelist` (видно только перечисленное) и `blacklist` (скрыто только перечисленное).
- Группы берутся из LuckPerms по имени, наследование групп учитывается.
- Группа `default` применяется всегда как базовая.
- Скрытые команды не выполняются, вместо них отправляется настраиваемый текст.
- Таб-комплит и клиентский список команд (`PlayerAvailableCommandsEvent`) фильтруются.
- `/help` (и `?`) перехватывается прокси и показывает кликабельный список только видимых команд.
- Удаление `plugin:command` из дерева (`remove-plugin-prefix`).
- `/mch reload` — перечитать конфиг без перезапуска прокси.
- При битом конфиге — fail-open: сервер не блокируется, ошибка пишется в лог.
- Если LuckPerms недоступен, применяется только группа `default`.

## Права

| Право | Описание |
| --- | --- |
| `mch.bypass` или `minequestcommandshide.bypass` | Полный обход фильтрации: видно и можно всё |
| `mch.reload` или `minequestcommandshide.reload` | Доступ к `/mch reload` |

Достаточно любого из двух вариантов.

## Команды

```
/mch reload
/minequestcommandshide reload
```

## Конфиг

Файл: `plugins/minequestcommandshide/config.yml`. Создаётся при первом запуске.

```yaml
mode: whitelist          # whitelist | blacklist
remove-plugin-prefix: true

help:
  enabled: true
  header: '&6Доступные команды:'
  format: '&e/%s'
  hover: '&7Нажмите, чтобы выполнить'
  footer: ''
  page-format: '&7Страница {page}/{pages}'
  page-size: 12

messages:
  deny: '&f▖ Неизвестная или неполная команда!'
  no-permission: '&cНедостаточно прав.'
  reload-success: '&aКонфиг MinequestCommandsHide перезагружен.'
  reload-failure: '&cНе удалось загрузить конфиг: %s'
  reload-usage: '&eИспользование: /mch reload'
  help-empty: '&7Нет доступных команд.'

groups:
  default:
    commands: [discord, link, tell, msg, reply, when, inspect, list, sit, hints]
  player:
    commands: [unlink]
  minequestplus:
    commands: [togglepm, togglechat, togglementions, server]
  helper:
    commands: [warning, kick, ban, coreprotect, co, tp, teleport, server, spectator, survival, invsee, endersee]
```

- Ключ группы — имя группы в LuckPerms (`default`, `player`, `minequestplus`, `helper`, ...).
- Команды можно писать со слэшем, с namespace (`minecraft:msg`) или без — всё нормализуется.
- Алиасы перечисляются явно: `msg`, `tell`, `reply` — разные команды.
- В `whitelist` игроку доступно объединение команд всех его групп (включая наследованные) и `default`.
- В `blacklist` из доступа убирается объединение перечисленных команд.

## Сборка

Нужен JDK 25.

```bash
./gradlew build
```

Готовый jar: `build/libs/MinequestCommandsHide-1.0.0.jar`, а также копия в корне проекта.

Сразу на dev-сервер Velocity:

```bash
./gradlew build -PvelocityPluginsDir=/home/minequest/MinequestLocal/velocity/plugins
```

или

```bash
bash scripts/deploy-dev.sh
```

## Установка

1. Положить `LuckPerms-Velocity-*.jar` и `MinequestCommandsHide-1.0.0.jar` в `plugins/`.
2. Запустить прокси, при необходимости поправить `plugins/minequestcommandshide/config.yml`.
3. Выдать нужным группам права `mch.bypass` / `mch.reload`, если требуется.
4. `/mch reload` после правок конфига.

## Ограничения

- После `/mch reload` новое дерево команд уедет клиенту только при переподключении
  или смене сервера: Velocity не позволяет принудительно переслать пакет.
- Фильтруются корневые команды. Тонкие подкоманды сторонних плагинов не настраиваются.
- `/help` перехватывается прокси; если `help.enabled: false`, работает обычная логика режима.
- Плагин рассчитан на Velocity 4.x и Java 25.

## Лицензия

MIT
