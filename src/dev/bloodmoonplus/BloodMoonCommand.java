package dev.bloodmoonplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;

public final class BloodMoonCommand implements TabExecutor {
   private static final Map<String, String> ALIASES = new LinkedHashMap<>();
   private static final List<String> USE_SUBS = List.of("next");
   private static final List<String> ADMIN_SUBS = List.of("start", "stop", "difficulty", "boss", "horde", "killmobs", "give", "items", "chances", "reload", "warn", "evolution", "countdown", "cancel", "lightning", "time", "duration", "dynamic", "black", "purple", "blood");
   private final BloodMoonPlus plugin;

   BloodMoonCommand(BloodMoonPlus plugin) {
      this.plugin = plugin;
   }

   private Messages m() {
      return this.plugin.messages();
   }

   public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
      if (args.length == 0) {
         this.status(s);
         return true;
      } else {
         String sub = ALIASES.getOrDefault(args[0].toLowerCase(Locale.ROOT), "");
         if (sub.isEmpty()) {
            this.m().send(s, "cmd.usage");
            return true;
         } else if (!USE_SUBS.contains(sub) && !s.hasPermission("bloodmoon.admin")) {
            this.m().send(s, "no-permission");
            return true;
         } else {
            String[] a = args.length <= 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
            switch (sub) {
               case "next":
                  this.next(s);
                  break;
               case "start":
                  this.start(s, a);
                  break;
               case "stop":
                  this.stop(s, a);
                  break;
               case "difficulty":
                  this.difficulty(s, a);
                  break;
               case "boss":
                  this.boss(s, a);
                  break;
               case "horde":
                  this.horde(s, a);
                  break;
               case "killmobs":
                  this.killmobs(s, a);
                  break;
               case "give":
                  this.give(s, a);
                  break;
               case "items":
                  this.m().send(s, "cmd.items-header");

                  for (String id : this.plugin.items().ids()) {
                     ItemFactory.ItemDef d = this.plugin.items().get(id);
                     s.sendMessage(
                        Util.mini(
                           "<dark_gray>- <white>"
                              + id
                              + " <gray>"
                              + this.plugin.loot().rarityDisplay(d.rarity())
                              + (d.limit() > 0 ? " <red>(" + this.plugin.data().created(id) + "/" + d.limit() + ")" : "")
                        )
                     );
                  }
                  break;
               case "chances":
                  if (a.length < 1) {
                     this.m().send(s, "cmd.usage-chances");
                     return true;
                  }

                  List<String> lines = this.plugin.loot().describe(a[0]);
                  if (lines == null) {
                     this.m().send(s, "cmd.table-unknown", "table", a[0]);
                     return true;
                  }

                  for (String l : lines) {
                     s.sendMessage(Util.mini(l));
                  }
                  break;
               case "reload":
                  this.plugin.reloadAll();
                  this.m().send(s, "cmd.reloaded");
                  break;
               case "warn":
                  this.warn(s, a);
                  break;
               case "evolution":
                  if (this.plugin.quitPenalty() != null) {
                     this.plugin.quitPenalty().broadcastEvolutionAlert();
                     s.sendMessage(Util.mini("<green>✔ Оповещение об эволюции Кровавой Луны отправлено всем игрокам!</green>"));
                  }
                  break;
               case "black":
                  this.startSpecial(s, a, MoonType.BLACK);
                  break;
               case "purple":
                  this.startSpecial(s, a, MoonType.PURPLE);
                  break;
               case "blood":
                  this.startSpecial(s, a, MoonType.BLOOD);
                  break;
               case "countdown":
                  this.countdown(s, a);
                  break;
               case "cancel":
                  this.plugin.countdown().cancel(true);
                  s.sendMessage(Util.mini("<yellow>⚡ Обратный отсчет отменен.</yellow>"));
                  break;
               case "lightning":
                  this.lightning(s, a);
                  break;
               case "time":
               case "duration":
                  this.time(s, a);
                  break;
               case "dynamic":
                  this.dynamic(s, a);
                  break;
               default:
                  this.m().send(s, "cmd.usage");
            }

            return true;
         }
      }
   }

   public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
      boolean admin = s.hasPermission("bloodmoon.admin");
      if (args.length == 1) {
         List<String> subs = new ArrayList<>(USE_SUBS);
         if (admin) {
            subs.addAll(ADMIN_SUBS);
            subs.addAll(List.of("отсчет", "время", "динамика", "молнии", "черная", "фиолетовая", "кровавая", "предупредить", "старт", "стоп"));
         }

         return filter(subs, args[0]);
      } else if (!admin) {
         return List.of();
      } else {
         String sub = ALIASES.getOrDefault(args[0].toLowerCase(Locale.ROOT), "");
         List<String> worlds = Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).toList();
         String cur = args[args.length - 1];
         switch (sub) {
            case "start": {
               List<String> o = new ArrayList<>(worlds);
               o.addAll(List.of("x1", "x2", "x3", "x5", "x10", "15m", "20m", "25m", "30m", "10s", "30s", "60s", "in", "black", "purple", "blood"));
               return filter(o, cur);
            }
            case "countdown": {
               if (args.length == 2) {
                  return filter(List.of("1m", "2m", "3m", "5m", "10m", "15m", "30s", "60s", "120s", "cancel"), cur);
               } else if (args.length == 3) {
                  return filter(List.of("normal", "dangerous", "lethal", "black", "purple", "blood", "x1", "x2", "x3", "x5"), cur);
               } else if (args.length == 4) {
                  List<String> o = new ArrayList<>(List.of("black", "purple", "blood", "normal", "dangerous", "lethal"));
                  o.addAll(worlds);
                  return filter(o, cur);
               } else {
                  return filter(worlds, cur);
               }
            }
            case "warn": {
               if (args.length == 2) {
                  return filter(List.of("10m", "5m", "3m", "2m", "1m", "30s"), cur);
               } else if (args.length == 3) {
                  return filter(List.of("normal", "dangerous", "lethal", "black", "purple", "blood", "start"), cur);
               } else if (args.length == 4) {
                  return filter(List.of("black", "purple", "blood", "start"), cur);
               } else {
                  return filter(List.of("start"), cur);
               }
            }
            case "black":
            case "purple":
            case "blood": {
               List<String> o = new ArrayList<>(worlds);
               o.addAll(List.of("15m", "20m", "25m", "30m", "x1", "x2", "x3", "x5"));
               return filter(o, cur);
            }
            case "stop": {
               List<String> o = new ArrayList<>(worlds);
               o.add("all");
               return filter(o, cur);
            }
            case "difficulty":
               return args.length == 2 ? filter(List.of("x1", "x2", "x3", "x5", "x10", "x50", "x100", "x500", "x1000"), cur) : filter(worlds, cur);
            case "boss":
               if (args.length == 2) {
                  return filter(new ArrayList<>(this.plugin.bosses().ids()), cur);
               } else if (args.length == 3) {
                  List<String> opts = new ArrayList<>(worlds);
                  opts.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                  return filter(opts, cur);
               }
               return List.of();
            case "horde":
               return filter(worlds, cur);
            case "killmobs":
               return filter(worlds, cur);
            case "chances":
               return args.length == 2 ? filter(new ArrayList<>(this.plugin.loot().tableIds()), cur) : List.of();
            case "give":
               if (args.length == 2) {
                  return filter(Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList(), cur);
               } else {
                  if (args.length == 3) {
                     return filter(new ArrayList<>(this.plugin.items().ids()), cur);
                  }

                  return List.of();
               }
            case "lightning": {
               List<String> players = new ArrayList<>(Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList());
               if (args.length == 2) {
                  return filter(List.of("storm", "smite", "strike", "stop", "status", "help"), cur);
               }
               String action = args[1].toLowerCase(Locale.ROOT);
               if (action.equals("storm") || action.equals("шторм")) {
                  if (args.length == 3) {
                     List<String> opts = new ArrayList<>(players);
                     opts.add("all");
                     opts.add("stop");
                     return filter(opts, cur);
                  } else if (args.length == 4) {
                     return filter(List.of("1", "3", "5", "10", "20"), cur);
                  } else if (args.length == 5) {
                     return filter(List.of("10", "15", "25", "50"), cur);
                  } else if (args.length == 6) {
                     return filter(List.of("30", "60", "120", "300"), cur);
                  }
               } else if (action.equals("smite") || action.equals("кара")) {
                  if (args.length == 3) {
                     List<String> opts = new ArrayList<>(players);
                     opts.add("stop");
                     return filter(opts, cur);
                  } else if (args.length == 4) {
                     return filter(List.of("1", "2", "3", "5", "10"), cur);
                  } else if (args.length == 5) {
                     return filter(List.of("1.0", "2.0", "4.0", "10.0"), cur);
                  } else if (args.length == 6) {
                     return filter(List.of("15", "30", "60", "120"), cur);
                  }
               } else if (action.equals("strike") || action.equals("молния")) {
                  if (args.length == 3) {
                     return filter(players, cur);
                  } else if (args.length == 4) {
                     return filter(List.of("0.0", "1.0", "2.0", "4.0"), cur);
                  }
               } else if (action.equals("stop") || action.equals("стоп")) {
                  if (args.length == 3) {
                     List<String> opts = new ArrayList<>(players);
                     opts.add("all");
                     return filter(opts, cur);
                  }
               }
               return List.of();
            }
            case "time":
            case "duration": {
               if (args.length == 2) {
                  return filter(List.of("status", "add", "set", "take", "help"), cur);
               } else if (args.length == 3) {
                  String act = args[1].toLowerCase(Locale.ROOT);
                  if (act.equals("add") || act.equals("set") || act.equals("take") || act.equals("sub")
                      || act.equals("добавить") || act.equals("установить") || act.equals("убавить")) {
                     return filter(List.of("30s", "1m", "2m", "5m", "10m", "15m", "20m", "30m"), cur);
                  }
               } else if (args.length == 4) {
                  List<String> opts = new ArrayList<>(worlds);
                  opts.add("all");
                  return filter(opts, cur);
               }
               return List.of();
            }
            case "dynamic": {
               if (args.length == 2) {
                  return filter(List.of("status", "on", "off", "reset", "help"), cur);
               } else if (args.length == 3 && (args[1].equalsIgnoreCase("reset") || args[1].equalsIgnoreCase("сброс"))) {
                  List<String> opts = new ArrayList<>(worlds);
                  opts.add("all");
                  return filter(opts, cur);
               }
               return List.of();
            }
            default:
               return List.of();
         }
      }
   }

   private static List<String> filter(List<String> opts, String cur) {
      String c = cur.toLowerCase(Locale.ROOT);
      List<String> out = new ArrayList<>();

      for (String o : opts) {
         if (o.toLowerCase(Locale.ROOT).startsWith(c)) {
            out.add(o);
         }
      }

      return out;
   }

   static Double parseMult(String t) {
      String x = t.toLowerCase(Locale.ROOT);
      if (x.equals("triple")) {
         return 3.0;
      } else {
         if (x.length() > 1 && x.charAt(0) == 'x') {
            try {
               return Double.parseDouble(x.substring(1));
            } catch (NumberFormatException ignored) {
            }
         }
         try {
            return Double.parseDouble(x);
         } catch (NumberFormatException ignored) {
         }
         return null;
      }
   }

   static Integer parseMinutes(String t) {
      String x = t.toLowerCase(Locale.ROOT);
      if (x.length() > 1 && x.endsWith("m")) {
         try {
            return Integer.parseInt(x.substring(0, x.length() - 1));
         } catch (NumberFormatException var3) {
         }
      }

      return null;
   }

   static Integer parseSeconds(String t) {
      String x = t.toLowerCase(Locale.ROOT);
      if (x.endsWith("s") || x.endsWith("sec")) {
         String num = x.replaceAll("[^0-9]", "");
         if (!num.isEmpty()) {
            try {
               return Integer.parseInt(num);
            } catch (NumberFormatException ignored) {
            }
         }
      }
      return null;
   }

   private void startSpecial(CommandSender s, String[] a, MoonType type) {
      List<String> list = new ArrayList<>(Arrays.asList(a));
      list.add(type.getId());
      this.start(s, list.toArray(new String[0]));
   }

   private void start(CommandSender s, String[] a) {
      Integer countdownSec = null;
      Double diff = null;
      Integer minutes = null;
      String worldName = null;
      MoonType moonType = MoonType.BLOOD;
      List<String> knownWorlds = Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).toList();

      for (int i = 0; i < a.length; i++) {
         String t = a[i];
         if (t.equalsIgnoreCase("black") || t.equalsIgnoreCase("черная") || t.equalsIgnoreCase("чёрная")) {
            moonType = MoonType.BLACK;
            continue;
         }
         if (t.equalsIgnoreCase("purple") || t.equalsIgnoreCase("фиолетовая") || t.equalsIgnoreCase("void")) {
            moonType = MoonType.PURPLE;
            continue;
         }
         if (t.equalsIgnoreCase("blood") || t.equalsIgnoreCase("кровавая")) {
            moonType = MoonType.BLOOD;
            continue;
         }
         if (t.equalsIgnoreCase("in") && i + 1 < a.length) {
            try {
               countdownSec = Integer.parseInt(a[i + 1].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {
            }
            continue;
         }
         Integer ps = parseSeconds(t);
         if (ps != null) {
            countdownSec = ps;
            continue;
         }
         Double d = parseMult(t);
         Integer mi = parseMinutes(t);
         if (d != null && !t.endsWith("s") && !t.endsWith("m")) {
            diff = d;
         } else if (mi != null) {
            minutes = mi;
         } else if (knownWorlds.contains(t)) {
            worldName = t;
         }
      }

      double dv = diff != null ? diff : this.plugin.getConfig().getDouble("difficulty.default", 1.0);
      int mv = minutes != null ? minutes : this.plugin.getConfig().getInt("event.duration-minutes", 20);

      if (countdownSec != null && countdownSec > 0) {
         List<World> targets = worldName != null
            ? List.of(Bukkit.getWorld(worldName))
            : this.plugin.events().defaultWorlds();
         this.plugin.countdown().start(countdownSec, dv, Math.max(1, mv), targets, s, CountdownManager.WarningType.NORMAL, moonType);
         s.sendMessage(Util.mini("<green>✔ Запущен обратный отсчет до " + moonType.getDisplayName() + " (" + countdownSec + " сек).</green>"));
         return;
      }

      String startedLabel;
      if (worldName != null) {
         World w = Bukkit.getWorld(worldName);
         if (w == null) {
            this.m().send(s, "unknown-world");
            return;
         }

         if (!this.plugin.events().start(w, dv, Math.max(1, mv), moonType)) {
            this.m().send(s, "event.already-active");
            return;
         }

         startedLabel = w.getName();
      } else {
         List<World> targets = this.plugin.events().defaultWorlds();
         if (targets.isEmpty()) {
            this.m().send(s, "unknown-world");
            return;
         }

         if (!this.plugin.events().startIn(targets, dv, Math.max(1, mv), moonType)) {
            this.m().send(s, "event.already-active");
            return;
         }

         startedLabel = targets.size() == 1 ? targets.get(0).getName() : "global";
      }

      s.sendMessage(Util.mini("<green>✔ Взошла " + moonType.getDisplayName() + " (" + startedLabel + ")!</green>"));
   }

   private void countdown(CommandSender s, String[] a) {
      if (a.length > 0 && (a[0].equalsIgnoreCase("cancel") || a[0].equalsIgnoreCase("stop") || a[0].equalsIgnoreCase("abort"))) {
         this.plugin.countdown().cancel(true);
         s.sendMessage(Util.mini("<yellow>⚡ Обратный отсчет отменен.</yellow>"));
         return;
      }
      Integer sec = null;
      Double diff = null;
      Integer minutes = null;
      String worldName = null;
      MoonType moonType = MoonType.BLOOD;
      CountdownManager.WarningType warnType = CountdownManager.WarningType.NORMAL;
      boolean diffExplicit = false;
      List<String> knownWorlds = Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).toList();

      for (int i = 0; i < a.length; i++) {
         String t = a[i];
         if (t.equalsIgnoreCase("black") || t.equalsIgnoreCase("черная") || t.equalsIgnoreCase("чёрная")) {
            moonType = MoonType.BLACK;
            continue;
         }
         if (t.equalsIgnoreCase("purple") || t.equalsIgnoreCase("фиолетовая") || t.equalsIgnoreCase("void")) {
            moonType = MoonType.PURPLE;
            continue;
         }
         if (t.equalsIgnoreCase("blood") || t.equalsIgnoreCase("кровавая")) {
            moonType = MoonType.BLOOD;
            continue;
         }
         if (t.equalsIgnoreCase("lethal") || t.equalsIgnoreCase("смертельная") || t.equalsIgnoreCase("deadly")) {
            warnType = CountdownManager.WarningType.LETHAL;
            continue;
         }
         if (t.equalsIgnoreCase("dangerous") || t.equalsIgnoreCase("опасная") || t.equalsIgnoreCase("danger")) {
            warnType = CountdownManager.WarningType.DANGEROUS;
            continue;
         }
         if (t.equalsIgnoreCase("normal") || t.equalsIgnoreCase("обычная")) {
            warnType = CountdownManager.WarningType.NORMAL;
            continue;
         }
         if (t.equalsIgnoreCase("in") && i + 1 < a.length) {
            Long dIn = parseDurationMs(a[i + 1]);
            if (dIn != null) {
               sec = (int) (dIn / 1000L);
            }
            i++;
            continue;
         }
         if (t.startsWith("x") || t.startsWith("X")) {
            Double d = parseMult(t);
            if (d != null) {
               diff = d;
               diffExplicit = true;
               continue;
            }
         }
         if (t.equalsIgnoreCase("dur") || t.equalsIgnoreCase("duration") || t.equalsIgnoreCase("длительность")) {
            if (i + 1 < a.length) {
               Long dDur = parseDurationMs(a[i + 1]);
               if (dDur != null) {
                  minutes = Math.max(1, (int) (dDur / 60000L));
               }
               i++;
               continue;
            }
         }
         Long durMs = parseDurationMs(t);
         if (durMs != null && durMs > 0 && sec == null) {
            sec = (int) (durMs / 1000L);
            continue;
         }
         Double d = parseMult(t);
         if (d != null && !t.endsWith("m") && !t.endsWith("s")) {
            diff = d;
            diffExplicit = true;
         } else if (knownWorlds.contains(t)) {
            worldName = t;
         }
      }

      int finalSec = sec != null ? sec : 30;
      double dv = diffExplicit && diff != null ? diff : this.plugin.countdown().getDefaultDifficulty(warnType);
      int mv = minutes != null ? minutes : this.plugin.getConfig().getInt("event.duration-minutes", 20);
      List<World> targets = worldName != null ? List.of(Bukkit.getWorld(worldName)) : this.plugin.events().defaultWorlds();

      this.plugin.countdown().start(finalSec, dv, Math.max(1, mv), targets, s, warnType, moonType);
      s.sendMessage(Util.mini("<green>✔ Запущен обратный отсчет до " + moonType.getDisplayName() + " (" + this.plugin.countdown().formatTime(finalSec) + ", сложность: x" + Util.fmt(dv) + ", тип: " + warnType.name() + ").</green>"));
   }

   private void warn(CommandSender s, String[] a) {
      if (a.length == 0) {
         s.sendMessage(Util.mini("<gold>Команда предупреждений о Луне:</gold>"));
         s.sendMessage(Util.mini("<yellow>/luna warn <время> [тип] [вид_луны] [start]</yellow>"));
         s.sendMessage(Util.mini("<gray>Типы предупреждений: <white>normal</white> (обычная), <white>dangerous</white> (опасная), <white>lethal</white> (смертельная)</gray>"));
         s.sendMessage(Util.mini("<gray>Виды луны: <red>blood</red> (кровавая), <dark_gray>black</dark_gray> (чёрная), <light_purple>purple</light_purple> (фиолетовая)</gray>"));
         s.sendMessage(Util.mini("<gray>Примеры:</gray>"));
         s.sendMessage(Util.mini("<gray>- <yellow>/luna warn 10m</yellow> (обычное оповещение за 10 мин)</gray>"));
         s.sendMessage(Util.mini("<gray>- <yellow>/luna warn 10m dangerous</yellow> (оповещение об опасной луне)</gray>"));
         s.sendMessage(Util.mini("<gray>- <yellow>/luna warn 10m lethal black</yellow> (смертельная чёрная луна)</gray>"));
         s.sendMessage(Util.mini("<gray>- <yellow>/luna warn 10m lethal black start</yellow> (оповестить и начать отсчет!)</gray>"));
         return;
      }

      int sec = 600;
      CountdownManager.WarningType warnType = CountdownManager.WarningType.NORMAL;
      MoonType moonType = MoonType.BLOOD;
      boolean shouldStart = false;

      for (String t : a) {
         if (t.equalsIgnoreCase("start") || t.equalsIgnoreCase("старт") || t.equalsIgnoreCase("--start")) {
            shouldStart = true;
            continue;
         }
         if (t.equalsIgnoreCase("black") || t.equalsIgnoreCase("черная") || t.equalsIgnoreCase("чёрная")) {
            moonType = MoonType.BLACK;
            continue;
         }
         if (t.equalsIgnoreCase("purple") || t.equalsIgnoreCase("фиолетовая")) {
            moonType = MoonType.PURPLE;
            continue;
         }
         if (t.equalsIgnoreCase("blood") || t.equalsIgnoreCase("кровавая")) {
            moonType = MoonType.BLOOD;
            continue;
         }
         if (t.equalsIgnoreCase("lethal") || t.equalsIgnoreCase("смертельная") || t.equalsIgnoreCase("deadly")) {
            warnType = CountdownManager.WarningType.LETHAL;
            continue;
         }
         if (t.equalsIgnoreCase("dangerous") || t.equalsIgnoreCase("опасная") || t.equalsIgnoreCase("danger")) {
            warnType = CountdownManager.WarningType.DANGEROUS;
            continue;
         }
         if (t.equalsIgnoreCase("normal") || t.equalsIgnoreCase("обычная")) {
            warnType = CountdownManager.WarningType.NORMAL;
            continue;
         }
         Long dur = parseDurationMs(t);
         if (dur != null) {
            sec = (int)(dur / 1000L);
         }
      }

      if (shouldStart) {
         double dv = this.plugin.countdown().getDefaultDifficulty(warnType);
         int mv = this.plugin.getConfig().getInt("event.duration-minutes", 20);
         this.plugin.countdown().start(sec, dv, mv, this.plugin.events().defaultWorlds(), s, warnType, moonType);
         s.sendMessage(Util.mini("<green>✔ Оповещение отправлено, запущен отсчет до " + moonType.getDisplayName() + " (" + this.plugin.countdown().formatTime(sec) + ")!</green>"));
      } else {
         this.plugin.countdown().broadcastTypeWarning(warnType, sec, moonType);
         s.sendMessage(Util.mini("<green>✔ Оповещение отправлено: <yellow>" + warnType.name() + " " + moonType.getDisplayName() + " (" + this.plugin.countdown().formatTime(sec) + ")</yellow>.</green>"));
      }
   }

   private void stop(CommandSender s, String[] a) {
      if (a.length > 0 && a[0].equalsIgnoreCase("all")) {
         if (!this.plugin.events().stop(null, false)) {
            this.m().send(s, "event.not-active");
         } else {
            this.m().send(s, "cmd.stopped-all");
         }
      } else if (a.length > 0) {
         World w = Bukkit.getWorld(a[0]);
         if (w == null) {
            this.m().send(s, "unknown-world");
         } else if (!this.plugin.events().stop(w, false)) {
            this.m().send(s, "event.not-active");
         } else {
            this.m().send(s, "cmd.stopped", "world", w.getName());
         }
      } else if (!this.plugin.events().stop(null, false)) {
         this.m().send(s, "event.not-active");
      } else {
         this.m().send(s, "cmd.stopped-all");
      }
   }

   private void difficulty(CommandSender s, String[] a) {
      if (a.length < 1) {
         this.m().send(s, "cmd.usage-difficulty");
      } else {
         Double d = parseMult(a[0]);
         if (d == null) {
            this.m().send(s, "cmd.usage-difficulty");
         } else {
            Collection<EventManager.Session> all = this.plugin.events().sessions();
            if (all.isEmpty()) {
               this.m().send(s, "event.not-active");
            } else {
               double v = Math.max(0.1, Math.min(this.plugin.getConfig().getDouble("difficulty.max", 1000.0), d));

               for (EventManager.Session ses : all) {
                  ses.difficulty = v;
               }

               this.m().send(s, "cmd.difficulty-set", "difficulty", Util.fmt(v));
            }
         }
      }
   }

   private void boss(CommandSender s, String[] a) {
      if (a.length < 1) {
         this.m().send(s, "cmd.usage-boss", "bosses", String.join(", ", this.plugin.bosses().ids()));
         return;
      }

      String bossId = a[0];
      if (!this.plugin.bosses().ids().contains(bossId)) {
         this.m().send(s, "cmd.boss-failed", "boss", bossId);
         return;
      }

      Collection<EventManager.Session> all = this.plugin.events().sessions();
      if (!all.isEmpty() && a.length < 2) {
         int spawned = 0;
         for (EventManager.Session ses : all) {
            if (this.plugin.bosses().spawn(ses, bossId)) {
               spawned++;
            }
         }

         if (spawned > 0) {
            this.m().send(s, "cmd.boss-spawned", "boss", bossId);
         } else {
            this.m().send(s, "cmd.boss-failed", "boss", bossId);
         }
         return;
      }

      World targetWorld = null;
      Player targetPlayer = null;
      Location spawnLoc = null;

      if (a.length > 1) {
         Player pArg = Bukkit.getPlayer(a[1]);
         if (pArg != null) {
            targetPlayer = pArg;
            targetWorld = pArg.getWorld();
            spawnLoc = this.plugin.events().findSpawn(pArg.getLocation(), 8.0, 16.0);
            if (spawnLoc == null) {
               spawnLoc = pArg.getLocation().add(0.0, 1.0, 0.0);
            }
         } else {
            World wArg = Bukkit.getWorld(a[1]);
            if (wArg != null) {
               targetWorld = wArg;
            }
         }
      }

      if (targetWorld == null) {
         if (s instanceof Player p) {
            targetWorld = p.getWorld();
            targetPlayer = p;
            spawnLoc = this.plugin.events().findSpawn(p.getLocation(), 8.0, 16.0);
            if (spawnLoc == null) {
               spawnLoc = p.getLocation().add(0.0, 1.0, 0.0);
            }
         } else {
            targetWorld = Bukkit.getWorlds().get(0);
         }
      }

      if (spawnLoc == null) {
         List<Player> inWorld = targetWorld.getPlayers().stream()
            .filter(pl -> pl.getGameMode() == GameMode.SURVIVAL || pl.getGameMode() == GameMode.ADVENTURE)
            .toList();
         if (!inWorld.isEmpty()) {
            targetPlayer = inWorld.get(0);
            spawnLoc = this.plugin.events().findSpawn(targetPlayer.getLocation(), 8.0, 16.0);
            if (spawnLoc == null) {
               spawnLoc = targetPlayer.getLocation().add(0.0, 1.0, 0.0);
            }
         } else {
            spawnLoc = targetWorld.getSpawnLocation().add(0.0, 1.0, 0.0);
         }
      }

      if (this.plugin.bosses().spawnAt(spawnLoc, bossId, 1.0, targetPlayer)) {
         this.m().send(s, "cmd.boss-spawned", "boss", bossId);
      } else {
         this.m().send(s, "cmd.boss-failed", "boss", bossId);
      }
   }

   private void horde(CommandSender s, String[] a) {
      Collection<EventManager.Session> all = this.plugin.events().sessions();
      if (all.isEmpty()) {
         this.m().send(s, "event.not-active");
      } else {
         int total = 0;

         for (EventManager.Session ses : all) {
            total += this.plugin.events().spawnWave(ses);
         }

         this.m().send(s, "cmd.horde-spawned", "count", String.valueOf(total));
      }
   }

   private void killmobs(CommandSender s, String[] a) {
      Collection<EventManager.Session> all = this.plugin.events().sessions();
      if (all.isEmpty()) {
         this.m().send(s, "cmd.killed", "count", "0");
      } else {
         int removed = 0;

         for (EventManager.Session ses : all) {
            this.plugin.bosses().removeAll(ses.world);
            removed += this.plugin.events().removeTagged(ses.world);
         }

         this.m().send(s, "cmd.killed", "count", String.valueOf(removed));
      }
   }

   private void give(CommandSender s, String[] a) {
      if (a.length < 2) {
         this.m().send(s, "cmd.usage-give");
      } else {
         Player p = Bukkit.getPlayerExact(a[0]);
         if (p == null) {
            this.m().send(s, "cmd.player-unknown", "player", a[0]);
         } else {
            ItemFactory.ItemDef d = this.plugin.items().get(a[1]);
            if (d == null) {
               this.m().send(s, "cmd.item-unknown", "item", a[1]);
            } else {
               int n = 1;
               if (a.length > 2) {
                  try {
                     n = Math.max(1, Math.min(64, Integer.parseInt(a[2])));
                  } catch (NumberFormatException var12) {
                  }
               }

               int serial = 0;
               List<ItemStack> created = new ArrayList<>();
               long deadline = System.currentTimeMillis() + 2000L;

               while (System.currentTimeMillis() < deadline) {
                  ItemStack ci = this.plugin.items().create(d, 1, p.getName());
                  if (ci == null) {
                     break;
                  }

                  created.add(ci);
                  if (++serial >= n) {
                     break;
                  }
               }

               if (created.isEmpty()) {
                  this.m().send(s, "cmd.item-unknown", "item", a[1]);
               } else {
                  for (ItemStack it : created) {
                     this.plugin.loot().giveOrDrop(p, it);
                  }

                  this.m().send(s, "cmd.given", "player", p.getName(), "item", a[1], "amount", String.valueOf(created.size()));
               }
            }
         }
      }
   }

   private void status(CommandSender s) {
      EventManager.Session ses = this.plugin.events().globalSession();
      if (ses == null) {
         this.m().send(s, "cmd.status-none");
         List<ScheduleManager.Upcoming> up = this.plugin.schedule().upcoming(1);
         if (!up.isEmpty()) {
            ScheduleManager.Upcoming u = up.get(0);
            this.m()
               .send(
                  s,
                  "cmd.next-line",
                  "when",
                  this.plugin.schedule().format(u),
                  "in",
                  Util.human(this.plugin.schedule().secondsUntil(u)),
                  "difficulty",
                  Util.fmt(u.slot().difficulty())
               );
         }
      } else {
         String worldLabel = this.plugin.events().activeWorlds() > 1 ? "global" : ses.world.getName();
         this.m()
            .send(
               s,
               "cmd.status-line",
               "world",
               worldLabel,
               "time",
               Util.dur((ses.endMs - System.currentTimeMillis()) / 1000L),
               "difficulty",
               Util.fmt(ses.difficulty)
            );
         List<ScheduleManager.Upcoming> up = this.plugin.schedule().upcoming(1);
         if (!up.isEmpty()) {
            ScheduleManager.Upcoming u = up.get(0);
            this.m()
               .send(
                  s,
                  "cmd.next-line",
                  "when",
                  this.plugin.schedule().format(u),
                  "in",
                  Util.human(this.plugin.schedule().secondsUntil(u)),
                  "difficulty",
                  Util.fmt(u.slot().difficulty())
               );
         }
      }
   }

   private void next(CommandSender s) {
      List<ScheduleManager.Upcoming> up = this.plugin.schedule().upcoming(6);
      if (up.isEmpty()) {
         this.m().send(s, "cmd.next-none");
      } else {
         this.m().send(s, "cmd.next-header");

         for (ScheduleManager.Upcoming u : up) {
            s.sendMessage(
               Util.mini(
                  this.m()
                     .fill(
                        this.m().raw("cmd.next-entry"),
                        "when",
                        this.plugin.schedule().format(u),
                        "in",
                        Util.human(this.plugin.schedule().secondsUntil(u)),
                        "difficulty",
                        Util.fmt(u.slot().difficulty())
                     )
               )
            );
         }
      }
   }

   private void lightning(CommandSender s, String[] a) {
      if (a.length == 0 || a[0].equalsIgnoreCase("help") || a[0].equalsIgnoreCase("помощь")) {
         s.sendMessage(Util.mini("<dark_red><bold>⚡ УПРАВЛЕНИЕ МОЛНИЯМИ КРОВАВОЙ ЛУНЫ:</bold></dark_red>"));
         s.sendMessage(Util.mini("<yellow>/luna lightning storm [игрок|all] [уд/сек] [радиус] [сек]</yellow> <gray>- Периметр молний вокруг игрока</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna lightning smite <игрок> [интервал_сек] [урон_хп] [сек]</yellow> <gray>- Прицельные удары с уроном</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna lightning strike <игрок> [урон_хп]</yellow> <gray>- Одиночный удар молнии</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna lightning stop [игрок|all]</yellow> <gray>- Остановить эффекты молний</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna lightning status</yellow> <gray>- Показать активные эффекты</gray>"));
         s.sendMessage(Util.mini("<gray>Подсказка: 1.0 HP = 0.5 ❤ (полсердечка), 2.0 HP = 1 ❤, 20.0 HP = полное здоровье.</gray>"));
         return;
      }

      String action = a[0].toLowerCase(Locale.ROOT);
      LightningEffectManager lm = this.plugin.lightning();
      if (lm == null) {
         s.sendMessage(Util.mini("<red>Модуль молний не инициализирован.</red>"));
         return;
      }

      switch (action) {
         case "storm":
         case "шторм": {
            if (a.length > 1 && a[1].equalsIgnoreCase("stop")) {
               lm.stopStorm(LightningEffectManager.ALL_PLAYERS);
               s.sendMessage(Util.mini("<green>✔ Все штормовые молнии остановлены.</green>"));
               return;
            }

            UUID targetId = LightningEffectManager.ALL_PLAYERS;
            String targetName = "всех игроков";

            int argIdx = 1;
            if (a.length > 1) {
               if (a[1].equalsIgnoreCase("all") || a[1].equalsIgnoreCase("все")) {
                  targetId = LightningEffectManager.ALL_PLAYERS;
                  targetName = "всех игроков";
                  argIdx = 2;
               } else {
                  Player p = Bukkit.getPlayerExact(a[1]);
                  if (p != null) {
                     targetId = p.getUniqueId();
                     targetName = p.getName();
                     argIdx = 2;
                  } else if (!isDouble(a[1]) && !isInteger(a[1])) {
                     s.sendMessage(Util.mini("<red>Игрок '" + a[1] + "' не найден.</red>"));
                     return;
                  }
               }
            }

            double rate = 3.0;
            if (a.length > argIdx) {
               try { rate = Math.max(0.1, Double.parseDouble(a[argIdx])); } catch (NumberFormatException ignored) {}
               argIdx++;
            }

            double radius = 15.0;
            if (a.length > argIdx) {
               try { radius = Math.max(1.0, Double.parseDouble(a[argIdx])); } catch (NumberFormatException ignored) {}
               argIdx++;
            }

            long duration = 0L;
            if (a.length > argIdx) {
               try { duration = Math.max(0L, Long.parseLong(a[argIdx])); } catch (NumberFormatException ignored) {}
            }

            lm.startStorm(targetId, rate, radius, duration);
            String durStr = duration > 0 ? " на " + duration + " сек." : " (постоянно)";
            s.sendMessage(Util.mini("<green>✔ Запущен периметр молний вокруг <yellow>" + targetName + "</yellow> (<white>" + rate + " уд/сек</white>, радиус <white>" + radius + "м</white>)" + durStr + "!</green>"));
            break;
         }
         case "smite":
         case "кара": {
            if (a.length < 2) {
               s.sendMessage(Util.mini("<yellow>Использование: /luna lightning smite <игрок> [интервал_сек] [урон_хп] [длительность_сек]</yellow>"));
               s.sendMessage(Util.mini("<gray>Пример: /luna lightning smite mike 2 1.0 30 (каждые 2 сек по 0.5 ❤ на 30 сек)</gray>"));
               return;
            }

            if (a.length > 2 && a[2].equalsIgnoreCase("stop")) {
               Player p = Bukkit.getPlayerExact(a[1]);
               if (p != null) {
                  lm.stopSmite(p.getUniqueId());
                  s.sendMessage(Util.mini("<green>✔ Прицельные молнии для <yellow>" + p.getName() + "</yellow> остановлены.</green>"));
               }
               return;
            }

            Player p = Bukkit.getPlayerExact(a[1]);
            if (p == null) {
               s.sendMessage(Util.mini("<red>Игрок '" + a[1] + "' не найден.</red>"));
               return;
            }

            int interval = 2;
            if (a.length > 2) {
               try { interval = Math.max(1, Integer.parseInt(a[2])); } catch (NumberFormatException ignored) {}
            }

            double damage = 1.0;
            if (a.length > 3) {
               try { damage = Math.max(0.0, Double.parseDouble(a[3])); } catch (NumberFormatException ignored) {}
            }

            long duration = 0L;
            if (a.length > 4) {
               try { duration = Math.max(0L, Long.parseLong(a[4])); } catch (NumberFormatException ignored) {}
            }

            lm.startSmite(p.getUniqueId(), damage, interval, duration);
            double hearts = damage / 2.0;
            String durStr = duration > 0 ? " на " + duration + " сек." : " (до остановки)";
            s.sendMessage(Util.mini("<green>✔ Запущена прицельная кара молниями по игроку <yellow>" + p.getName() + "</yellow>: каждые <white>" + interval + "с</white> урон <red>" + damage + " HP (" + hearts + " ❤)</red>" + durStr + "!</green>"));
            break;
         }
         case "strike":
         case "молния": {
            if (a.length < 2) {
               s.sendMessage(Util.mini("<yellow>Использование: /luna lightning strike <игрок> [урон_хп]</yellow>"));
               return;
            }

            Player p = Bukkit.getPlayerExact(a[1]);
            if (p == null) {
               s.sendMessage(Util.mini("<red>Игрок '" + a[1] + "' не найден.</red>"));
               return;
            }

            double damage = 1.0;
            if (a.length > 2) {
               try { damage = Math.max(0.0, Double.parseDouble(a[2])); } catch (NumberFormatException ignored) {}
            }

            lm.singleStrike(p, damage);
            s.sendMessage(Util.mini("<green>✔ Прицельная молния ударила в игрока <yellow>" + p.getName() + "</yellow> (урон: <red>" + damage + " HP</red>)!</green>"));
            break;
         }
         case "stop":
         case "стоп": {
            if (a.length > 1 && !a[1].equalsIgnoreCase("all") && !a[1].equalsIgnoreCase("все")) {
               Player p = Bukkit.getPlayerExact(a[1]);
               if (p != null) {
                  lm.stopAll(p.getUniqueId());
                  s.sendMessage(Util.mini("<green>✔ Все эффекты молний для игрока <yellow>" + p.getName() + "</yellow> остановлены.</green>"));
               } else {
                  s.sendMessage(Util.mini("<red>Игрок '" + a[1] + "' не найден.</red>"));
               }
            } else {
               lm.stopAllGlobal();
               s.sendMessage(Util.mini("<green>✔ Все активные штормы и прицельные молнии остановлены для всех игроков.</green>"));
            }
            break;
         }
         case "status":
         case "статус": {
            int stormCount = lm.getStorms().size();
            int smiteCount = lm.getSmites().size();
            s.sendMessage(Util.mini("<gold>Статус системы молний:</gold>"));
            s.sendMessage(Util.mini("<gray>- Активных штормов периметра: <yellow>" + stormCount + "</yellow></gray>"));
            s.sendMessage(Util.mini("<gray>- Активных прицельных кар (smite): <yellow>" + smiteCount + "</yellow></gray>"));
            break;
         }
         default:
            s.sendMessage(Util.mini("<red>Неизвестное действие. Введите /luna lightning help для подсказок.</red>"));
      }
   }

   private void time(CommandSender s, String[] a) {
      if (a.length == 0 || a[0].equalsIgnoreCase("status") || a[0].equalsIgnoreCase("статус")) {
         Collection<EventManager.Session> sessions = this.plugin.events().sessions();
         if (sessions.isEmpty()) {
            s.sendMessage(Util.mini("<gray>Сейчас нет активной Кровавой Луны.</gray>"));
            return;
         }
         long now = System.currentTimeMillis();
         s.sendMessage(Util.mini("<gold>Статус длительности Кровавой Луны:</gold>"));
         for (EventManager.Session ses : sessions) {
            long remMs = Math.max(0L, ses.endMs - now);
            long totalSec = remMs / 1000L;
            long m = totalSec / 60L;
            long sec = totalSec % 60L;
            s.sendMessage(Util.mini(
               "<gray>- Мир <white>" + ses.world.getName() + "</white>: <yellow>"
               + m + "м " + sec + "с</yellow> (сложность: <yellow>x"
               + String.format(Locale.ROOT, "%.2f", ses.difficulty) + "</yellow>)</gray>"
            ));
         }
         return;
      }

      String action = a[0].toLowerCase(Locale.ROOT);
      if (action.equals("help") || action.equals("помощь") || action.equals("?")) {
         s.sendMessage(Util.mini("<gold>Команды управления таймером Кровавой Луны:</gold>"));
         s.sendMessage(Util.mini("<yellow>/luna time status</yellow> <gray>- просмотр оставшегося времени</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna time add <время> [мир]</yellow> <gray>- добавить время (пример: /luna time add 5m)</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna time take <время> [мир]</yellow> <gray>- сократить время (пример: /luna time take 2m)</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna time set <время> [мир]</yellow> <gray>- установить оставшееся время (пример: /luna time set 3m)</gray>"));
         return;
      }

      if (a.length < 2) {
         s.sendMessage(Util.mini("<red>Использование: /luna time add|take|set <время (например 5m, 30s)> [мир]</red>"));
         return;
      }

      Long durMs = parseDurationMs(a[1]);
      if (durMs == null || durMs <= 0L) {
         s.sendMessage(Util.mini("<red>Некорректный формат времени. Примеры: 30s, 2m, 5m, 1h или число секунд.</red>"));
         return;
      }

      World targetWorld = null;
      if (a.length >= 3 && !a[2].equalsIgnoreCase("all") && !a[2].equalsIgnoreCase("все")) {
         targetWorld = Bukkit.getWorld(a[2]);
         if (targetWorld == null) {
            s.sendMessage(Util.mini("<red>Мир '" + a[2] + "' не найден.</red>"));
            return;
         }
      }

      if (action.equals("add") || action.equals("добавить") || action.equals("+")) {
         int count = this.plugin.events().adjustDuration(targetWorld, durMs);
         if (count == 0) {
            s.sendMessage(Util.mini("<gray>Нет активной Кровавой Луны в указанном мире.</gray>"));
         } else {
            s.sendMessage(Util.mini("<green>✔ Длительность Кровавой Луны увеличена на <yellow>" + formatDuration(durMs) + "</yellow> (затронуто миров: " + count + ").</green>"));
         }
      } else if (action.equals("take") || action.equals("sub") || action.equals("убавить") || action.equals("отнять") || action.equals("-")) {
         int count = this.plugin.events().adjustDuration(targetWorld, -durMs);
         if (count == 0) {
            s.sendMessage(Util.mini("<gray>Нет активной Кровавой Луны в указанном мире.</gray>"));
         } else {
            s.sendMessage(Util.mini("<green>✔ Длительность Кровавой Луны сокращена на <yellow>" + formatDuration(durMs) + "</yellow> (затронуто миров: " + count + ").</green>"));
         }
      } else if (action.equals("set") || action.equals("установить") || action.equals("задать")) {
         int count = this.plugin.events().setDurationRemaining(targetWorld, durMs);
         if (count == 0) {
            s.sendMessage(Util.mini("<gray>Нет активной Кровавой Луны в указанном мире.</gray>"));
         } else {
            s.sendMessage(Util.mini("<green>✔ Оставшееся время Кровавой Луны установлено на <yellow>" + formatDuration(durMs) + "</yellow> (затронуто миров: " + count + ").</green>"));
         }
      } else {
         s.sendMessage(Util.mini("<red>Неизвестное действие. Введите /luna time help для справки.</red>"));
      }
   }

   private void dynamic(CommandSender s, String[] a) {
      DynamicDifficultyManager dm = this.plugin.dynamicDifficulty();
      if (dm == null) {
         s.sendMessage(Util.mini("<red>Модуль динамической сложности недоступен.</red>"));
         return;
      }

      if (a.length == 0 || a[0].equalsIgnoreCase("status") || a[0].equalsIgnoreCase("статус")) {
         s.sendMessage(Util.mini("<gold>Статус динамической сложности:</gold>"));
         s.sendMessage(Util.mini("<gray>- Включено в конфиге: <yellow>" + dm.isEnabled() + "</yellow></gray>"));
         Collection<EventManager.Session> sessions = this.plugin.events().sessions();
         if (sessions.isEmpty()) {
            s.sendMessage(Util.mini("<gray>- Сейчас нет активных сессий Кровавой Луны.</gray>"));
         } else {
            for (EventManager.Session ses : sessions) {
               double base = dm.getBaseDifficulty(ses.world.getName());
               s.sendMessage(Util.mini(
                  "<gray>- Мир <white>" + ses.world.getName() + "</white>: текущая сложность <yellow>x"
                  + String.format(Locale.ROOT, "%.2f", ses.difficulty) + "</yellow> (базовая: x"
                  + String.format(Locale.ROOT, "%.2f", base) + ")</gray>"
               ));
            }
         }
         return;
      }

      String action = a[0].toLowerCase(Locale.ROOT);
      if (action.equals("on") || action.equals("enable") || action.equals("вкл")) {
         dm.setEnabled(true);
         s.sendMessage(Util.mini("<green>✔ Динамическая сложность включена.</green>"));
      } else if (action.equals("off") || action.equals("disable") || action.equals("выкл")) {
         dm.setEnabled(false);
         s.sendMessage(Util.mini("<yellow>⚡ Динамическая сложность отключена.</yellow>"));
      } else if (action.equals("reset") || action.equals("сброс")) {
         if (a.length >= 2 && !a[1].equalsIgnoreCase("all")) {
            dm.resetDifficulty(a[1]);
            s.sendMessage(Util.mini("<green>✔ Сложность сброшена к базовой в мире " + a[1] + ".</green>"));
         } else {
            for (EventManager.Session ses : this.plugin.events().sessions()) {
               dm.resetDifficulty(ses.world.getName());
            }
            s.sendMessage(Util.mini("<green>✔ Сложность сброшена к базовой во всех активных мирах.</green>"));
         }
      } else {
         s.sendMessage(Util.mini("<gold>Команды динамической сложности:</gold>"));
         s.sendMessage(Util.mini("<yellow>/luna dynamic status</yellow> <gray>- текущий статус и сложность</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna dynamic on|off</yellow> <gray>- включить/выключить эскалацию</gray>"));
         s.sendMessage(Util.mini("<yellow>/luna dynamic reset [мир]</yellow> <gray>- сбросить сложность к базовой</gray>"));
      }
   }

   private static Long parseDurationMs(String input) {
      if (input == null || input.trim().isEmpty()) return null;
      String str = input.trim().toLowerCase(Locale.ROOT);
      long multiplier = 1000L;
      if (str.endsWith("ms") || str.endsWith("мс")) {
         str = str.substring(0, str.length() - 2);
         multiplier = 1L;
      } else if (str.endsWith("s") || str.endsWith("с") || str.endsWith("sec")) {
         str = str.replaceAll("[^0-9.]", "");
         multiplier = 1000L;
      } else if (str.endsWith("m") || str.endsWith("м") || str.endsWith("min")) {
         str = str.replaceAll("[^0-9.]", "");
         multiplier = 60000L;
      } else if (str.endsWith("h") || str.endsWith("ч") || str.endsWith("hour")) {
         str = str.replaceAll("[^0-9.]", "");
         multiplier = 3600000L;
      } else {
         str = str.replaceAll("[^0-9.]", "");
         multiplier = 1000L;
      }
      try {
         double val = Double.parseDouble(str);
         return (long)(val * multiplier);
      } catch (NumberFormatException e) {
         return null;
      }
   }

   private static String formatDuration(long ms) {
      long totalSec = Math.max(0L, ms / 1000L);
      long m = totalSec / 60L;
      long s = totalSec % 60L;
      if (m > 0 && s > 0) return m + "м " + s + "с";
      if (m > 0) return m + "м";
      return s + "с";
   }

   private static boolean isDouble(String str) {
      try { Double.parseDouble(str); return true; } catch (NumberFormatException e) { return false; }
   }

   private static boolean isInteger(String str) {
      try { Integer.parseInt(str); return true; } catch (NumberFormatException e) { return false; }
   }

   static {
      for (String s : new String[]{"next", "start", "stop", "difficulty", "boss", "horde", "killmobs", "give", "items", "chances", "reload", "warn", "evolution", "countdown", "cancel", "lightning", "time", "duration", "dynamic", "black", "purple", "blood"}) {
         ALIASES.put(s, s);
      }

      ALIASES.put("proxima", "next");
      ALIASES.put("siguiente", "next");
      ALIASES.put("iniciar", "start");
      ALIASES.put("detener", "stop");
      ALIASES.put("parar", "stop");
      ALIASES.put("dificultad", "difficulty");
      ALIASES.put("jefe", "boss");
      ALIASES.put("horda", "horde");
      ALIASES.put("limpiar", "killmobs");
      ALIASES.put("dar", "give");
      ALIASES.put("probabilidades", "chances");
      ALIASES.put("recargar", "reload");
      ALIASES.put("предупредить", "warn");
      ALIASES.put("оповестить", "warn");
      ALIASES.put("эволюция", "evolution");
      ALIASES.put("отсчет", "countdown");
      ALIASES.put("таймер", "time");
      ALIASES.put("отмена", "cancel");
      ALIASES.put("abort", "cancel");
      ALIASES.put("молнии", "lightning");
      ALIASES.put("гроза", "lightning");
      ALIASES.put("шторм", "lightning");
      ALIASES.put("время", "time");
      ALIASES.put("длительность", "duration");
      ALIASES.put("динамика", "dynamic");
      ALIASES.put("черная", "black");
      ALIASES.put("чёрная", "black");
      ALIASES.put("фиолетовая", "purple");
      ALIASES.put("кровавая", "blood");
   }
}
