package dev.bloodmoonplus.survivor;

import dev.bloodmoonplus.BloodMoonPlus;
import dev.bloodmoonplus.EventManager;
import dev.bloodmoonplus.Util;
import dev.bloodmoonplus.config.WorldConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SurvivorManager {
   private final BloodMoonPlus plugin;

   public SurvivorManager(BloodMoonPlus plugin) {
      this.plugin = plugin;
   }

   public List<Player> processRewards(EventManager.Session s, boolean natural) {
      WorldConfig wc = this.plugin.worldConfigs().get(s.world.getName());
      WorldConfig.SurvivorPolicy policy = wc.survivor();
      if (!policy.enabled()) {
         return List.of();
      } else if (!natural) {
         return List.of();
      } else {
         long startMs = s.startMs;
         long endMs = s.endMs;
         UUID sessionUuid = UUID.randomUUID();
         List<Player> eligible = new ArrayList<>();

         for (UUID id : s.participants) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) {
               if (!p.getWorld().equals(s.world)) {
                  if (policy.disqualifyOnWorldLeave()) {
                  }
               } else if (!policy.disqualifyOnDeath() || !s.died.contains(id)) {
                  long participation = (endMs - startMs) / 1000L;
                  if (participation >= (long)policy.minParticipationSeconds()) {
                     eligible.add(p);
                  }
               }
            } else if (policy.requireOnlineAtEnd()) {
            }
         }

         if (eligible.isEmpty()) {
            if (this.plugin.messages().announce("survivors", false)) {
               this.plugin.messages().all("event.survivors-none", "world", s.world.getName());
            }

            return List.of();
         } else {
            if (this.plugin.messages().announce("survivors", false)) {
               this.plugin.messages().world(s.world, "event.survivors-header", "world", s.world.getName());
            }

            RewardContext base = new RewardContext(
               s.world,
               sessionUuid.toString(),
               startMs,
               endMs,
               (long)s.participants.size(),
               (long)s.died.size(),
               (long)eligible.size(),
               "",
               "NONE",
               null,
               null,
               s.world.getName(),
               0.0,
               0.0,
               0.0,
               0.0,
               0.0
            );

            for (Player p : eligible) {
               for (String msg : policy.messages()) {
                  String resolved = base.resolve(msg, p);
                  p.sendMessage(Util.mini(resolved));
               }
            }

            for (String raw : policy.commands()) {
               RewardContext.ParsedCommand pc = RewardContext.parse(raw);
               if (pc != null) {
                  switch (pc.audience()) {
                     case CONSOLE_ONCE:
                     case CONSOLE:
                        String resolved = base.resolve(pc.command(), null);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolved);
                        break;
                     case CONSOLE_PER_PLAYER:
                        for (Player p : eligible) {
                           String resolvedx = base.resolve(pc.command(), p);
                           Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolvedx);
                        }
                        break;
                     case PLAYER_EXEC:
                        for (Player p : eligible) {
                           String resolvedx = base.resolve(pc.command(), p);
                           p.performCommand(resolvedx);
                        }
                  }
               }
            }

            return eligible;
         }
      }
   }
}
