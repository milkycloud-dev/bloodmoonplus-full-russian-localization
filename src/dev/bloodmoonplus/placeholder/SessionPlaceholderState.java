package dev.bloodmoonplus.placeholder;

import java.util.UUID;

public record SessionPlaceholderState(
   UUID sessionUuid, String worldName, long timeRemainingSeconds, long deaths, long uniqueDeaths, long participantsCurrent, long survivorsCurrent
) {
   public static SessionPlaceholderState empty(String world) {
      return new SessionPlaceholderState(new UUID(0L, 0L), world, 0L, 0L, 0L, 0L, 0L);
   }
}
