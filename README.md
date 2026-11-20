<p align="center"><img src="assets/logo.png" width="128" height="128" alt="LunarWrath logo"></p>

<h1 align="center">LunarWrath</h1>

<p align="center">Moon event plugin for Paper and Purpur: scheduled blood, black and purple moons with hordes, flying bosses, tiered loot and a world that punishes anyone who tries to wait the night out.</p>

<p align="center">
  <a href="https://github.com/milkycloud-dev/lunar-wrath/actions/workflows/build.yml"><img src="https://github.com/milkycloud-dev/lunar-wrath/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <a href="https://github.com/milkycloud-dev/lunar-wrath/actions/workflows/release.yml"><img src="https://github.com/milkycloud-dev/lunar-wrath/actions/workflows/release.yml/badge.svg" alt="Release"></a>
  <a href="https://github.com/milkycloud-dev/lunar-wrath/releases/latest"><img src="https://img.shields.io/github/v/release/milkycloud-dev/lunar-wrath" alt="Latest release"></a>
</p>

<p align="center"><a href="#english">English</a> | <a href="#русский">Русский</a></p>

---

<a id="english"></a>

## English

### Why it exists

This started as a Russian translation of BloodMoonPlus 1.0.1. Then players found color codes leaking into item tooltips, the moon was trivial to sit out in the Nether or on a pillar, and bosses either never showed up or flattened half the spawn. Fixing that one piece at a time turned into rewriting most of the event logic, so by now it is its own plugin with its own release line.

The goal is simple: a few times a day the night turns hostile for everyone at once, it is loud and visible, and the only way through is to fight. Hiding gets you hurt, quitting gets you killed, and nothing the event spawns is allowed to wreck the terrain people built.

### How it works

A scheduler starts the event at fixed times (weekly mode, by real time zone) or every N real hours. When it fires, every configured world gets locked into night and thunder, a boss bar with the remaining time appears, and hordes start spawning around each eligible player. Waves grow with difficulty, which you set per schedule slot and which can rise by itself if players camp.

Bosses are planned at the start of the session and spawn near random participants. When the timer runs out the world is cleared of event mobs, the time is set to dawn, and survivors get their rewards. The session is persisted, so a restart or reload in the middle of a moon resumes it with the same end time, difficulty and moon type.

### Features

- **Three moon types.** Blood (classic), Black (stronger, darker, harder bosses) and Purple (astral mobs and effects), each with its own announcement, colors and boss bar. Start any of them by schedule or by hand.
- **Hordes with mixed mob rosters.** Weighted mob lists, armor, health and damage scaling, per-player alive caps, special raiders (paratroopers, pyromancers, crossbowmen) that unlock at higher difficulty.
- **Eleven bosses.** Ground bosses with abilities (slam, charge, roar, summon, sonic boom, lightning, pull, guard, self heal) plus a Black Moon Ender Dragon and a flying Wither that hunt players in the Overworld. Damage leaderboard and a separate reward table for the top damager. Bosses never break blocks.
- **Wither lifecycle.** Outside the moon the Wither is banned in the controlled worlds; during the moon players may build one. With WorldGuard installed the plugin toggles `deny-spawn` on `__global__` for you and restores it on stop, restart and shutdown.
- **Anti-camping.** Dynamic difficulty rises for players who hide and falls when they fight. Dimension wrath hits anyone waiting in the Nether or the End, elytra wrath grounds high flyers, quitting during the moon kills the player on the spot.
- **Tiered loot.** Six rarities with pity counters, 45 custom items with abilities, separate tables for mobs, bosses and survivors. Epic, legendary and mythic drops get their own chat lines, sounds, titles and particles.
- **Countdown and warnings.** `/luna countdown 5m dangerous black` gives players one atmospheric warning, then a quiet countdown, then the moon.
- **Terrain protection.** Event mobs and boss explosions do not break blocks; regular explosions keep working outside the moon.
- **Spectator lock.** Non-admins cannot switch to spectator during the moon; operators and `bloodmoon.bypass` are exempt.
- **PlaceholderAPI.** Around 60 placeholders under `%bloodmoon_...%`: status, remaining time, active boss health, session stats, history, pity counters.
- **Localized.** English, Russian and Spanish locales; missing keys fall back to the copy bundled in the jar, so a new version never shows raw keys.

### Requirements

- Paper or Purpur 1.21.4 or newer (tested on Purpur 26.3), Java 21.
- Optional: PlaceholderAPI, WorldGuard 7 (for the Wither sync), Multiverse-Core, MythicMobs (boss mode `MYTHICMOBS`).

### Setup from scratch

