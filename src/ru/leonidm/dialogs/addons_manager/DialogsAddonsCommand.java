package ru.leonidm.dialogs.addons_manager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.leonidm.dialogs.api.AddonsLogFilter;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DialogsAddonsCommand implements TabExecutor {

    private static Field dataFolderField, configFileField;

    static {
        try {
            dataFolderField = JavaPlugin.class.getDeclaredField("dataFolder");
            dataFolderField.setAccessible(true);
            configFileField = JavaPlugin.class.getDeclaredField("configFile");
            configFileField.setAccessible(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("dialogs.addons_manager.*")) {
            sender.sendMessage("§cYou don't have enough permissions!");
            return true;
        }

        if(args.length < 2) {
            sender.sendMessage("§cUsage: §f/daddons [enable/disable] [name]§c!");
            return true;
        }



        switch(args[0]) {
            case "enable" -> {
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    stringBuilder.append(args[i]).append(' ');
                }
                String fullName = stringBuilder.toString();

                if(fullName.endsWith(" ")) fullName = fullName.substring(0, fullName.length() - 1);
                if(!fullName.endsWith(".jar")) fullName += ".jar";

                File addonFile = new File("plugins/DialogsM/addons/", fullName);
                Plugin addon;
                try {
                    addon = Bukkit.getPluginManager().loadPlugin(addonFile);
                } catch(Exception e) {
                    e.printStackTrace();
                    sender.sendMessage("§cError occurred, check console for more information! Maybe, this plugin is already enabled?");
                    return true;
                }

                File addonDirectory = new File(addonsDirectory, addon.getName());

                try {
                    dataFolderField.set(addon, addonDirectory);
                    configFileField.set(addon, new File(addonDirectory, "config.yml"));
                } catch(Exception e) {
                    e.printStackTrace();
                }
                addon.reloadConfig();

                AddonsLogFilter.addAddonToIgnore(addon.getName());

                Bukkit.getPluginManager().enablePlugin(addon);

                sender.sendMessage("§aEnabled!");
            }
            case "disable" -> {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
                if(plugin == null) {
                    sender.sendMessage("§cThis addon wasn't loaded!");
                    return true;
                }
                if(!plugin.isEnabled()) {
                    sender.sendMessage("§cThis addon is already disabled!");
                    return true;
                }

                Bukkit.getPluginManager().disablePlugin(plugin);
                sender.sendMessage("§aDisabled!");
            }
            default -> {
                sender.sendMessage("§cUsage: §f/daddons [enable/disable] [name]§c!");
            }
        }

        return true;
    }

    private static final File addonsDirectory = new File("plugins/DialogsM/addons/");
    private static final List<String> args1 = Arrays.asList("enable", "disable");
    private static List<String> files = null;
    private static long lastUpdate = 0;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("dialogs.addons_manager.*")) return null;

        switch(args.length) {
            case 1 -> {
                return args1.stream()
                        .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            case 2 -> {
                long currentTime = new Date().getTime();
                if(currentTime - lastUpdate >= 10000) {
                    files = new ArrayList<>();
                    File[] addons = addonsDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
                    for(File addon : addons) {
                       files.add(addon.getName().replace(".jar", ""));
                    }
                    lastUpdate = currentTime;
                }
                return files.stream()
                        .filter(arg -> arg.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return null;
    }
}
