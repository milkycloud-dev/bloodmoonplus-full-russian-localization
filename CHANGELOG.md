# Changelog

## 1.0.2 (2026-11-18)

- Added Quit Penalty module ("Blood Moon Evolution"): players who disconnect during the active penalty cooldown (default 60 seconds) die immediately upon logout.
- Added full-screen scary warning title and subtitle with configurable delay and duration.
- Added terrifying red chat broadcast warning upon Blood Moon start.
- Added server-wide coward death broadcast when a player logs out and dies during the penalty window.
- Added reconnect punishment verification via PersistentDataContainer (`bloodmoon:quit_penalized`).
- Added join warning with remaining danger cooldown timer.
- Added comprehensive configuration options in `config.yml` under `quit-penalty:` (duration, messages, sounds, creative bypass, drop toggles).

## 1.0.1 (2026-11-18)

- Unlocked difficulty scaling up to 1000.0 without wave frequency or entity flooding issues.
- Integrated mob levels and Russian custom titles.
- Added epic effects module for high difficulty events.

## 1.0.0 (2026-11-11)

First public release of the Russian localization package.

- Clean, standalone Russian localization and configuration files for BloodMoonPlus v1.0.1.
- Verified compatibility, zero style/spelling warnings.
- Pre-built binary package attached in GitHub Releases.
