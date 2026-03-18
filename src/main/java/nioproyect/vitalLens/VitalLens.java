package nioproyect.vitalLens;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class VitalLens extends JavaPlugin {

    private HealthManager healthManager;

    @Override
    public void onEnable() {

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

    public HealthManager getHealthManager() {
        return healthManager;
    }
}