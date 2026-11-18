package dev.bloodmoonplus.placeholder;

import java.util.UUID;
import org.bukkit.entity.Player;

public record PlayerPlaceholderState(
   UUID playerUuid,
   String playerName,
   boolean participating,
   long participationSeconds,
   long participationPercent,
   boolean survivorEligible,
   String survivorStatus
) {
   public static PlayerPlaceholderState empty(Player p) {
      return new PlayerPlaceholderState(p.getUniqueId(), p.getName(), false, 0L, 0L, false, "placeholder.not-participating");
   }
}
