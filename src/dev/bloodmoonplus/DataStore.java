package dev.bloodmoonplus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class DataStore {
   private final BloodMoonPlus plugin;
   private final File file;
   private YamlConfiguration cfg;
   private final ReentrantLock lock = new ReentrantLock();
   private boolean dirty;

   DataStore(BloodMoonPlus plugin) {
      this.plugin = plugin;
      plugin.getDataFolder().mkdirs();
      this.file = new File(plugin.getDataFolder(), "data.yml");
      this.cfg = YamlConfiguration.loadConfiguration(this.file);
   }

   private void syncReload() {
      this.lock.lock();

      try {
         this.cfg = YamlConfiguration.loadConfiguration(this.file);
      } finally {
         this.lock.unlock();
      }
   }

   void saveNow() {
      this.lock.lock();

      try {
         try {
            this.cfg.save(this.file);
         } catch (IOException var5) {
            this.plugin.getLogger().warning("No se pudo guardar data.yml: " + var5.getMessage());
         }

         this.dirty = false;
      } finally {
         this.lock.unlock();
      }
   }

   void flushIfDirty() {
      if (this.dirty) {
         this.saveNow();
      }
   }

   boolean isFired(String key) {
      this.lock.lock();

      boolean var2;
      try {
         var2 = this.cfg.getStringList("fired").contains(key);
      } finally {
         this.lock.unlock();
      }

      return var2;
   }

   void markFired(String key) {
      this.lock.lock();

      try {
         List<String> l = new ArrayList<>(this.cfg.getStringList("fired"));
         l.add(key);

         while (l.size() > 80) {
            l.remove(0);
         }

         this.cfg.set("fired", l);
         this.dirty = true;
      } finally {
         this.lock.unlock();
      }
   }

   int created(String itemId) {
      this.lock.lock();

      int var2;
      try {
         var2 = this.cfg.getInt("created." + itemId, 0);
      } finally {
         this.lock.unlock();
      }

      return var2;
   }

   int nextSerial(String itemId) {
      this.lock.lock();

      int var3;
      try {
         int n = this.created(itemId) + 1;
         this.cfg.set("created." + itemId, n);
         this.dirty = true;
         var3 = n;
      } finally {
         this.lock.unlock();
      }

      return var3;
   }

   int pity(UUID u, String rarity) {
      this.lock.lock();

      int var3;
      try {
         var3 = this.cfg.getInt("pity." + u + "." + rarity, 0);
      } finally {
         this.lock.unlock();
      }

      return var3;
   }

   void setPity(UUID u, String rarity, int v) {
      this.lock.lock();

      try {
         this.cfg.set("pity." + u + "." + rarity, v < 0 ? null : v);
         this.dirty = true;
      } finally {
         this.lock.unlock();
      }
   }

   void saveActive(String world, long start, long end, double diff, MoonType type) {
      this.lock.lock();

      try {
         String b = "active." + world;
         this.cfg.set(b + ".start", start);
         this.cfg.set(b + ".end", end);
         this.cfg.set(b + ".difficulty", diff);
         this.cfg.set(b + ".type", (type != null ? type : MoonType.BLOOD).getId());
         this.dirty = true;
      } finally {
         this.lock.unlock();
      }
   }

   void clearActive(String world) {
      this.lock.lock();

      try {
         this.cfg.set("active." + world, null);
         this.dirty = true;
      } finally {
         this.lock.unlock();
      }
   }

   ConfigurationSection active() {
      this.lock.lock();

      ConfigurationSection var1;
      try {
         var1 = this.cfg.getConfigurationSection("active");
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   Long getLastIntervalFire() {
      this.lock.lock();

      Long var1;
      try {
         var1 = this.cfg.getLong("interval.lastFire");
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   void setLastIntervalFire(long ms) {
      this.lock.lock();

      try {
         this.cfg.set("interval.lastFire", ms);
         this.dirty = true;
      } finally {
         this.lock.unlock();
      }
   }
}
