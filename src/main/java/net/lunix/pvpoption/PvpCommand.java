package net.lunix.pvpoption;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PvpCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) { send(sender, "§cOnly players can use this command."); return true; }
            toggle(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "on"     -> { if (requirePlayer(sender)) setFlag((Player) sender, true);  }
            case "off"    -> { if (requirePlayer(sender)) setFlag((Player) sender, false); }
            case "status" -> { if (requirePlayer(sender)) status((Player) sender);         }
            case "list"   -> listFlagged(sender);
            case "admin"  -> handleAdmin(sender, args);
            default       -> send(sender, "§cUnknown subcommand. Use /pvpoption.");
        }
        return true;
    }

    private boolean requirePlayer(CommandSender sender) {
        if (sender instanceof Player) return true;
        send(sender, "§cOnly players can use this command.");
        return false;
    }

    private void toggle(Player player) {
        boolean current = PvpPlayerData.isPvpFlagged(player.getUniqueId());
        boolean inWarmup = PvpManager.warmupExpiry.containsKey(player.getUniqueId());
        setFlag(player, !current && !inWarmup);
    }

    private void setFlag(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (!PvpManager.pvpEnabled) { send(player, "§cPvP flagging is currently disabled server-wide."); return; }

        if (enabled && PvpPlayerData.isLocked(uuid)) {
            send(player, "§cYour PvP access has been restricted by an admin.");
            return;
        }

        if (!enabled) {
            if (PvpManager.warmupExpiry.containsKey(uuid)) {
                PvpManager.warmupExpiry.remove(uuid);
                send(player, "§ePvP flag warmup cancelled.");
                return;
            }
            if (PvpPlayerData.isPvpFlagged(uuid)) {
                long remaining = PvpManager.getRemainingCooldown(uuid);
                if (remaining > 0) { send(player, "§cYou are in combat! Cannot disable PvP for another " + remaining + " second(s)."); return; }
                PvpManager.deactivateFlag(player);
                send(player, "§aPvP DISABLED. You are now protected from other players.");
            } else {
                send(player, "§7PvP is already disabled.");
            }
            return;
        }

        if (PvpPlayerData.isPvpFlagged(uuid) || PvpManager.warmupExpiry.containsKey(uuid)) {
            send(player, "§7PvP is already enabled (or warming up).");
            return;
        }

        if (PvpManager.warmupSeconds > 0) {
            PvpManager.warmupExpiry.put(uuid, System.currentTimeMillis() + (PvpManager.warmupSeconds * 1000L));
            send(player, "§eEntering PvP mode in " + PvpManager.warmupSeconds + " second(s)... Use /pvp off to cancel.");
        } else {
            PvpManager.activateFlag(player);
            send(player, "§cPvP ENABLED. You can now deal and take damage from other flagged players.");
        }
    }

    private void status(Player player) {
        UUID uuid = player.getUniqueId();
        boolean flagged = PvpPlayerData.isPvpFlagged(uuid);
        boolean inWarmup = PvpManager.warmupExpiry.containsKey(uuid);
        boolean locked = PvpPlayerData.isLocked(uuid);
        if (inWarmup) {
            send(player, "§eYour PvP status: WARMING UP");
        } else {
            send(player, "Your PvP status: " + (flagged ? "§cENABLED" : "§aDISABLED")
                + (locked ? " §4[RESTRICTED]" : ""));
        }
        if (flagged) {
            long remaining = PvpManager.getRemainingCooldown(uuid);
            if (remaining > 0) send(player, "§e  Combat cooldown: " + remaining + " second(s) remaining");
        }
    }

    private void listFlagged(CommandSender sender) {
        var flagged = Bukkit.getOnlinePlayers().stream()
            .filter(p -> PvpPlayerData.isPvpFlagged(p.getUniqueId()))
            .toList();
        if (flagged.isEmpty()) {
            send(sender, "§7No players are currently flagged for PvP.");
        } else {
            send(sender, "§6--- Players flagged for PvP ---");
            for (Player p : flagged) {
                long remaining = PvpManager.getRemainingCooldown(p.getUniqueId());
                send(sender, "  §c" + p.getName() + (remaining > 0 ? "§e  (in combat, " + remaining + "s)" : ""));
            }
        }
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pvpoption.admin")) { send(sender, "§cYou do not have permission."); return; }
        if (args.length < 2) { showAdminSettings(sender); return; }

        switch (args[1].toLowerCase()) {
            case "enable"  -> setSystemEnabled(sender, true);
            case "disable" -> setSystemEnabled(sender, false);
            case "warmup" -> {
                if (args.length < 3) { send(sender, "Warmup: §b" + PvpManager.warmupSeconds + " second(s)"); return; }
                parseInt(sender, args[2], v -> { PvpManager.warmupSeconds = v; PvpManager.saveConfig(); send(sender, "PvP flag warmup set to §b" + v + " second(s)"); });
            }
            case "cooldown" -> {
                if (args.length < 3) { send(sender, "Cooldown: §b" + PvpManager.cooldownSeconds + " second(s)"); return; }
                parseInt(sender, args[2], v -> { PvpManager.cooldownSeconds = v; PvpManager.saveConfig(); send(sender, "PvP combat cooldown set to §b" + v + " second(s)"); });
            }
            case "autounflag" -> {
                if (args.length < 3) { send(sender, "Auto-unflag: §b" + PvpManager.autoUnflagMinutes + " minute(s)"); return; }
                parseInt(sender, args[2], v -> { PvpManager.autoUnflagMinutes = v; PvpManager.saveConfig(); send(sender, "Auto-unflag idle time set to §b" + v + " minute(s)"); });
            }
            case "broadcast" -> {
                if (args.length < 3) { send(sender, "Broadcast: " + (PvpManager.broadcastToggle ? "§aON" : "§cOFF")); return; }
                boolean on = args[2].equalsIgnoreCase("on");
                PvpManager.broadcastToggle = on;
                PvpManager.saveConfig();
                send(sender, "PvP flag toggle broadcast: " + (on ? "§aON" : "§cOFF"));
            }
            case "reload" -> {
                boolean silent = args.length >= 3 && args[2].equalsIgnoreCase("silent");
                PvpManager.loadConfig();
                String msg = "§a[pvpOption] Config reloaded by an admin.";
                if (silent) send(sender, msg);
                else Bukkit.getOnlinePlayers().forEach(p -> send(p, msg));
            }
            case "set" -> {
                if (args.length < 4) { send(sender, "§cUsage: /pvpoption admin set <player> on|off"); return; }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) { send(sender, "§cPlayer not found."); return; }
                adminSet(sender, target, args[3].equalsIgnoreCase("on"));
            }
            case "status" -> {
                if (args.length < 3) { showAdminSettings(sender); return; }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) { send(sender, "§cPlayer not found."); return; }
                adminStatus(sender, target);
            }
            case "lock" -> {
                if (args.length < 3) { send(sender, "§cUsage: /pvpoption admin lock <player>"); return; }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) { send(sender, "§cPlayer not found."); return; }
                adminLock(sender, target);
            }
            case "unlock" -> {
                if (args.length < 3) { send(sender, "§cUsage: /pvpoption admin unlock <player>"); return; }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) { send(sender, "§cPlayer not found."); return; }
                adminUnlock(sender, target);
            }
            default -> showAdminSettings(sender);
        }
    }

    private void showAdminSettings(CommandSender sender) {
        send(sender, "§6--- pvpOption Admin Settings ---");
        send(sender, "  system:            " + (PvpManager.pvpEnabled ? "§aENABLED" : "§cDISABLED"));
        send(sender, "  warmupSeconds:     §b" + PvpManager.warmupSeconds   + (PvpManager.warmupSeconds   == 0 ? "§7  (disabled)" : ""));
        send(sender, "  cooldownSeconds:   §b" + PvpManager.cooldownSeconds  + (PvpManager.cooldownSeconds  == 0 ? "§7  (disabled)" : ""));
        send(sender, "  autoUnflagMinutes: §b" + PvpManager.autoUnflagMinutes + (PvpManager.autoUnflagMinutes == 0 ? "§7  (disabled)" : ""));
        send(sender, "  broadcastToggle:   " + (PvpManager.broadcastToggle ? "§aON" : "§cOFF"));
    }

    private void setSystemEnabled(CommandSender sender, boolean enabled) {
        PvpManager.pvpEnabled = enabled;
        PvpManager.saveConfig();
        PvpManager.applyServerPvp(enabled);
        if (!enabled) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                UUID uuid = p.getUniqueId();
                if (PvpManager.warmupExpiry.remove(uuid) != null)
                    send(p, "§eYour PvP flag warmup was cancelled (system disabled).");
                PvpManager.cooldownExpiry.remove(uuid);
            });
        }
        String msg = "§6[pvpOption] PvP flagging has been " + (enabled ? "§aENABLED" : "§cDISABLED") + "§6 server-wide by an admin.";
        Bukkit.getOnlinePlayers().forEach(p -> send(p, msg));
        send(sender, msg);
    }

    private void adminSet(CommandSender sender, Player target, boolean enabled) {
        UUID uuid = target.getUniqueId();
        PvpManager.warmupExpiry.remove(uuid);
        boolean alreadyFlagged = PvpPlayerData.isPvpFlagged(uuid);
        String state = enabled ? "§cENABLED" : "§aDISABLED";
        if (enabled && !alreadyFlagged) PvpManager.activateFlag(target);
        else if (!enabled && alreadyFlagged) { PvpManager.cooldownExpiry.remove(uuid); PvpManager.deactivateFlag(target); }
        send(sender, "Set " + target.getName() + "'s PvP flag to " + state);
        send(target, "An admin has set your PvP flag to " + state);
    }

    private void adminStatus(CommandSender sender, Player target) {
        UUID uuid = target.getUniqueId();
        boolean flagged = PvpPlayerData.isPvpFlagged(uuid);
        boolean inWarmup = PvpManager.warmupExpiry.containsKey(uuid);
        boolean locked = PvpPlayerData.isLocked(uuid);
        long remaining = PvpManager.getRemainingCooldown(uuid);
        String statusStr = inWarmup ? "§eWARMING UP" : flagged ? "§cENABLED" : "§aDISABLED";
        send(sender, target.getName() + "'s PvP status: " + statusStr
            + (remaining > 0 ? "§e  (" + remaining + "s cooldown)" : "")
            + (locked ? " §4[RESTRICTED]" : ""));
    }

    private void adminLock(CommandSender sender, Player target) {
        UUID uuid = target.getUniqueId();
        PvpManager.warmupExpiry.remove(uuid);
        PvpManager.cooldownExpiry.remove(uuid);
        if (PvpPlayerData.isPvpFlagged(uuid)) PvpManager.deactivateFlag(target);
        PvpPlayerData.setLocked(uuid, true);
        send(sender, "§aLocked " + target.getName() + " — PvP disabled and restricted.");
        send(target, "§cAn admin has restricted your PvP access.");
    }

    private void adminUnlock(CommandSender sender, Player target) {
        PvpPlayerData.setLocked(target.getUniqueId(), false);
        send(sender, "§aUnlocked " + target.getName() + " — PvP access restored.");
        send(target, "§aYour PvP access has been restored by an admin.");
    }

    private void parseInt(CommandSender sender, String str, java.util.function.IntConsumer action) {
        try {
            int v = Integer.parseInt(str);
            if (v < 0) throw new NumberFormatException();
            action.accept(v);
        } catch (NumberFormatException e) {
            send(sender, "§cInvalid number.");
        }
    }

    private void send(CommandSender sender, String msg) {
        sender.sendMessage(msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1)
            return filter(List.of("on", "off", "status", "list", "admin"), args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("pvpoption.admin"))
            return filter(List.of("enable", "disable", "warmup", "cooldown", "autounflag", "broadcast", "reload", "set", "status", "lock", "unlock"), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            String sub = args[1].toLowerCase();
            if (sub.equals("broadcast")) return filter(List.of("on", "off"), args[2]);
            if (sub.equals("reload"))    return filter(List.of("silent"), args[2]);
            if (sub.equals("set") || sub.equals("status") || sub.equals("lock") || sub.equals("unlock"))
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("set"))
            return filter(List.of("on", "off"), args[3]);
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        return options.stream().filter(s -> s.startsWith(prefix.toLowerCase())).toList();
    }
}
