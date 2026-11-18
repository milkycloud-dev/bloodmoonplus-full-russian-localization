package dev.bloodmoonplus.survivor;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class RewardContext {
   private static final Pattern PLACEHOLDER = Pattern.compile("%([a-z_]+)(?::([^%]+))?%", 2);
   private final String worldName;
   private final String sessionUuid;
   private final long startTime;
   private final long endTime;
   private final long durationSeconds;
   private final long participantCount;
   private final long deathCount;
   private final long survivorCount;
   private final String bossName;
   private final String bossType;
   private final String bossKiller;
   private final UUID bossKillerUuid;
   private final String bossWorld;
   private final double bossX;
   private final double bossY;
   private final double bossZ;
   private final double bossHealth;
   private final double bossMaxHealth;
   private final double bossHealthPercent;

   public RewardContext(
      World world,
      String sessionUuid,
      long startMs,
      long endMs,
      long participants,
      long deaths,
      long survivors,
      String bossName,
      String bossType,
      String bossKiller,
      UUID bossKillerUuid,
      String bossWorld,
      double bossX,
      double bossY,
      double bossZ,
      double bossHealth,
      double bossMaxHealth
   ) {
      this.worldName = world == null ? "" : world.getName();
      this.sessionUuid = sessionUuid;
      this.startTime = startMs;
      this.endTime = endMs;
      this.durationSeconds = Math.max(0L, (endMs - startMs) / 1000L);
      this.participantCount = participants;
      this.deathCount = deaths;
      this.survivorCount = survivors;
      this.bossName = bossName;
      this.bossType = bossType;
      this.bossKiller = bossKiller;
      this.bossKillerUuid = bossKillerUuid;
      this.bossWorld = bossWorld;
      this.bossX = bossX;
      this.bossY = bossY;
      this.bossZ = bossZ;
      this.bossHealth = bossHealth;
      this.bossMaxHealth = bossMaxHealth;
      this.bossHealthPercent = bossMaxHealth > 0.0 ? bossHealth * 100.0 / bossMaxHealth : 0.0;
   }

   public String resolve(String text, Player target) {
      if (text == null) {
         return "";
      } else {
         Matcher m = PLACEHOLDER.matcher(text);
         StringBuilder sb = new StringBuilder();

         while (m.find()) {
            String key = m.group(1).toLowerCase();
            String opt = m.group(2);

            String replacement = switch (key) {
               case "player", "player_name" -> target == null ? "" : target.getName();
               case "player_uuid" -> target == null ? "" : target.getUniqueId().toString();
               case "world", "world_name" -> this.worldName;
               case "world_uuid" -> this.worldName.isEmpty() ? "" : Bukkit.getWorld(this.worldName).getUID().toString();
               case "session_uuid" -> this.sessionUuid;
               case "start_time" -> String.valueOf(this.startTime);
               case "end_time" -> String.valueOf(this.endTime);
               case "duration_seconds" -> String.valueOf(this.durationSeconds);
               case "participant_count" -> String.valueOf(this.participantCount);
               case "survivor_count" -> String.valueOf(this.survivorCount);
               case "death_count" -> String.valueOf(this.deathCount);
               case "participation_seconds" -> opt;
               case "participation_percent" -> opt;
               case "died" -> opt;
               case "survived" -> opt;
               case "boss_name" -> this.bossName;
               case "boss_type" -> this.bossType;
               case "boss_uuid" -> "";
               case "boss_killer" -> this.bossKiller;
               case "boss_killer_uuid" -> this.bossKillerUuid == null ? "" : this.bossKillerUuid.toString();
               case "boss_world" -> this.bossWorld == null ? "" : this.bossWorld;
               case "boss_x" -> String.valueOf((int)this.bossX);
               case "boss_y" -> String.valueOf((int)this.bossY);
               case "boss_z" -> String.valueOf((int)this.bossZ);
               case "boss_health" -> String.valueOf((int)this.bossHealth);
               case "boss_max_health" -> String.valueOf((int)this.bossMaxHealth);
               case "boss_health_percent" -> String.valueOf((int)this.bossHealthPercent);
               default -> m.group(0);
            };
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
         }

         m.appendTail(sb);
         String result = sb.toString().replace("$w", this.worldName).replace("$b", this.bossName);
         if (target != null) {
            result = result.replace("$p", target.getName());
         }

         return result;
      }
   }

   public static RewardContext.ParsedCommand parse(String raw) {
      if (raw == null) {
         return null;
      } else {
         String trimmed = raw.trim();
         if (trimmed.isEmpty()) {
            return null;
         } else {
            if (trimmed.startsWith("/")) {
               trimmed = trimmed.substring(1);
            }

            RewardContext.Audience aud = RewardContext.Audience.CONSOLE;
            if (trimmed.endsWith(";s")) {
               aud = RewardContext.Audience.CONSOLE_ONCE;
               trimmed = trimmed.substring(0, trimmed.length() - 2).trim();
            } else if (trimmed.endsWith(";f")) {
               aud = RewardContext.Audience.CONSOLE_PER_PLAYER;
               trimmed = trimmed.substring(0, trimmed.length() - 2).trim();
            } else if (trimmed.endsWith(";p")) {
               aud = RewardContext.Audience.PLAYER_EXEC;
               trimmed = trimmed.substring(0, trimmed.length() - 2).trim();
            }

            return new RewardContext.ParsedCommand(trimmed, aud);
         }
      }
   }

   public static enum Audience {
      CONSOLE_ONCE,
      CONSOLE_PER_PLAYER,
      PLAYER_EXEC,
      CONSOLE;
   }

   public static record ParsedCommand(String command, RewardContext.Audience audience) {
   }
}
