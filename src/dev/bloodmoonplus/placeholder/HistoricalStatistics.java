package dev.bloodmoonplus.placeholder;

import java.time.Instant;

public record HistoricalStatistics(
   long totalEvents, long totalDeathEvents, long totalUniqueDeaths, long totalBossesSpawned, long totalBossesDefeated, HistoricalStatistics.LastEvent lastEvent
) {
   public static HistoricalStatistics empty() {
      return new HistoricalStatistics(0L, 0L, 0L, 0L, 0L, new HistoricalStatistics.LastEvent("", 0L, 0L, 0L, 0L, 0L, "", "NONE", Instant.EPOCH));
   }

   public static record LastEvent(
      String world,
      long durationSeconds,
      long deathCount,
      long uniqueDeaths,
      long participants,
      long survivors,
      String bossName,
      String bossType,
      Instant endedAt
   ) {
   }
}
