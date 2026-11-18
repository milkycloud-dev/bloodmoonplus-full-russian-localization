package dev.bloodmoonplus;

import java.util.Locale;
import net.kyori.adventure.bossbar.BossBar;

public enum MoonType {
   BLOOD("blood", "Кровавая Луна", BossBar.Color.RED),
   BLACK("black", "Чёрная Луна", BossBar.Color.PURPLE),
   PURPLE("purple", "Фиолетовая Луна", BossBar.Color.PURPLE);

   private final String id;
   private final String displayName;
   private final BossBar.Color barColor;

   MoonType(String id, String displayName, BossBar.Color barColor) {
      this.id = id;
      this.displayName = displayName;
      this.barColor = barColor;
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public BossBar.Color getBarColor() {
      return this.barColor;
   }

   public static MoonType fromString(String str) {
      if (str == null) return BLOOD;
      String s = str.trim().toLowerCase(Locale.ROOT);
      if (s.contains("black") || s.contains("черн") || s.contains("чёрн") || s.contains("dark") || s.contains("тьма")) {
         return BLACK;
      }
      if (s.contains("purple") || s.contains("фиолет") || s.contains("violet") || s.contains("void") || s.contains("астрал")) {
         return PURPLE;
      }
      return BLOOD;
   }
}
