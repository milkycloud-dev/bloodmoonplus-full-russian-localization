# Changelog

## 1.0.5 (2026-11-19)

- **Standalone Boss Summoning Outside Blood Moon:**
  - `/luna boss <id> [world|player]` now supports summoning bosses anytime on demand, even when Blood Moon is completely inactive.
  - Added support for targeting specific players or worlds directly in the command.
  - Tab completion now auto-completes boss IDs at argument 2, and online players or worlds at argument 3.
- **Enhanced Non-Destructive Wither Boss (`wither_sombra`, `azote_carmesi`):**
  - **Zero Block Destruction Guarantee:** Cancelled `EntityChangeBlockEvent` (prevents wither eating/breaking terrain), cleared `blockList()` with zero yield in `EntityExplodeEvent` and `BlockExplodeEvent`, and disabled explosion fires in `ExplosionPrimeEvent`.
  - **Aggressive Aerial Pursuit:** Flying AI applies continuous vertical and horizontal vectors towards victims, hovering ~3.5 blocks above them.
  - **Three-Headed Targeting:** Main head and both side heads (`Wither.Head.CENTER`, `LEFT`, `RIGHT`) simultaneously lock onto survival/adventure victims.
  - **Multi-Skull Barrage:** Fires spread wither skulls at nearby players with zero explosion yield.
  - **Explosion Shockwave (`EXPLODE` ability):** Powerful radial explosion with soul flames, custom sounds, and player knockback that deals zero block damage.
  - **Survival/Adventure Targeting:** Admins in Survival mode can now engage and test bosses directly, while Spectator and Creative players remain untargetable.

## 1.0.4 (2026-11-18)

- **Countdown Parser & Single Warning Logic:**
  - Fixed parameter parsing in `/luna countdown` when specifying duration units like `5m`, `10m`, `30s` (previously defaulted to 30 seconds due to duration token collision).
  - Implemented single-warning countdown mode: sending strictly one atmospheric warning announcement at scheduled time, with zero periodic chat spam during the countdown until the moon rises (`countdown.periodic-alerts: false`).
- **Non-Destructive Black Moon Bosses:**
  - Added Ender Dragon boss (`dragon_negro` / `Чёрный Дракон Края`): actively hunts players in the Overworld without destroying blocks (cancelled `EntityChangeBlockEvent` and block-cleared explosions).
  - Added Wither boss (`wither_sombra` / `Теневой Исушитель`): hunts players and fires wither skulls without destroying terrain.
  - Registered both bosses in `bosses.yml` with spawn tables and abilities (`/luna boss dragon_negro`, `/luna boss wither_sombra`).
- **Operator (OP) Permissions & Bypass:**
  - Players with OP (`p.isOp()`) are completely exempt and allowed to switch to Spectator mode (`/gm 3`, `/gamemode spectator`) without being kicked.
  - Blood Moon mobs and bosses ignore OP players (`EntityTargetLivingEntityEvent` cancelled).
  - OP players are ignored by Dimension Wrath, Elytra Wrath, Dynamic Difficulty camping tracking, and Quit Penalty.
- **Comprehensive Tab Completion:**
  - Added auto-completion across all subcommands, time formats, warning levels, moon types, and boss identifiers via Tab.

## 1.0.3 (2026-11-18)

- **Moon Types (Black Moon & Purple Moon):**
  - Added support for 3 distinct Moon variations: classic Blood Moon (`blood`), Black Moon (`black`), and Purple Moon (`purple`).
  - Added quick start commands: `/luna black [duration] [diff] [world]`, `/luna purple`, `/luna blood` (with Russian aliases `черная`, `фиолетовая`, `кровавая`).
  - Black Moon features darkness aura, 1.5x mob damage/health multiplier by default, 1.5x horde size multiplier, and purple boss bar.
  - Purple Moon features astral/void theme, 1.3x mob stats, 1.25x movement speed, and dragon breath/portal particles.
  - Fully configurable attributes, announcements, bossbar colors, and multipliers in `config.yml` under `moon-types:`.
- **Three Warning Types (Normal, Dangerous, Lethal):**
  - Added 3 distinct warning levels for planned Blood Moons via `/luna warn <time> [normal|dangerous|lethal] [moon_type] [start]`.
  - Added atmospheric chat broadcasts with gradients, full-screen titles, and custom sound effects.
  - Added support for `start` flag in `/luna warn` to schedule and start countdown automatically.
- **Dynamic Difficulty Escalation (Anti-Camping & Anti-Passivity):**
  - Automatically escalates Blood Moon difficulty (+0.25x per 25s up to x5.0) when players hide in bunkers or avoid combat for > 45 seconds.
  - Spawns punitive hordes around camping players to force engagement.
  - Gradually decreases difficulty (-0.1x) when players actively engage in combat and defeat monsters.
  - Added management commands: `/luna dynamic status`, `/luna dynamic on|off`, `/luna dynamic reset [world]`.
- **Dimension Wrath (Nether & The End Anti-Camping):**
  - Prevents players from evading the event by hiding in Nether or The End.
  - Quadrupled damage (4.0 HP / 2 full hearts every 2 seconds) with targeted lightning strikes and dimension-specific particles.
  - Broadcasts warning alerts to chat every 30 seconds and shows actionbar alerts.
- **Elytra Wrath (Sky Flight Anti-Cheat):**
  - Detects players gliding on Elytra at high altitude (Y >= 90) for > 3 seconds.
  - Strikes with lightning, collapses Elytra flight (`setGliding(false)`), inflicts 4.0 HP damage every 2 seconds, and broadcasts alerts every 30 seconds.
- **Spectator Mode (GM 3) Lockdown:**
  - Blocks `/gm 3`, `/gamemode spectator`, and related commands during countdown and active Blood Moon.
  - Automatically kicks existing spectators back into Survival mode when the event or countdown begins.
- **Timer Adjustment Controls:**
  - Added `/luna time status`, `/luna time add <time> [world]`, `/luna time take <time> [world]`, `/luna time set <time> [world]`.
- **Lightning Control Commands:**
  - Added `/luna lightning storm`, `smite`, `strike`, `stop`, `status`.
- **Scheduled Warning Clean-Up:**
  - Scheduled warnings set strictly to 10 minutes before start (`schedule.warnings-minutes: [10]`), eliminating 5-minute and 1-minute spam.

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
