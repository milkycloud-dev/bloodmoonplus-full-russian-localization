package dev.bloodmoonplus;

import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

final class Reg {
   private Reg() {
   }

   static String norm(String s) {
      String k = s.trim().toLowerCase(Locale.ROOT);
      return k.startsWith("minecraft:") ? k.substring(10) : k;
   }

   static Enchantment enchantment(String key) {
      NamespacedKey k = NamespacedKey.fromString(norm(key));
      return k == null ? null : (Enchantment)Registry.ENCHANTMENT.get(k);
   }

   static PotionEffectType effect(String key) {
      NamespacedKey k = NamespacedKey.fromString(norm(key));
      return k == null ? null : (PotionEffectType)Registry.EFFECT.get(k);
   }
}
