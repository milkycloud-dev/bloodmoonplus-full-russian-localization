package dev.bloodmoonplus;

import dev.bloodmoonplus.i18n.LocaleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Messages {
   private final BloodMoonPlus plugin;
   private final LocaleManager locales;

   Messages(BloodMoonPlus plugin, LocaleManager locales) {
      this.plugin = plugin;
      this.locales = locales;
   }

   void reload() {
      this.locales.reload();
   }

   public String raw(String key) {
      return this.locales.get(key);
   }

   public String fill(String text, String... kv) {
      return LocaleManager.fill(text, kv);
   }

   public Component get(String key, String... kv) {
      return Util.mini(this.locales.get(key, kv));
   }

   public Component prefixed(String key, String... kv) {
      return Util.mini(this.locales.get("prefix") + this.locales.get(key, kv));
   }

   public void send(CommandSender to, String key, String... kv) {
      to.sendMessage(this.prefixed(key, kv));
   }

   public void world(World w, String key, String... kv) {
      Component c = this.prefixed(key, kv);

      for (Player p : w.getPlayers()) {
         p.sendMessage(c);
      }
   }

   public void all(String key, String... kv) {
      Bukkit.broadcast(this.prefixed(key, kv));
   }

   public void allDirect(String text) {
      Bukkit.broadcast(Util.mini(this.locales.get("prefix") + text));
   }

   public boolean announce(String category, boolean def) {
      return this.plugin.getConfig().getBoolean("announce." + category, def);
   }
}
