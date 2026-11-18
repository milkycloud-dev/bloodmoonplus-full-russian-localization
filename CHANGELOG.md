# Changelog

## 1.0.2 (2026-11-18)

- **Countdown & Warning System:**
  - Added `/luna countdown <seconds> [difficulty] [duration] [world]` command (with Russian aliases `отсчет`, `таймер`, `обратный-отсчет`).
  - Added support for `/luna start in <seconds>` / `/luna start <seconds>s` syntax.
  - Added `/luna cancel` / `/luna countdown cancel` command to abort scheduled countdowns.
  - Added configurable scary sound effects (`entity.wither.ambient`, `entity.ender_dragon.growl`) on warning intervals.
  - Added ticking heartbeat sound (`block.note_block.basedrum`) on the final 5 seconds (5, 4, 3, 2, 1) with accelerating pitch.
- **Hidden Event Parameters (Immersion):**
  - Added `warning.show-parameters: false` toggle (default: `false`).
  - Scheduled warnings, countdown alerts, and start messages no longer leak difficulty multipliers (`x{difficulty}`) or duration, preserving immersion and mystery.
- **Block Placement Fix & Configuration:**
  - Fixed client/server WorldBorder limitation that prevented players from placing blocks during Blood Moon (expanded vignette border to maximum world size 59,999,996 with infinite warning distance).
  - Added configurable `block-placement:` toggle (`allow: true/false`, `exempt-creative: true`, `bypass-permission`, `deny-message`).
- **Full Warning & Announcement Customization:**
  - All start titles, subtitles, chat alerts, and countdown messages are now fully configurable directly in `config.yml` under `event.start-announcement:`, `countdown:`, and `warning:`.
  - Fixed multi-world announcement duplication in `QuitPenaltyManager` (now dispatched per-world without duplicate broadcast spam).
- **Quit Penalty Module ("Blood Moon Evolution"):**
  - Players who disconnect during the active penalty cooldown (default 60 seconds) die immediately upon logout.
  - Added full-screen scary warning title and subtitle with configurable delay and duration.
  - Added terrifying red chat broadcast warning upon Blood Moon start.
  - Added server-wide coward death broadcast when a player logs out and dies during the penalty window.
  - Added reconnect punishment verification via PersistentDataContainer (`bloodmoon:quit_penalized`).
  - Added join warning with remaining danger cooldown timer.
  - Added comprehensive configuration options in `config.yml` under `quit-penalty:` (duration, messages, sounds, creative bypass, drop toggles).
- **Mob & Combat Enhancements:**
  - Fixed mob infighting during hordes (`combat.prevent-mob-infighting: true`).
  - Scaled lightning strike frequency and rain thresholds for intense atmospheric immersion.
  - Unlocked difficulty scaling up to 1000.0 without entity or wave flooding.

## 1.0.1 (2026-11-17)

- Исправлено отображение цветового кода &7 в описании (lore) кастомных предметов (items.yml): амперсанд заменен на тег <gray> для корректного отображения в MiniMessage без артефактов.
- Разблокировано масштабирование сложности до 1000.0 без лагов спавна и перегрузки сущностей.
- Интегрированы уровни мобов и русские кастомные титулы.
- Добавлен модуль эпических эффектов для высоких сложностей.
- Обновлена конфигурация по умолчанию и приложен обновленный бинарный пакет плагина BloodMoonPlus-1.0.1-RU.jar в релизе.

## 1.0.0 (2026-11-11)

First public release of the Russian localization package.

- Clean, standalone Russian localization and configuration files for BloodMoonPlus v1.0.1.
- Verified compatibility, zero style/spelling warnings.
- Pre-built binary package attached in GitHub Releases.
