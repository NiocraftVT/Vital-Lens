package nioproyect.vitalLens;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;


        plugin.getHealthManager().updateDisplay(entity);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        plugin.getHealthManager().remove(event.getEntity());
    }
}