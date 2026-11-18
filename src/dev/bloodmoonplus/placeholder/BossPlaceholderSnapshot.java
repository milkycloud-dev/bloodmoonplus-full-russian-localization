package dev.bloodmoonplus.placeholder;

import java.util.UUID;
import org.bukkit.entity.LivingEntity;

public record BossPlaceholderSnapshot(
   UUID bossUuid, String bossName, String bossType, double health, double maxHealth, int healthPercent, BossPlaceholderState state
) {
   public static BossPlaceholderSnapshot empty() {
      return new BossPlaceholderSnapshot(new UUID(0L, 0L), "", "NONE", 0.0, 0.0, 0, BossPlaceholderState.NONE);
   }

   public static BossPlaceholderSnapshot fromEntity(LivingEntity e, double maxHealth, String name, String type, BossPlaceholderState state) {
      double h = Math.max(0.0, e.getHealth());
      double m = Math.max(1.0, maxHealth);
      int pct = (int)Math.min(100L, Math.max(0L, Math.round(h * 100.0 / m)));
      return new BossPlaceholderSnapshot(e.getUniqueId(), name == null ? "" : name, type == null ? "VANILLA" : type, h, m, pct, state);
   }
}
