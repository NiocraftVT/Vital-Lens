package nioproyect.vitalLens;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HealthManager {

    private final VitalLens plugin;
    private final Map<UUID, TextDisplay> displays = new HashMap<>();
    private final Map<UUID, String> lastText = new HashMap<>();
    private final double VIEW_DISTANCE = 8;


    private YamlConfiguration lang;

    public HealthManager(VitalLens plugin) {
        this.plugin = plugin;


        loadLanguage();
    }

    public VitalLens getPlugin() {
        return plugin;
    }


    private void loadLanguage() {

        String langName = plugin.getConfig().getString("language", "en");

        File folder = new File(plugin.getDataFolder(), "lang");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, langName + ".yml");


        if (!file.exists()) {
            plugin.getLogger().warning("Language file not found: " + langName + ".yml");
            lang = null;
            return;
        }

        lang = YamlConfiguration.loadConfiguration(file);
    }

    public void createDisplay(LivingEntity entity) {
        if (entity == null || !entity.isValid()) return;

        UUID uuid = entity.getUniqueId();
        if (displays.containsKey(uuid)) return;
        if (!isPlayerNearby(entity)) return;

        Location loc = entity.getEyeLocation().add(0, 0.5, 0);

        try {
            TextDisplay text = entity.getWorld().spawn(loc, TextDisplay.class);

            text.setBillboard(Display.Billboard.VERTICAL);
            text.setSeeThrough(true);
            text.setShadowed(false);

            int width = plugin.getConfig().getInt("health-bar.width", 120);
            text.setLineWidth(width);

            text.setViewRange(8);

            text.setInterpolationDuration(3);
            text.setInterpolationDelay(0);

            displays.put(uuid, text);

            updateDisplay(entity);

        } catch (Exception ignored) {}
    }

    public void updateDisplay(LivingEntity entity) {

        TextDisplay display = displays.get(entity.getUniqueId());
        if (display == null) return;

        display.setLineWidth(plugin.getConfig().getInt("health-bar.width", 120));

        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();

        boolean numbersEnabled = plugin.getConfig().getBoolean("health-bar.show-numbers");

        String type = getMobType(entity);


        String text = type + "§f" + getTranslatedName(entity) + "\n";

        text += buildBar(health, maxHealth);

        if (numbersEnabled) {
            String color = getHealthColor(health, maxHealth);
            String heart = plugin.getConfig().getString("health-bar.heart-symbol", "❤");
            text += " §7" + color + (int) health + "/" + (int) maxHealth + heart;
        }

        display.setText(text);
    }


    private String getTranslatedName(LivingEntity entity) {

        if (lang == null) return entity.getName();

        String key = "entities." + entity.getType().name();

        return lang.getString(key, entity.getName());
    }

    private String buildBar(double health, double maxHealth) {

        int length = 10;
        int filled = (int) ((health / maxHealth) * length);

        String style = plugin.getConfig().getString("health-bar.style", "default");

        String[] styleSymbols = BarStyles.getStyle(style);


        if (styleSymbols == null || styleSymbols.length < 2) {
            styleSymbols = BarStyles.getStyle("default");
        }

        String full = styleSymbols[0];
        String empty = styleSymbols[1];

        StringBuilder bar = new StringBuilder();
        bar.append("§7[");


        bar.append(getHealthColor(health, maxHealth));
        for (int i = 0; i < filled; i++) {
            bar.append(full);
        }


        bar.append("§7");
        for (int i = filled; i < length; i++) {
            bar.append(empty);
        }

        bar.append("§7]");

        return bar.toString();
    }

    private String getHealthColor(double health, double maxHealth) {

        double percent = health / maxHealth;

        if (percent >= 0.75) return colorHex("#0FF200");
        if (percent >= 0.50) return colorHex("#ffee00");
        if (percent >= 0.25) return colorHex("#ff9900");
        return colorHex("#ff3b3b");
    }

    private String colorHex(String hex) {

        if (!hex.startsWith("#")) return hex;

        hex = hex.replace("#", "");

        StringBuilder color = new StringBuilder("§x");

        for (char c : hex.toCharArray()) {
            color.append("§").append(c);
        }

        return color.toString();
    }

    private String getMobType(LivingEntity entity) {

        if (!plugin.getConfig().getBoolean("mob-type-icon.enable"))
            return "";


        if (entity instanceof Monster)
            return colorHex("#ff3b3b") + "[\uD83D\uDDE1] HOSTIL " + "§f";


        if (entity.getLastDamageCause() != null) {


            if (entity.getLastDamageCause() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent damage) {

                if (damage.getDamager() instanceof Player) {
                    return colorHex("#ff3b3b") + "[\uD83D\uDDE1] HOSTIL " + "§f";
                }
            }
        }

        if (entity instanceof Animals)
            return colorHex("#5CFF5C") + "[❀] PACIFICO " + "§f";


        if (entity instanceof Villager)
            return colorHex("#5CFF5C") + "[❀] PACIFICO " + "§f";


        return colorHex("#FFD93B") + "[\uD83C\uDF1F] NEUTRAL " + "§f";
    }

    private boolean isPlayerNearby(LivingEntity entity) {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distanceSquared(entity.getLocation()) <= VIEW_DISTANCE * VIEW_DISTANCE)
                return true;
        }
        return false;
    }

    public void remove(Entity entity) {

        UUID uuid = entity.getUniqueId();
        TextDisplay display = displays.remove(uuid);

        if (display != null) safeRemove(display);

        lastText.remove(uuid);
    }

    private void safeRemove(TextDisplay display) {
        try {
            display.remove();
        } catch (Exception ignored) {}
    }

    public void updatePositions() {

        Iterator<Map.Entry<UUID, TextDisplay>> iterator = displays.entrySet().iterator();

        while (iterator.hasNext()) {

            var entry = iterator.next();

            Entity entity = Bukkit.getEntity(entry.getKey());

            if (!(entity instanceof LivingEntity living) || !living.isValid() || living.isDead()) {
                safeRemove(entry.getValue());
                iterator.remove();
                lastText.remove(entry.getKey());
                continue;
            }

            if (!isPlayerNearby(living)) {
                safeRemove(entry.getValue());
                iterator.remove();
                lastText.remove(entry.getKey());
                continue;
            }

            Location newLoc = living.getEyeLocation().add(0, 0.5, 0);

            updateDisplay(living);

            try {
                if (entry.getValue().getLocation().distanceSquared(newLoc) > 0.002) {
                    entry.getValue().teleport(newLoc);
                }
            } catch (Exception ignored) {}
        }
    }

    public void autoCreateNearby() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            for (Entity entity : player.getNearbyEntities(VIEW_DISTANCE, VIEW_DISTANCE, VIEW_DISTANCE)) {

                if (!(entity instanceof LivingEntity living)) continue;
                if (!living.isValid() || living.isDead()) continue;

                if (!displays.containsKey(living.getUniqueId())) {
                    createDisplay(living);
                }
            }
        }
    }

    public void reloadAllBars() {

        loadLanguage();
        plugin.reloadConfig();

        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {

                if (!entity.isDead()) {
                    updateDisplay(entity);
                }
            }
        }
    }

    public Map<UUID, TextDisplay> getDisplays() {
        return displays;
    }
}