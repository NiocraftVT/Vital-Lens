package nioproyect.vitalLens;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class VitalLens extends JavaPlugin implements TabCompleter {

    private HealthManager healthManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.healthManager = new HealthManager(this);

        getServer().getPluginManager().registerEvents(new MobListener(healthManager), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            healthManager.updatePositions();
        }, 0L, 1L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            healthManager.autoCreateNearby();
        }, 0L, 1L);


        getCommand("vitallens").setTabCompleter(this);
    }

    @Override
    public void onDisable() {

        if (healthManager != null) {
            healthManager.getDisplays().values().forEach(display -> {
                try {
                    display.remove();
                } catch (Exception ignored) {}
            });
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

            reloadConfig();
            healthManager.reloadAllBars();

            sender.sendMessage("VitalLens recargado correctamente!");
            return true;
        }

        sender.sendMessage("usa: /vitallens reload");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("vitallens")) return null;

        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.add("reload");
        }

        return list;
    }

    public HealthManager getHealthManager() {
        return healthManager;
    }
}