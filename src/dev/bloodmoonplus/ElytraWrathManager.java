package dev.bloodmoonplus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public final class ElytraWrathManager implements Listener {
   private final BloodMoonPlus plugin;
   private BukkitTask task;

   private boolean enabled = true;
   private double minAltitude = 90.0;
   private int flightTimeSeconds = 3;
   private int strikeIntervalSeconds = 2;
   private double damageAmount = 4.0;
   private boolean disruptFlight = true;
   private int broadcastIntervalSeconds = 30;
   private boolean exemptCreative = true;
   private String bypassPermission = "bloodmoon.bypass.elytrawrath";

   private String broadcastMessage;
   private String actionbarMessage;
   private String deathMessage;

   private final Map<UUID, Long> glidingStartMs = new ConcurrentHashMap<>();
   private final Map<UUID, Long> lastStrikeSec = new ConcurrentHashMap<>();
   private final Map<UUID, Long> lastBroadcastSec = new ConcurrentHashMap<>();
   private final Map<UUID, Long> lastDamageMs = new ConcurrentHashMap<>();

   public ElytraWrathManager(BloodMoonPlus plugin) {
      this.plugin = plugin;
      this.reload();
      this.startTask();
   }

   public void reload() {
      FileConfiguration cfg = this.plugin.getConfig();
      this.enabled = cfg.getBoolean("elytra-wrath.enabled", true);
      this.minAltitude = cfg.getDouble("elytra-wrath.min-altitude", 90.0);
      this.flightTimeSeconds = Math.max(0, cfg.getInt("elytra-wrath.flight-time-seconds", 3));
      this.strikeIntervalSeconds = Math.max(1, cfg.getInt("elytra-wrath.strike-interval-seconds", 2));
      this.damageAmount = Math.max(0.1, cfg.getDouble("elytra-wrath.damage", 4.0));
      this.disruptFlight = cfg.getBoolean("elytra-wrath.disrupt-flight", true);
      this.broadcastIntervalSeconds = Math.max(1, cfg.getInt("elytra-wrath.broadcast-interval-seconds", 30));
      this.exemptCreative = cfg.getBoolean("elytra-wrath.exempt-creative", true);
      this.bypassPermission = cfg.getString("elytra-wrath.bypass-permission", "bloodmoon.bypass.elytrawrath");

      this.broadcastMessage = cfg.getString(
         "elytra-wrath.broadcast-message",
         "<gradient:#00e5ff:#0055ff><bold>⚡ ГНЕВ НЕБЕС:</bold></gradient> <red>В небесах нет спасения от Кровавой Луны! Игрок <yellow>{player}</yellow> поражен небесной молнией за попытку летать на элитрах!</red>"
      );
      this.actionbarMessage = cfg.getString(
         "elytra-wrath.actionbar-message",
         "<gradient:#00e5ff:#0088ff><bold>⚡ Небесный разряд Кровавой Луны сбивает вас с небес! (-{hearts} ❤)</bold></gradient>"
      );
      this.deathMessage = cfg.getString(
         "elytra-wrath.death-message",
         "<gradient:#00e5ff:#0055ff><bold>☠ Игрок <yellow>{player}</yellow> пытался скрыться от Кровавой Луны в небесах на элитрах, но был испепелен молнией!</gradient>"
      );
   }

   public void startTask() {
      if (this.task != null) {
         this.task.cancel();
      }
      this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
   }

   public void shutdown() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
      this.glidingStartMs.clear();
      this.lastStrikeSec.clear();
      this.lastBroadcastSec.clear();
      this.lastDamageMs.clear();
   }

   private boolean isExempt(Player p) {
      if (p == null) return true;
      if (p.isOp()) return true;
      if (this.exemptCreative && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR)) {
         return true;
      }
      return this.bypassPermission != null && !this.bypassPermission.isEmpty() && p.hasPermission(this.bypassPermission);
   }

   private void tick() {
      if (!this.enabled) {
         return;
      }

      boolean bmActive = this.plugin.events() != null && this.plugin.events().hasGlobalSession();
      if (!bmActive) {
         return;
      }

      long nowMs = System.currentTimeMillis();
      long nowSec = nowMs / 1000L;
      double hearts = this.damageAmount / 2.0;
      String heartsStr = (hearts == (long)hearts) ? String.format("%.0f", hearts) : String.format("%.1f", hearts);

      for (Player p : Bukkit.getOnlinePlayers()) {
         if (this.isExempt(p)) {
            continue;
         }

         UUID id = p.getUniqueId();
         boolean isGliding = p.isGliding() || (p.isFlying() && p.getGameMode() == GameMode.SURVIVAL);
         boolean isHigh = p.getLocation().getY() >= this.minAltitude;

         if (isGliding && isHigh) {
            this.glidingStartMs.putIfAbsent(id, nowMs);
            long start = this.glidingStartMs.get(id);

            if (nowMs - start >= (long)this.flightTimeSeconds * 1000L) {
               long lastStrike = this.lastStrikeSec.getOrDefault(id, 0L);
               long lastBc = this.lastBroadcastSec.getOrDefault(id, 0L);

               if (nowSec - lastStrike >= (long)this.strikeIntervalSeconds) {
                  this.lastStrikeSec.put(id, nowSec);
                  this.lastDamageMs.put(id, nowMs);

                  Location loc = p.getLocation();
                  World w = p.getWorld();
                  w.strikeLightningEffect(loc);
                  p.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 1.2F);
                  p.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.9F, 1.4F);
                  w.spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(0, 1, 0), 25, 0.6, 0.6, 0.6, 0.1);

                  if (this.actionbarMessage != null && !this.actionbarMessage.isEmpty()) {
                     p.sendActionBar(Util.mini(this.actionbarMessage.replace("{hearts}", heartsStr)));
                  }

                  if (this.disruptFlight) {
                     p.setGliding(false);
                  }

                  p.damage(this.damageAmount);
               }

               if (nowSec - lastBc >= (long)this.broadcastIntervalSeconds) {
                  this.lastBroadcastSec.put(id, nowSec);
                  if (this.broadcastMessage != null && !this.broadcastMessage.isEmpty()) {
                     Bukkit.broadcast(Util.mini(this.broadcastMessage.replace("{player}", p.getName()).replace("{hearts}", heartsStr)));
                  }
               }
            }
         } else {
            this.glidingStartMs.remove(id);
            this.lastStrikeSec.remove(id);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onDeath(PlayerDeathEvent e) {
      if (!this.enabled) {
         return;
      }
      Player p = e.getPlayer();
      Long t = this.lastDamageMs.get(p.getUniqueId());
      if (t != null && (System.currentTimeMillis() - t) <= 2500L) {
         if (this.deathMessage != null && !this.deathMessage.isEmpty()) {
            e.deathMessage(Util.mini(this.deathMessage.replace("{player}", p.getName())));
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      UUID id = e.getPlayer().getUniqueId();
      this.glidingStartMs.remove(id);
      this.lastStrikeSec.remove(id);
      this.lastBroadcastSec.remove(id);
      this.lastDamageMs.remove(id);
   }
}
