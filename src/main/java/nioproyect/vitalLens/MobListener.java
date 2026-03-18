package nioproyect.vitalLens;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class MobListener implements Listener {

    private final VitalLens plugin;

    public MobListener(VitalLens plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        plugin.getHealthManager().createDisplay(event.getEntity());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity entity)) return;


        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getHealthManager().updateDisplay(entity);
        });
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        plugin.getHealthManager().remove(event.getEntity());
    }
}