1. Download `LunarWrath.jar` from the latest release and put it into `plugins/`.
2. Start the server once. The plugin creates `plugins/BloodMoonPlus/` with `config.yml`, `bosses.yml`, `items.yml`, `loot.yml` and `locales/`.
3. Pick the language in `locales/locales.yml` (`Language: en`, `ru` or `es`).
4. In `config.yml`:
   - `worlds`: worlds where the moon happens.
   - `schedule`: `mode: weekly` with times per day and a `timezone`, or `mode: interval` with `interval-hours`.
   - `event`: duration, night lock, terrain protection, start announcement, console commands on start, end and per survivor (`{world}`, `{player}`).
   - `wither-control`: `enabled`, `allow-only-during-bloodmoon`, `sync-worldguard`, `worlds`, and `keep-deny-spawn` for other mobs that must stay banned in `__global__`.
   - `horde`, `difficulty`, `dynamic-difficulty`, `dimension-wrath`, `elytra-wrath`, `quit-penalty`, `spectator-block`, `block-placement`, `countdown`, `announce`: each block is commented in the file.
5. Tune bosses in `bosses.yml` (`abilities` use `TYPE:cooldown:args`) and drops in `loot.yml`.
6. Run `/luna reload`, then `/luna start` to try it.

Permissions:

| Permission | Default | What it gives |
|---|---|---|
| `bloodmoon.use` | everyone | `/luna` (status), `/luna next` |
| `bloodmoon.admin` | op | every admin subcommand |
| `bloodmoon.bypass` | op | exempt from spectator lock and wrath |
| `bloodmoon.bypass.blockplace` | op | build while `block-placement.allow` is false |

Main commands (`/luna`, aliases `/bm`, `/bloodmoon`): `start [minutes] [difficulty] [world]`, `stop`, `black`, `purple`, `blood`, `countdown <time> [normal|dangerous|lethal] [moon] [world]`, `boss <id> [player|world]`, `horde`, `difficulty`, `duration`, `dynamic`, `lightning`, `give`, `items`, `chances`, `killmobs`, `next`, `reload`. Tab completion covers all arguments.

### Limits

- The plugin id stays `BloodMoonPlus` (data folder, PlaceholderAPI prefix, permission nodes), so existing installs upgrade by swapping the jar.
- Fixed night needs worlds with their own clock; the Nether and the End skip the time lock.
- WorldGuard sync works through console commands on `__global__` only; region flags are not touched.

### Releases

Every `v*` tag builds the jar in GitHub Actions and publishes a release with the notes from [CHANGELOG.md](CHANGELOG.md). Build locally with `python build.py` (needs JDK 21 and the Paper API on the classpath, see the workflow).

### License

Proprietary, see [LICENSE](LICENSE). Based on BloodMoonPlus 1.0.1; rights to the original code stay with its authors.

---

<a id="русский"></a>

## Русский

### Зачем это появилось

Начиналось как русский перевод BloodMoonPlus 1.0.1. Потом игроки нашли цветовые коды, которые вылезали в описаниях предметов, луну можно было спокойно пересидеть в Незере или на столбе, а боссы то не появлялись, то сносили половину спавна. Чинили по одному куску, и в итоге переписали большую часть логики события, так что теперь это отдельный плагин со своими релизами.

Задумка простая: несколько раз в день ночь становится враждебной сразу для всех, это громко и заметно, и пройти её можно только боем. Прятки наказываются, выход из игры убивает, а всё, что порождает событие, не имеет права ломать то, что построили игроки.

### Как работает

Планировщик запускает событие в заданное время (недельный режим по реальному часовому поясу) или каждые N реальных часов. В выбранных мирах наступает ночь с грозой, появляется полоса с оставшимся временем, вокруг подходящих игроков начинают спавниться орды. Волны растут вместе со сложностью: её задают для каждого слота расписания, и она поднимается сама, если игроки отсиживаются.

Боссы планируются в начале сессии и появляются рядом со случайными участниками. Когда таймер кончается, мир очищается от мобов события, время ставится на рассвет, выжившие получают награды. Сессия сохраняется на диск: после рестарта или перезагрузки посреди луны она продолжается с тем же временем окончания, сложностью и типом луны.

### Возможности

