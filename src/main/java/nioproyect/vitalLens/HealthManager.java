package nioproyect.vitalLens;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HealthManager {

    private final VitalLens plugin;

    private final Map<UUID, TextDisplay> displays = new HashMap<>();
    private final Map<UUID, String> lastText = new HashMap<>();

    private final double VIEW_DISTANCE = 10;

    public HealthManager(VitalLens plugin) {
        this.plugin = plugin;
    }

    public void createDisplay(LivingEntity entity) {

        UUID uuid = entity.getUniqueId();

        if (displays.containsKey(uuid)) return;
        if (!isPlayerNearby(entity)) return;

        Location loc = entity.getEyeLocation().add(0, 0.5, 0);

        TextDisplay text = entity.getWorld().spawn(loc, TextDisplay.class);

        text.setBillboard(Display.Billboard.VERTICAL);
        text.setSeeThrough(true);
        text.setShadowed(false);

        text.setInterpolationDuration(3);
        text.setInterpolationDelay(0);

        text.setViewRange(8);

        displays.put(uuid, text);

        updateDisplay(entity);
    }

    public void updateDisplay(LivingEntity entity) {

        UUID uuid = entity.getUniqueId();
        TextDisplay display = displays.get(uuid);

        if (display == null) return;

        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();

        double percent = health / maxHealth;

        String color;
        if (percent > 0.6) color = "§a";
        else if (percent > 0.3) color = "§e";
        else color = "§c";

        int bars = (int) (percent * 10);

        StringBuilder bar = new StringBuilder("§8[");

        for (int i = 0; i < bars; i++) bar.append(color).append("█");
        for (int i = bars; i < 10; i++) bar.append("§7░");

        bar.append("§8]");

        String text = entity.getName() + " " + bar + " §7" +
                (int) health + "/" + (int) maxHealth + "❤";

        if (!text.equals(lastText.get(uuid))) {
            display.text(Component.text(text));
            lastText.put(uuid, text);
        }
    }


    public void updatePositions() {

        Iterator<Map.Entry<UUID, TextDisplay>> iterator = displays.entrySet().iterator();

        while (iterator.hasNext()) {

            var entry = iterator.next();

            Entity entity = Bukkit.getEntity(entry.getKey());

            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                entry.getValue().remove();
                iterator.remove();
                lastText.remove(entry.getKey());
                continue;
            }


            if (!isPlayerNearby(living)) {
                entry.getValue().remove();
                iterator.remove();
                lastText.remove(entry.getKey());
                continue;
            }

            TextDisplay display = entry.getValue();

            Location newLoc = living.getEyeLocation().add(0, 0.5, 0);

            if (display.getLocation().distanceSquared(newLoc) > 0.002) {
                display.teleport(newLoc);
            }
        }
    }


    public void autoCreateNearby() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            for (Entity entity : player.getNearbyEntities(VIEW_DISTANCE, VIEW_DISTANCE, VIEW_DISTANCE)) {

                if (!(entity instanceof LivingEntity living)) continue;
                if (living.isDead()) continue;

                if (!displays.containsKey(living.getUniqueId())) {
                    createDisplay(living);
                }
            }
        }
    }

    private boolean isPlayerNearby(LivingEntity entity) {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distanceSquared(entity.getLocation()) <= VIEW_DISTANCE * VIEW_DISTANCE) {
                return true;
            }
        }

        return false;
    }

    public void remove(Entity entity) {

        UUID uuid = entity.getUniqueId();

        TextDisplay display = displays.remove(uuid);

        if (display != null) display.remove();

        lastText.remove(uuid);
    }

    public Map<UUID, TextDisplay> getDisplays() {
        return displays;
    }
}