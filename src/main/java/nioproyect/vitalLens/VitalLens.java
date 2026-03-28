package nioproyect.vitalLens;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class VitalLens extends JavaPlugin {

    private HealthManager healthManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.healthManager = new HealthManager(this);

        getServer().getPluginManager().registerEvents(new MobListener(this), this);


        Bukkit.getScheduler().runTaskTimer(this, () -> {
            healthManager.updatePositions();
        }, 0L, 1L);


        Bukkit.getScheduler().runTaskTimer(this, () -> {
            healthManager.autoCreateNearby();
        }, 0L, 1L);
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

        sender.sendMessage("usa: vitalLens reload");
        return true;

    }

    public HealthManager getHealthManager() {
        return healthManager;
    }
}