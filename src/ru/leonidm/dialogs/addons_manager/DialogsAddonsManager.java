package ru.leonidm.dialogs.addons_manager;

import org.bukkit.plugin.java.JavaPlugin;

public class DialogsAddonsManager extends JavaPlugin {

    private static DialogsAddonsManager instance;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("daddons").setExecutor(new DialogsAddonsCommand());
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    public static DialogsAddonsManager getInstance() {
        return instance;
    }
}
