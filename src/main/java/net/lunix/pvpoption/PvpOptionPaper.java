package net.lunix.pvpoption;

import org.bukkit.plugin.java.JavaPlugin;

public class PvpOptionPaper extends JavaPlugin {

    private static PvpOptionPaper instance;

    @Override
    public void onEnable() {
        instance = this;
        PvpManager.init(this);
        getServer().getPluginManager().registerEvents(new PvpListener(), this);
        var cmd = getCommand("pvpoption");
        if (cmd != null) {
            var handler = new PvpCommand();
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    @Override
    public void onDisable() {
        PvpManager.shutdown();
    }

    public static PvpOptionPaper getInstance() {
        return instance;
    }
}
