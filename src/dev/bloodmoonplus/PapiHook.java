package dev.bloodmoonplus;

import dev.bloodmoonplus.placeholder.BossPlaceholderSnapshot;
import dev.bloodmoonplus.placeholder.BossPlaceholderState;
import dev.bloodmoonplus.placeholder.HistoricalStatistics;
import dev.bloodmoonplus.placeholder.PlayerPlaceholderState;
import dev.bloodmoonplus.placeholder.SessionPlaceholderState;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class PapiHook extends PlaceholderExpansion {
   private final BloodMoonPlus plugin;

   PapiHook(BloodMoonPlus plugin) {
      this.plugin = plugin;
   }

   public String getIdentifier() {
      return "bloodmoon";
   }

   public String getAuthor() {
      return "BloodMoonPlus";
   }

   public String getVersion() {
      return this.plugin.getPluginMeta().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public String onRequest(OfflinePlayer player, String params) {
      if (params != null && !params.isEmpty()) {
         String[] parts = params.toLowerCase(Locale.ROOT).split("_", 3);
         String p = parts[0];
         String sub = parts.length > 1 ? parts[1] : "";
         String extra = parts.length > 2 ? parts[2] : "";
         String worldName = null;
         if (player instanceof Player p2) {
            worldName = p2.getWorld().getName();
         }

         SessionPlaceholderState session = null;
         EventManager.Session global = this.plugin.events().globalSession();
         if (global != null) {
            session = this.plugin.snapshots().sessionFor(global.world.getName());
         }

         boolean active = session != null;
         PlayerPlaceholderState playerState = player != null && player.isOnline() ? this.plugin.snapshots().playerFor(player.getUniqueId()) : null;
         BossPlaceholderSnapshot bossSnap = this.plugin.snapshots().boss();
         if (bossSnap == null) {
            bossSnap = BossPlaceholderSnapshot.empty();
         }

         HistoricalStatistics hist = this.plugin.snapshots().history();

         return switch (p) {
            case "active" -> active ? "true" : "false";
            case "activeformatted" -> active ? this.plugin.locales().get("placeholder.active") : this.plugin.locales().get("placeholder.inactive");
            case "world" -> active ? session.worldName() : this.plugin.locales().get("placeholder.none");
            case "time" -> {
               switch (sub) {
                  case "remaining":
                  case "remainingseconds":
                     yield String.valueOf(active ? session.timeRemainingSeconds() : 0L);
                  case "remainingformatted":
                     yield this.formatTime(active ? session.timeRemainingSeconds() : 0L);
                  default:
                     yield null;
               }
            }
            case "boss" -> {
               switch (sub) {
                  case "alive":
                     yield String.valueOf(bossSnap.state() == BossPlaceholderState.ALIVE);
                  case "name":
                     yield bossSnap.state() == BossPlaceholderState.ALIVE ? bossSnap.bossName() : this.plugin.locales().get("placeholder.no-boss");
                  case "type":
                     yield bossSnap.bossType();
                  case "health":
                     yield String.valueOf((int)bossSnap.health());
                  case "max":
                  case "max_health":
                     yield String.valueOf((int)bossSnap.maxHealth());
                  case "health_percent":
                     yield String.valueOf(bossSnap.healthPercent());
                  case "health_formatted":
                     yield bossSnap.healthPercent() + "%";
                  case "state":
                     yield bossSnap.state().name();
                  case "state_formatted":
                     yield this.formatBossState(bossSnap.state());
                  case "display":
                     switch (extra) {
                        case "line_1":
                           yield this.formatDisplayLine1(bossSnap);
                        case "line_2":
                           yield this.formatDisplayLine2(bossSnap);
                        case "line_3":
                           yield this.formatDisplayLine3(bossSnap);
                        default:
                           yield null;
                     }
                  default:
                     yield null;
               }
            }
            case "participating" -> {
               switch (sub) {
                  case "":
                     yield String.valueOf(playerState != null && playerState.participating());
                  case "seconds":
                     yield String.valueOf(playerState != null ? playerState.participationSeconds() : 0L);
                  case "percent":
                     yield String.valueOf(playerState != null ? playerState.participationPercent() : 0L);
                  case "formatted":
                     yield this.formatTime(playerState != null ? playerState.participationSeconds() : 0L);
                  default:
                     yield null;
               }
            }
            case "survivor" -> {
               switch (sub) {
                  case "eligible":
                     yield String.valueOf(playerState != null && playerState.survivorEligible());
                  case "status":
                     yield playerState != null
                        ? this.plugin.locales().get(playerState.survivorStatus(), playerState.playerName())
                        : this.plugin.locales().get("placeholder.not-participating");
                  default:
                     yield null;
               }
            }
            case "death", "deaths" -> sub.isEmpty() ? String.valueOf(active ? session.deaths() : 0L) : null;
            case "unique", "unique_deaths" -> sub.equals("deaths") ? String.valueOf(active ? session.uniqueDeaths() : 0L) : null;
            case "participants", "participants_current" -> String.valueOf(active ? session.participantsCurrent() : 0L);
            case "survivors", "survivors_current" -> String.valueOf(active ? session.survivorsCurrent() : 0L);
            case "total" -> {
               switch (sub) {
                  case "events":
                     yield String.valueOf(hist.totalEvents());
                  case "death_events":
                     yield String.valueOf(hist.totalDeathEvents());
                  case "unique_deaths":
                     yield String.valueOf(hist.totalUniqueDeaths());
                  case "bosses_spawned":
                     yield String.valueOf(hist.totalBossesSpawned());
                  case "bosses_defeated":
                     yield String.valueOf(hist.totalBossesDefeated());
                  default:
                     yield null;
               }
            }
            case "last" -> {
               switch (sub) {
                  case "event_world":
                     yield hist.lastEvent().world().isEmpty() ? this.plugin.locales().get("placeholder.none") : hist.lastEvent().world();
                  case "event_duration_seconds":
                     yield String.valueOf(hist.lastEvent().durationSeconds());
                  case "event_duration_formatted":
                     yield this.formatTime(hist.lastEvent().durationSeconds());
                  case "event_death_count":
                     yield String.valueOf(hist.lastEvent().deathCount());
                  case "event_unique_deaths":
                     yield String.valueOf(hist.lastEvent().uniqueDeaths());
                  case "event_participants":
                     yield String.valueOf(hist.lastEvent().participants());
                  case "event_survivors":
                     yield String.valueOf(hist.lastEvent().survivors());
                  case "event_ended_at":
                     yield hist.lastEvent().endedAt() != null && !hist.lastEvent().endedAt().equals(Instant.EPOCH)
                        ? DateTimeFormatter.ISO_INSTANT.format(hist.lastEvent().endedAt())
                        : this.plugin.locales().get("placeholder.none");
                  case "boss_name":
                     yield hist.lastEvent().bossName().isEmpty() ? this.plugin.locales().get("placeholder.no-boss") : hist.lastEvent().bossName();
                  case "boss_type":
                     yield hist.lastEvent().bossType();
                  default:
                     yield null;
               }
            }
            case "drop", "drop_chance" -> sub.equals("chance") && !extra.isEmpty() ? this.formatDropChance(extra) : null;
            case "pity" -> {
               if (!sub.isEmpty()) {
                  UUID pid = player != null ? player.getUniqueId() : null;
                  if (pid != null) {
                     yield String.valueOf(this.plugin.data().pity(pid, sub.toUpperCase(Locale.ROOT)));
                  }
               }

               yield null;
            }
            case "bosses_defeated_session" -> {
               long n = 0L;
               if (global != null && global.kills.size() > 0) {
                  n = this.countDefeatedBosses();
               }

               yield String.valueOf(n);
            }
            case "horde" -> {
               switch (sub) {
                  case "active":
                     yield String.valueOf(active);
                  case "count":
                     if (!active) {
                        yield "0";
                     } else {
                        long approx = Math.max(0L, session.participantsCurrent() * 8L);
                        yield String.valueOf(approx);
                     }
                  default:
                     yield null;
               }
            }
            case "abilities" -> sub.equals("count") && player instanceof Player pp ? String.valueOf(this.countAbilities(pp)) : null;
            case "items" -> sub.equals("unique_owned") && player instanceof Player pp2 ? String.valueOf(this.countUniqueItems(pp2)) : null;
            case "mythic" -> sub.equals("active") ? String.valueOf(Bukkit.getPluginManager().getPlugin("MythicMobs") != null) : null;
            case "permanent" -> sub.equals("world") && worldName != null
            ? String.valueOf(this.plugin.worldConfigs().get(worldName).permanentBloodMoon())
            : "false";
            case "event" -> sub.equals("elapsed") && active ? this.formatTime(1200L - session.timeRemainingSeconds()) : "0:00";
            default -> null;
         };
      } else {
         return null;
      }
   }

   private String formatTime(long sec) {
      long h = sec / 3600L;
      long m = sec % 3600L / 60L;
      long s = sec % 60L;
      return h > 0L ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
   }

   private String formatBossState(BossPlaceholderState s) {
      return switch (s) {
         case ALIVE -> this.plugin.locales().get("placeholder.boss-state-alive");
         case DEFEATED -> this.plugin.locales().get("placeholder.boss-state-defeated");
         case NOT_SPAWNED -> this.plugin.locales().get("placeholder.boss-state-not-spawned");
         case NONE -> this.plugin.locales().get("placeholder.boss-state-none");
      };
   }

   private String formatDisplayLine1(BossPlaceholderSnapshot s) {
      if (s.state() == BossPlaceholderState.ALIVE) {
         return this.plugin.locales().get("placeholder.boss-display-name", "name", s.bossName());
      } else if (s.state() == BossPlaceholderState.DEFEATED) {
         return this.plugin.locales().get("placeholder.boss-display-defeated", "name", s.bossName());
      } else {
         return s.state() == BossPlaceholderState.NOT_SPAWNED ? this.plugin.locales().get("placeholder.boss-display-not-spawned") : "";
      }
   }

   private String formatDisplayLine2(BossPlaceholderSnapshot s) {
      return s.state() == BossPlaceholderState.ALIVE ? this.plugin.locales().get("placeholder.boss-display-type", "type", s.bossType()) : "";
   }

   private String formatDisplayLine3(BossPlaceholderSnapshot s) {
      return s.state() == BossPlaceholderState.ALIVE ? this.plugin.locales().get("placeholder.boss-display-health", "health", s.healthPercent() + "%") : "";
   }

   private String formatDropChance(String itemId) {
      ItemFactory.ItemDef def = this.plugin.items().get(itemId);
      if (def == null) {
         return "?";
      } else {
         for (LootManager.Table table : this.plugin.loot().allTables()) {
            for (LootManager.Entry entry : table.effective) {
               if (entry.item().equals(itemId)) {
                  return Math.round(entry.dropChance() * 100.0) + "%";
               }
            }
         }

         return "100%";
      }
   }

   private long countDefeatedBosses() {
      return this.plugin.bosses().defeatedCount();
   }

   private int countAbilities(Player p) {
      int n = 0;

      for (ItemStack item : p.getInventory().getContents()) {
         if (item != null && item.hasItemMeta()) {
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(Keys.ITEM_ABILITIES, PersistentDataType.STRING)) {
               n++;
            }
         }
      }

      return n;
   }

   private int countUniqueItems(Player p) {
      int n = 0;

      for (ItemStack item : p.getInventory().getContents()) {
         if (item != null && item.hasItemMeta()) {
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(Keys.ITEM_SERIAL, PersistentDataType.INTEGER)) {
               n++;
            }
         }
      }

      return n;
   }
}
