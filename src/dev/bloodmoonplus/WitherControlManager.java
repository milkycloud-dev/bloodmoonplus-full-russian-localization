package dev.bloodmoonplus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public final class WitherControlManager {
   private final BloodMoonPlus plugin;
   private boolean enabled = true;
   private boolean allowOnlyDuringBloodMoon = true;
   private boolean syncWorldGuard = true;
   private final Set<String> controlledWorlds = new HashSet<>();

   WitherControlManager(BloodMoonPlus plugin) {
      this.plugin = plugin;
   }

   public void reload() {
      FileConfiguration cfg = this.plugin.getConfig();
      this.enabled = cfg.getBoolean("wither-control.enabled", true);
      this.allowOnlyDuringBloodMoon = cfg.getBoolean("wither-control.allow-only-during-bloodmoon", true);
      this.syncWorldGuard = cfg.getBoolean("wither-control.sync-worldguard", true);

      this.controlledWorlds.clear();
      List<String> list = cfg.getStringList("wither-control.worlds");
      if (list == null || list.isEmpty()) {
         this.controlledWorlds.add("world");
      } else {
         for (String s : list) {
            if (s != null && !s.trim().isEmpty()) {
               this.controlledWorlds.add(s.trim());
            }
         }
      }

      this.checkState();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isAllowOnlyDuringBloodMoon() {
      return this.enabled && this.allowOnlyDuringBloodMoon;
   }

   public boolean isControlledWorld(World w) {
      if (!this.enabled || w == null) {
         return false;
      }
      return this.controlledWorlds.contains(w.getName());
   }

   public void checkState() {
      if (!this.enabled || !this.syncWorldGuard) {
         return;
      }
      if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
         return;
      }

      for (String wName : this.controlledWorlds) {
         World w = Bukkit.getWorld(wName);
         if (w != null) {
            boolean active = this.plugin.events() != null && this.plugin.events().isActive(w);
            if (active) {
               this.setWorldGuardDenySpawn(w, false);
            } else {
               this.setWorldGuardDenySpawn(w, true);
            }
         }
      }
   }

   public void onBloodMoonStart(World w) {
      if (!this.enabled || w == null || !this.controlledWorlds.contains(w.getName())) {
         return;
      }
      if (this.syncWorldGuard && Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
         this.setWorldGuardDenySpawn(w, false);
         this.plugin.getLogger().info("WorldGuard: снят флаг deny-spawn для мира '" + w.getName() + "' на время Кровавой Луны.");
      }
   }

   public void onBloodMoonStop(World w) {
      if (!this.enabled || w == null || !this.controlledWorlds.contains(w.getName())) {
         return;
      }
      if (this.syncWorldGuard && Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
         this.setWorldGuardDenySpawn(w, true);
         this.plugin.getLogger().info("WorldGuard: возвращен флаг deny-spawn (minecraft:wither) для мира '" + w.getName() + "' по окончании Кровавой Луны.");
      }
   }

   public void shutdown() {
      if (!this.enabled || !this.syncWorldGuard) {
         return;
      }
      if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
         return;
      }
      for (String wName : this.controlledWorlds) {
         World w = Bukkit.getWorld(wName);
         if (w != null) {
            this.setWorldGuardDenySpawn(w, true);
         }
      }
   }

   private void setWorldGuardDenySpawn(World w, boolean deny) {
      try {
         String cmd;
         if (deny) {
            cmd = "rg flag __global__ -w " + w.getName() + " deny-spawn minecraft:wither";
         } else {
            cmd = "rg flag __global__ -w " + w.getName() + " deny-spawn";
         }
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
      } catch (Exception ex) {
         this.plugin.getLogger().warning("Ошибка переключения флага WorldGuard deny-spawn: " + ex.getMessage());
      }
   }
}
