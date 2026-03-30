package nioproyect.vitalLens;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobListener implements Listener {

    private final HealthManager healthManager;

    public MobListener(HealthManager healthManager) {
        this.healthManager = healthManager;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        healthManager.remove(entity);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity entity)) return;


        Bukkit.getScheduler().runTaskLater(healthManager.getPlugin(), () -> {
            healthManager.updateDisplay(entity);
        }, 1L);
    }
}