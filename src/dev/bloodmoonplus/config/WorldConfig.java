package dev.bloodmoonplus.config;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class WorldConfig {
   private final String worldName;
   private final YamlConfiguration yml;
   private final boolean permanentBloodMoon;
   private final boolean bedNotAllowed;
   private final boolean dyingLossesInventory;
   private final boolean dyingLossesExperience;
   private final WorldConfig.SurvivorPolicy survivor;
   private final WorldConfig.BossConfig boss;
   private final List<String> commandsOnStart;
   private final List<String> commandsOnEnd;

   public WorldConfig(String worldName, YamlConfiguration yml) {
      this.worldName = worldName;
      this.yml = yml;
      this.permanentBloodMoon = yml.getBoolean("WorldPermanent", false);
      this.bedNotAllowed = yml.getBoolean("BedNotAllowed", true);
      this.dyingLossesInventory = yml.getBoolean("DyingLossesInventory", false);
      this.dyingLossesExperience = yml.getBoolean("DyingLossesExperience", false);
      this.survivor = parseSurvivor(yml.getConfigurationSection("SurvivorRewards"));
      this.boss = parseBoss(yml.getConfigurationSection("Boss"));
      this.commandsOnStart = yml.getStringList("CommandsOnStart");
      this.commandsOnEnd = yml.getStringList("CommandsOnEnd");
   }

   private static WorldConfig.SurvivorPolicy parseSurvivor(ConfigurationSection s) {
      return s == null
         ? new WorldConfig.SurvivorPolicy(false, true, true, 60, true, false, false, true, List.of(), List.of())
         : new WorldConfig.SurvivorPolicy(
            s.getBoolean("Enabled", false),
            s.getBoolean("RequireOnlineAtEnd", true),
            s.getBoolean("IncludeLateJoiners", true),
            s.getInt("MinimumParticipationSeconds", 60),
            s.getBoolean("DisqualifyOnDeath", true),
            s.getBoolean("DisqualifyOnWorldLeave", false),
            s.getBoolean("DisqualifyOnDisconnect", false),
            s.getBoolean("RewardOncePerSession", true),
            s.getStringList("Messages"),
            s.getStringList("Commands")
         );
   }

   private static WorldConfig.BossConfig parseBoss(ConfigurationSection s) {
      if (s == null) {
         return new WorldConfig.BossConfig(
            "VANILLA",
            true,
            "%boss_name% &c%boss_health%&7/&c%boss_max_health%",
            "RED",
            "SEGMENTED_10",
            "NEARBY",
            64,
            true,
            false,
            "KILLER",
            true,
            true,
            List.of(),
            false,
            "BloodMoonBoss",
            true,
            false,
            true
         );
      } else {
         String mode = s.getString("Mode", "VANILLA");
         ConfigurationSection bar = s.getConfigurationSection("VanillaBossBar");
         ConfigurationSection rew = s.getConfigurationSection("Rewards");
         ConfigurationSection myt = s.getConfigurationSection("MythicMobs");
         return new WorldConfig.BossConfig(
            mode,
            bar != null && bar.getBoolean("Enabled", true),
            bar != null ? bar.getString("Title", "%boss_name% &c%boss_health%&7/&c%boss_max_health%") : "%boss_name%",
            bar != null ? bar.getString("Color", "RED") : "RED",
            bar != null ? bar.getString("Style", "SEGMENTED_10") : "SEGMENTED_10",
            bar != null ? bar.getString("Audience", "NEARBY") : "NEARBY",
            bar != null ? bar.getInt("ViewDistance", 64) : 64,
            bar != null && bar.getBoolean("ShowHealthNumbers", true),
            rew != null && rew.getBoolean("Enabled", false),
            rew != null ? rew.getString("Mode", "KILLER") : "KILLER",
            rew != null && rew.getBoolean("RequirePlayerKiller", true),
            rew != null && rew.getBoolean("RewardOnce", true),
            rew != null ? rew.getStringList("Commands") : List.of(),
            myt != null && myt.getBoolean("Enabled", false),
            myt != null ? myt.getString("InternalName", "BloodMoonBoss") : "BloodMoonBoss",
            myt != null && myt.getBoolean("UseMythicMobsRewards", true),
            myt != null && myt.getBoolean("RunBloodMoonRewardCommands", false),
            myt != null && myt.getBoolean("FallbackToVanilla", true)
         );
      }
   }

   public String worldName() {
      return this.worldName;
   }

   public boolean permanentBloodMoon() {
      return this.permanentBloodMoon;
   }

   public boolean bedNotAllowed() {
      return this.bedNotAllowed;
   }

   public boolean dyingLossesInventory() {
      return this.dyingLossesInventory;
   }

   public boolean dyingLossesExperience() {
      return this.dyingLossesExperience;
   }

   public WorldConfig.SurvivorPolicy survivor() {
      return this.survivor;
   }

   public WorldConfig.BossConfig boss() {
      return this.boss;
   }

   public List<String> commandsOnStart() {
      return this.commandsOnStart;
   }

   public List<String> commandsOnEnd() {
      return this.commandsOnEnd;
   }

   public YamlConfiguration raw() {
      return this.yml;
   }

   public static record BossConfig(
      String mode,
      boolean barEnabled,
      String barTitle,
      String barColor,
      String barStyle,
      String barAudience,
      int barViewDistance,
      boolean showHealthNumbers,
      boolean rewardsEnabled,
      String rewardsMode,
      boolean requirePlayerKiller,
      boolean rewardOnce,
      List<String> rewardCommands,
      boolean mythicEnabled,
      String mythicInternalName,
      boolean mythicUseMythicRewards,
      boolean mythicRunBloodMoonRewards,
      boolean mythicFallback
   ) {
   }

   public static record SurvivorPolicy(
      boolean enabled,
      boolean requireOnlineAtEnd,
      boolean includeLateJoiners,
      int minParticipationSeconds,
      boolean disqualifyOnDeath,
      boolean disqualifyOnWorldLeave,
      boolean disqualifyOnDisconnect,
      boolean rewardOncePerSession,
      List<String> messages,
      List<String> commands
   ) {
   }
}
