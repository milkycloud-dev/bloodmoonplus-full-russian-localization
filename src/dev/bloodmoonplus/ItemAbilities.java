package dev.bloodmoonplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public final class ItemAbilities implements Listener {
   private final BloodMoonPlus plugin;
   private boolean processing;
   private final Set<UUID> bleeding = new HashSet<>();
   private static final Map<String, List<String[]>> PARSE_CACHE = new ConcurrentHashMap<>();

   ItemAbilities(BloodMoonPlus plugin) {
      this.plugin = plugin;
   }

   private static List<String[]> read(ItemStack it, NamespacedKey key) {
      if (it != null && it.hasItemMeta()) {
         String raw = (String)it.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
         if (raw != null && !raw.isEmpty()) {
            String ck = key.getKey() + "|" + raw;
            List<String[]> cached = PARSE_CACHE.get(ck);
            if (cached != null) {
               return cached;
            } else {
               List<String[]> out = new ArrayList<>();

               for (String part : raw.split(";")) {
                  if (!part.isBlank()) {
                     String[] a = part.trim().split(":");
                     a[0] = a[0].toUpperCase(Locale.ROOT);
                     out.add(a);
                  }
               }

               List<String[]> immutable = List.copyOf(out);
               if (PARSE_CACHE.size() > 512) {
                  PARSE_CACHE.clear();
               }

               PARSE_CACHE.put(ck, immutable);
               return immutable;
            }
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static double d(String[] a, int i, double def) {
      if (i >= a.length) {
         return def;
      } else {
         try {
            return Double.parseDouble(a[i].trim());
         } catch (NumberFormatException var5) {
            return def;
         }
      }
   }

   private static Player playerSource(Entity damager) {
      if (damager instanceof Player) {
         return (Player)damager;
      } else {
         if (damager instanceof Projectile pr) {
            ProjectileSource var3 = pr.getShooter();
            if (var3 instanceof Player) {
               return (Player)var3;
            }
         }

         return null;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onDamage(EntityDamageByEntityEvent e) {
      if (!this.processing) {
         Player attacker = playerSource(e.getDamager());
         if (attacker != null && e.getEntity() instanceof LivingEntity victim) {
            this.offense(attacker, victim, e);
         }

         if (e.getEntity() instanceof Player defender) {
            this.defense(defender, e);
         }
      }
   }

   private void offense(Player attacker, LivingEntity victim, EntityDamageByEntityEvent e) {
      List<String[]> abs = read(attacker.getInventory().getItemInMainHand(), Keys.ITEM_ABILITIES);
      if (!abs.isEmpty()) {
         ThreadLocalRandom rnd = ThreadLocalRandom.current();
         double dmg = e.getDamage();
         boolean crit = false;

         for (String[] a : abs) {
            String var11 = a[0];
            switch (var11) {
               case "DAMAGE_BONUS":
                  dmg *= 1.0 + d(a, 1, 0.0);
                  break;
               case "BOSS_SLAYER":
                  if (EventManager.isTagged(victim)) {
                     dmg *= 1.0 + d(a, 1, 0.0);
                  }
                  break;
               case "EXECUTE":
                  if (victim.getHealth() / Util.maxHealth(victim) <= d(a, 1, 0.3)) {
                     dmg *= 1.0 + d(a, 2, 0.5);
                  }
                  break;
               case "CRIT":
                  if (rnd.nextDouble() < d(a, 1, 0.15)) {
                     dmg *= d(a, 2, 2.0);
                     crit = true;
                  }
            }
         }

         e.setDamage(dmg);
         if (crit) {
            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.5, 0.4, 0.25);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 0.9F);
         }

         for (String[] a : abs) {
            String var37 = a[0];
            switch (var37) {
               case "LIFESTEAL":
                  double heal = e.getFinalDamage() * d(a, 1, 0.0);
                  attacker.setHealth(Math.min(Util.maxHealth(attacker), attacker.getHealth() + heal));
                  break;
               case "LIGHTNING":
                  if (rnd.nextDouble() < d(a, 1, 0.1)) {
                     victim.getWorld().strikeLightningEffect(victim.getLocation());
                     this.processing = true;

                     try {
                        victim.damage(d(a, 2, 4.0), attacker);
                     } finally {
                        this.processing = false;
                     }
                  }
                  break;
               case "CHAIN_LIGHTNING":
                  if (!(rnd.nextDouble() < d(a, 1, 0.1))) {
                     break;
                  }

                  int chains = (int)d(a, 2, 2.0);
                  double chainDmg = d(a, 3, 4.0);
                  double decay = d(a, 4, 0.5);
                  LivingEntity current = victim;
                  Set<LivingEntity> hit = new HashSet<>();
                  hit.add(victim);

                  for (int i = 0; i < chains; i++) {
                     LivingEntity next = null;
                     double bestDist = Double.MAX_VALUE;

                     for (LivingEntity nearby : current.getLocation().getNearbyLivingEntities(8.0)) {
                        if (!hit.contains(nearby) && !nearby.equals(attacker)) {
                           double dist = current.getLocation().distanceSquared(nearby.getLocation());
                           if (dist < bestDist) {
                              bestDist = dist;
                              next = nearby;
                           }
                        }
                     }

                     if (next == null) {
                        break;
                     }

                     next.getWorld().strikeLightningEffect(next.getLocation());
                     this.processing = true;

                     try {
                        next.damage(chainDmg, attacker);
                     } finally {
                        this.processing = false;
                     }

                     hit.add(next);
                     current = next;
                     chainDmg *= 1.0 - decay;
                  }
                  break;
               case "POISON":
                  if (rnd.nextDouble() < d(a, 1, 0.2)) {
                     PotionEffectType t = Reg.effect("poison");
                     if (t != null) {
                        victim.addPotionEffect(new PotionEffect(t, (int)(d(a, 2, 4.0) * 20.0), (int)d(a, 3, 1.0)));
                     }
                  }
                  break;
               case "BLEED":
                  if (rnd.nextDouble() < d(a, 1, 0.25)) {
                     PersistentDataContainer pdc = victim.getPersistentDataContainer();
                     int ticks = (int)(d(a, 2, 5.0) * 20.0);
                     pdc.set(Keys.BLEED, PersistentDataType.INTEGER, ticks);
                     pdc.set(Keys.BLEED_DMG, PersistentDataType.DOUBLE, d(a, 3, 1.0));
                     this.bleeding.add(victim.getUniqueId());
                     victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.5, 0.3, 0.05);
                  }
                  break;
               case "IGNITE":
                  if (rnd.nextDouble() < d(a, 1, 0.2)) {
                     victim.setFireTicks((int)(d(a, 2, 4.0) * 20.0));
                  }
                  break;
               case "WITHER":
                  this.applyEffect(victim, "wither", a, rnd.nextDouble());
                  break;
               case "SLOW":
                  this.applyEffect(victim, "slowness", a, rnd.nextDouble());
            }
         }
      }
   }

   private void applyEffect(LivingEntity victim, String effect, String[] a, double roll) {
      if (!(roll >= d(a, 1, 0.2))) {
         PotionEffectType t = Reg.effect(effect);
         if (t != null) {
            int ticks = (int)(d(a, 2, 4.0) * 20.0);
            int amp = (int)d(a, 3, 1.0);
            victim.addPotionEffect(new PotionEffect(t, ticks, amp));
         }
      }
   }

   private void defense(Player defender, EntityDamageByEntityEvent e) {
      double reduction = 0.0;
      double dodge = 0.0;
      double reflect = 0.0;
      List<String[]> thorns = new ArrayList<>();
      List<String[]> thunder = new ArrayList<>();
      List<String[]> blast = new ArrayList<>();
      List<String[]> levitate = new ArrayList<>();

      for (ItemStack piece : defender.getInventory().getArmorContents()) {
         for (String[] a : read(piece, Keys.ITEM_ABILITIES)) {
            String var19 = a[0];
            switch (var19) {
               case "DAMAGE_REDUCTION":
                  reduction += d(a, 1, 0.0);
                  break;
               case "DODGE":
                  dodge += d(a, 1, 0.0);
                  break;
               case "REFLECT":
                  reflect += d(a, 1, 0.0);
                  break;
               case "THORNS":
                  thorns.add(a);
                  break;
               case "THUNDER_GUARD":
                  thunder.add(a);
                  break;
               case "BLAST_WAVE":
                  blast.add(a);
                  break;
               case "LEVITATE":
                  levitate.add(a);
            }
         }
      }

      ThreadLocalRandom rnd = ThreadLocalRandom.current();
      if (dodge > 0.0 && rnd.nextDouble() < Math.min(0.6, dodge)) {
         e.setCancelled(true);
         defender.getWorld().spawnParticle(Particle.CLOUD, defender.getLocation().add(0.0, 1.0, 0.0), 15, 0.3, 0.5, 0.3, 0.02);
         defender.playSound(defender.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0F, 1.6F);
      } else {
         if (reduction > 0.0) {
            e.setDamage(e.getDamage() * (1.0 - Math.min(0.8, reduction)));
         }

         if (reflect > 0.0 && e.getDamager() instanceof LivingEntity refAttacker && rnd.nextDouble() < Math.min(0.5, reflect)) {
            double ret = e.getFinalDamage() * d(new String[]{"REFLECT", String.valueOf(reflect)}, 1, 0.3);
            refAttacker.damage(ret, defender);
            defender.getWorld().spawnParticle(Particle.ENCHANT, defender.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.5, 0.3, 0.05);
         }

         if (!thorns.isEmpty() && e.getDamager() instanceof LivingEntity attacker) {
            for (String[] a : thorns) {
               if (rnd.nextDouble() < d(a, 1, 0.2)) {
                  attacker.damage(d(a, 2, 3.0), defender);
               }
            }
         }

         if (!thunder.isEmpty()) {
            for (String[] ax : thunder) {
               if (!(rnd.nextDouble() >= d(ax, 1, 0.2))) {
                  boolean self = ax.length > 3 && "self".equalsIgnoreCase(ax[3].trim());
                  LivingEntity strike = (LivingEntity)(self ? defender : sourceOf(e.getDamager()));
                  if (strike != null && !strike.isDead()) {
                     strike.getWorld().strikeLightningEffect(strike.getLocation());
                     if (self) {
                        strike.damage(d(ax, 2, 6.0));
                     } else {
                        strike.damage(d(ax, 2, 6.0), defender);
                     }
                  }
               }
            }
         }

         if (!blast.isEmpty()) {
            for (String[] axx : blast) {
               if (!(rnd.nextDouble() >= d(axx, 1, 0.2))) {
                  LivingEntity src = sourceOf(e.getDamager());
                  if (src != null && !src.equals(defender) && !src.isDead()) {
                     Vector dir = src.getLocation().toVector().subtract(defender.getLocation().toVector()).setY(0);
                     if (dir.lengthSquared() < 0.01) {
                        dir = new Vector(rnd.nextDouble() - 0.5, 0.0, rnd.nextDouble() - 0.5);
                     }

                     src.setVelocity(dir.normalize().multiply(d(axx, 3, 1.3)).setY(0.7));
                     src.damage(d(axx, 2, 5.0), defender);
                     src.getWorld().spawnParticle(Particle.EXPLOSION, src.getLocation().add(0.0, 1.0, 0.0), 3, 0.3, 0.3, 0.3, 0.1);
                     src.getWorld().playSound(src.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7F, 1.5F);
                  }
               }
            }
         }

         if (!levitate.isEmpty()) {
            for (String[] axxx : levitate) {
               if (!(rnd.nextDouble() >= d(axxx, 1, 0.15))) {
                  LivingEntity src = sourceOf(e.getDamager());
                  if (src != null && !src.equals(defender) && !src.isDead()) {
                     PotionEffectType t = Reg.effect("levitation");
                     if (t != null) {
                        src.addPotionEffect(new PotionEffect(t, (int)(d(axxx, 2, 3.0) * 20.0), (int)d(axxx, 3, 0.0)));
                        src.getWorld().spawnParticle(Particle.END_ROD, src.getLocation().add(0.0, 1.2, 0.0), 12, 0.3, 0.4, 0.3, 0.02);
                     }
                  }
               }
            }
         }
      }
   }

   private static LivingEntity sourceOf(Entity damager) {
      if (damager instanceof LivingEntity) {
         return (LivingEntity)damager;
      } else {
         if (damager instanceof Projectile pr) {
            ProjectileSource var3 = pr.getShooter();
            if (var3 instanceof LivingEntity) {
               return (LivingEntity)var3;
            }
         }

         return null;
      }
   }

   void tickPassives() {
      if (!this.bleeding.isEmpty()) {
         Iterator<UUID> it = this.bleeding.iterator();

         while (it.hasNext()) {
            if (Bukkit.getEntity(it.next()) instanceof LivingEntity le && !le.isDead() && le.isValid()) {
               PersistentDataContainer pdc = le.getPersistentDataContainer();
               Integer bleedTicks = (Integer)pdc.get(Keys.BLEED, PersistentDataType.INTEGER);
               if (bleedTicks != null && bleedTicks > 0) {
                  Double configured = (Double)pdc.get(Keys.BLEED_DMG, PersistentDataType.DOUBLE);
                  double perHit = configured != null ? configured : 1.0;
                  int left = bleedTicks - 40;
                  this.processing = true;

                  try {
                     le.damage(perHit);
                  } finally {
                     this.processing = false;
                  }

                  if (left <= 0) {
                     pdc.remove(Keys.BLEED);
                     it.remove();
                  } else {
                     pdc.set(Keys.BLEED, PersistentDataType.INTEGER, left);
                  }

                  le.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, le.getLocation().add(0.0, 1.0, 0.0), 2, 0.2, 0.3, 0.2, 0.01);
               } else {
                  it.remove();
               }
               continue;
            }

            it.remove();
         }
      }

      for (Player p : Bukkit.getOnlinePlayers()) {
         this.applyPassives(p, p.getInventory().getItemInMainHand());
         this.applyPassives(p, p.getInventory().getItemInOffHand());

         for (ItemStack piece : p.getInventory().getArmorContents()) {
            this.applyPassives(p, piece);
         }

         this.applyHealAura(p);
      }
   }

   private void applyHealAura(Player p) {
      int radius = 0;
      double healPerSec = 0.0;

      for (ItemStack piece : p.getInventory().getArmorContents()) {
         for (String[] a : read(piece, Keys.ITEM_PASSIVES)) {
            if (a[0].equalsIgnoreCase("HEAL_AURA")) {
               radius = Math.max(radius, (int)d(a, 1, 5.0));
               healPerSec = Math.max(healPerSec, d(a, 2, 1.0));
            }
         }
      }

      if (radius != 0 && !(healPerSec <= 0.0)) {
         for (Player nearby : p.getWorld().getPlayers()) {
            if (!nearby.equals(p) && !(nearby.getLocation().distanceSquared(p.getLocation()) > (double)(radius * radius))) {
               double newHp = Math.min(Util.maxHealth(nearby), nearby.getHealth() + healPerSec);
               nearby.setHealth(newHp);
               nearby.getWorld().spawnParticle(Particle.HEART, nearby.getLocation().add(0.0, 1.5, 0.0), 2, 0.3, 0.3, 0.3, 0.01);
            }
         }
      }
   }

   private void applyPassives(Player p, ItemStack it) {
      if (it != null && it.hasItemMeta()) {
         for (String[] a : read(it, Keys.ITEM_PASSIVES)) {
            PotionEffectType t = Reg.effect(a[0]);
            if (t != null) {
               int amp = (int)d(a, 1, 0.0);
               int dur = 140;
               p.addPotionEffect(new PotionEffect(t, dur, amp, true, false, true));
            }
         }
      }
   }

   void cleanStalePassives(Player p) {
      if (p != null && p.isOnline()) {
         Map<PotionEffectType, Integer> stillNeeded = new HashMap<>();

         for (ItemStack it : p.getInventory().getArmorContents()) {
            if (it != null && it.hasItemMeta()) {
               for (String[] a : read(it, Keys.ITEM_PASSIVES)) {
                  PotionEffectType t = Reg.effect(a[0]);
                  if (t != null) {
                     stillNeeded.merge(t, 1, Integer::sum);
                  }
               }
            }
         }

         for (ItemStack itx : List.of(p.getInventory().getItemInMainHand(), p.getInventory().getItemInOffHand())) {
            if (itx != null && itx.hasItemMeta()) {
               for (String[] ax : read(itx, Keys.ITEM_PASSIVES)) {
                  PotionEffectType t = Reg.effect(ax[0]);
                  if (t != null) {
                     stillNeeded.merge(t, 1, Integer::sum);
                  }
               }
            }
         }

         for (PotionEffect pe : p.getActivePotionEffects()) {
            if (pe.isAmbient()) {
               int still = stillNeeded.getOrDefault(pe.getType(), 0);
               if (still <= 0) {
                  p.removePotionEffect(pe.getType());
               }
            }
         }
      }
   }
}