- **Три типа луны.** Кровавая (классика), Чёрная (сильнее, темнее, жёстче боссы) и Фиолетовая (астральные мобы и эффекты), у каждой свои оповещения, цвета и полоса. Запуск по расписанию или вручную.
- **Орды со смешанным составом.** Весовые списки мобов, броня, масштаб здоровья и урона, лимит живых мобов на игрока, особые налётчики (десантники, пироманты, арбалетчики) на высокой сложности.
- **Одиннадцать боссов.** Наземные боссы со способностями (удар о землю, рывок, рёв, призыв, звуковой удар, молнии, притяжение, защита, лечение), а также Дракон Края Чёрной Луны и летающий Иссушитель, которые охотятся на игроков в обычном мире. Топ урона и отдельная таблица наград для лучшего. Боссы не ломают блоки.
- **Жизненный цикл Иссушителя.** Вне луны Иссушитель запрещён в выбранных мирах, во время луны его можно построить. При установленном WorldGuard плагин сам снимает и возвращает `deny-spawn` у `__global__`, в том числе при остановке, рестарте и выключении сервера.
- **Против отсиживания.** Динамическая сложность растёт у тех, кто прячется, и падает у тех, кто дерётся. Гнев измерений бьёт тех, кто пережидает в Незере или Энде, гнев небес сбивает высоко летающих на элитрах, выход из игры во время луны убивает игрока на месте.
- **Лут по редкостям.** Шесть редкостей со счётчиками гаранта, 45 кастомных предметов со способностями, отдельные таблицы для мобов, боссов и выживших. Эпические, легендарные и мифические дропы получают свои строки в чате, звуки, титулы и частицы.
- **Отсчёт и предупреждения.** `/luna countdown 5m dangerous black` даёт игрокам одно атмосферное предупреждение, потом тихий отсчёт и восход луны.
- **Защита ландшафта.** Мобы события и взрывы боссов не ломают блоки, обычные взрывы вне луны работают как обычно.
- **Запрет наблюдателя.** Во время луны обычные игроки не могут перейти в режим наблюдателя, операторы и `bloodmoon.bypass` исключены.
- **PlaceholderAPI.** Около 60 плейсхолдеров `%bloodmoon_...%`: статус, оставшееся время, здоровье босса, статистика сессии, история, счётчики гаранта.
- **Локализация.** Английский, русский и испанский; недостающие ключи берутся из копии внутри jar, поэтому после обновления не всплывают сырые ключи.

### Требования

- Paper или Purpur 1.21.4 и новее (проверено на Purpur 26.3), Java 21.
- Необязательно: PlaceholderAPI, WorldGuard 7 (для синхронизации Иссушителя), Multiverse-Core, MythicMobs (режим боссов `MYTHICMOBS`).

### Настройка с нуля

1. Скачайте `LunarWrath.jar` из последнего релиза и положите в `plugins/`.
2. Запустите сервер один раз. Плагин создаст `plugins/BloodMoonPlus/` с `config.yml`, `bosses.yml`, `items.yml`, `loot.yml` и `locales/`.
3. Выберите язык в `locales/locales.yml` (`Language: ru`, `en` или `es`).
4. В `config.yml`:
   - `worlds`: миры, где проходит луна.
   - `schedule`: `mode: weekly` со временем по дням и `timezone` или `mode: interval` с `interval-hours`.
   - `event`: длительность, фиксированная ночь, защита ландшафта, оповещение о старте, консольные команды на старт, конец и для каждого выжившего (`{world}`, `{player}`).
   - `wither-control`: `enabled`, `allow-only-during-bloodmoon`, `sync-worldguard`, `worlds` и `keep-deny-spawn` для других мобов, которые всегда должны оставаться запрещены в `__global__`.
   - `horde`, `difficulty`, `dynamic-difficulty`, `dimension-wrath`, `elytra-wrath`, `quit-penalty`, `spectator-block`, `block-placement`, `countdown`, `announce`: каждый блок прокомментирован в файле.
5. Боссов настраивают в `bosses.yml` (`abilities` в формате `ТИП:перезарядка:аргументы`), дроп в `loot.yml`.
6. Выполните `/luna reload`, затем `/luna start`, чтобы проверить.

Права:

| Право | По умолчанию | Что даёт |
|---|---|---|
| `bloodmoon.use` | все | `/luna` (статус), `/luna next` |
| `bloodmoon.admin` | оператор | все админские подкоманды |
| `bloodmoon.bypass` | оператор | иммунитет к запрету наблюдателя и гневу |
| `bloodmoon.bypass.blockplace` | оператор | строить, когда `block-placement.allow` выключен |

Основные команды (`/luna`, синонимы `/bm`, `/bloodmoon`): `start [минуты] [сложность] [мир]`, `stop`, `black`, `purple`, `blood`, `countdown <время> [normal|dangerous|lethal] [луна] [мир]`, `boss <id> [игрок|мир]`, `horde`, `difficulty`, `duration`, `dynamic`, `lightning`, `give`, `items`, `chances`, `killmobs`, `next`, `reload`. Автодополнение по Tab есть для всех аргументов.

### Ограничения

- Внутренний идентификатор остаётся `BloodMoonPlus` (папка данных, префикс PlaceholderAPI, права), поэтому существующая установка обновляется простой заменой jar.
- Фиксированная ночь работает только в мирах со своими часами; в Незере и Энде время не фиксируется.
- Синхронизация с WorldGuard идёт консольными командами и только для `__global__`, флаги других регионов не меняются.

### Релизы

Каждый тег `v*` собирает jar в GitHub Actions и публикует релиз с заметками из [CHANGELOG.md](CHANGELOG.md). Локальная сборка: `python build.py` (нужны JDK 21 и Paper API в classpath, см. workflow).

### Лицензия

Проприетарная, см. [LICENSE](LICENSE). Основано на BloodMoonPlus 1.0.1; права на исходный код оригинала остаются за его авторами.
