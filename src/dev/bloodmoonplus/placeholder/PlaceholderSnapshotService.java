package dev.bloodmoonplus.placeholder;

import dev.bloodmoonplus.BloodMoonPlus;
import dev.bloodmoonplus.EventManager;
import dev.bloodmoonplus.stats.StatisticsStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlaceholderSnapshotService {
   private final BloodMoonPlus plugin;
   private final StatisticsStore stats;
   private final AtomicReference<HistoricalStatistics> history;
   private final Map<String, SessionPlaceholderState> sessionCache = new ConcurrentHashMap<>();
   private final Map<UUID, PlayerPlaceholderState> playerCache = new ConcurrentHashMap<>();
   private final AtomicReference<BossPlaceholderSnapshot> boss = new AtomicReference<>(BossPlaceholderSnapshot.empty());

   public PlaceholderSnapshotService(BloodMoonPlus plugin, StatisticsStore stats) {
      this.plugin = plugin;
      this.stats = stats;
      this.history = new AtomicReference<>(stats.get());
   }

   public void publish() {
      this.history.set(this.stats.get());
      EventManager.Session global = this.plugin.events().globalSession();
      if (global != null) {
         this.sessionCache
            .put(
               global.world.getName(),
               new SessionPlaceholderState(
                  UUID.randomUUID(),
                  global.world.getName(),
                  Math.max(0L, (global.endMs - System.currentTimeMillis()) / 1000L),
                  (long)global.kills.size(),
                  (long)global.died.size(),
                  (long)global.participants.size(),
                  this.countSurvivors(global)
               )
            );
      } else {
         this.sessionCache.clear();
      }

      for (Player p : Bukkit.getOnlinePlayers()) {
         PlayerPlaceholderState snap = PlayerPlaceholderState.empty(p);
         if (global != null && global.participants.contains(p.getUniqueId())) {
            snap = new PlayerPlaceholderState(
               p.getUniqueId(),
               p.getName(),
               true,
               0L,
               0L,
               !global.died.contains(p.getUniqueId()),
               global.died.contains(p.getUniqueId()) ? "placeholder.disqualified" : "placeholder.eligible"
            );
         }

         this.playerCache.put(p.getUniqueId(), snap);
      }

      BossPlaceholderSnapshot bosses = this.plugin.bosses().aliveSnapshot();
      if (bosses != null) {
         this.boss.set(bosses);
      }
   }

   private long countSurvivors(EventManager.Session s) {
      return (long)(s.participants.size() - s.died.size());
   }

   public HistoricalStatistics history() {
      return this.history.get();
   }

   public SessionPlaceholderState sessionFor(String world) {
      return this.sessionCache.get(world);
   }

   public PlayerPlaceholderState playerFor(UUID id) {
      return this.playerCache.get(id);
   }

   public BossPlaceholderSnapshot boss() {
      return this.boss.get();
   }
}
