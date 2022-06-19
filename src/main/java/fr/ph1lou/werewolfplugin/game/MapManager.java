package fr.ph1lou.werewolfplugin.game;

import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.game.IMapManager;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import fr.ph1lou.werewolfplugin.Main;
import fr.ph1lou.werewolfplugin.worldloader.WorldFillTask;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;


public class MapManager implements IMapManager {

    public static final String NO_FALL = "no_fall";
    private final WereWolfAPI game;
    private World world;
    private WorldFillTask wft = null;

    public MapManager(WereWolfAPI game) {
        this.game = game;
        Main main = JavaPlugin.getPlugin(Main.class);
        File mapFolder = new File(main.getDataFolder() +
                File.separator + "maps");
        if (!mapFolder.exists()) {
            if (!mapFolder.mkdirs()) {
                Bukkit.getLogger().warning("[WereWolfPlugin] Folder Map Creation Failed");
            }
        }
    }

    public void init() {
        setLobbyWorld();
        createMap(true);
    }

    @Override
    public void generateMap(int mapRadius) {

        int chunksPerRun = 80;
        if (wft == null || wft.getPercentageCompleted() == 100) {
            wft = new WorldFillTask(
                    Bukkit.getServer(),
                    world.getName(),
                    chunksPerRun,
                    false,
                    mapRadius);
            wft.setTaskID(BukkitUtils.scheduleSyncRepeatingTask(wft, 1, 1));
        }
    }


    @Override
    public void createMap() {
        createMap(true);
    }


    public void createMap(boolean roofed) {
        Bukkit.broadcastMessage(game.translate(Prefix.RED , "werewolf.commands.admin.preview.create"));
        WorldCreator wc = new WorldCreator("werewolf_map");
        wc.environment(World.Environment.NORMAL);
        wc.type(WorldType.NORMAL);
        this.world = wc.createWorld();
        BukkitUtils.scheduleSyncDelayedTask(() -> {
            setWorld(roofed);
            generateMap(game.getConfig().getBorderMax()/2);
        });
    }

    @Override
    public void loadMap() {
        loadMap(null);
    }

    @Override
    public void loadMap(@Nullable File map) {

        File werewolfWorld = this.world.getWorldFolder();

        deleteMap();
        if (map != null && map.exists()) {
            try {
                FileUtils.copyDirectory(map, werewolfWorld);
                createMap(false);
            } catch (IOException ignored) {
                this.createMap();
            }
        }
        else {
            this.createMap();
        }

    }

    @Override
    public void deleteMap() {

        if (world == null) {
            return;
        }

        if (wft != null) {
            wft.cancel();
            wft = null;
        }

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> player.getWorld().equals(world))
                .forEach(player -> player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

        try {
            Bukkit.unloadWorld(world, false);
            FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer() + File.separator + world.getName()));
            this.world = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWorld(boolean roofed) {

        try {
            world.setAutoSave(false);
            world.setWeatherDuration(0);
            world.setThundering(false);
            world.setTime(0);
            world.setPVP(false);
            VersionUtils.getVersionUtils().setGameRuleValue(world, "doFireTick", false);
            VersionUtils.getVersionUtils().setGameRuleValue(world, "reducedDebugInfo", true);
            VersionUtils.getVersionUtils().setGameRuleValue(world, "naturalRegeneration", false);
            VersionUtils.getVersionUtils().setGameRuleValue(world, "keepInventory", true);
            VersionUtils.getVersionUtils().setGameRuleValue(world, "announceAdvancements", false);
            world.save();
            int x = world.getSpawnLocation().getBlockX();
            int z = world.getSpawnLocation().getBlockZ();
            world.getWorldBorder().reset();

            if (roofed) {
                Location biome = VersionUtils.getVersionUtils().findBiome(world);
                x = biome.getBlockX();
                z = biome.getBlockZ();
            }
            world.setSpawnLocation(x, 151, z);

            for (int i = -16; i <= 16; i++) {

                for (int j = -16; j <= 16; j++) {

                    new Location(world, i + x, 150, j + z).getBlock().setType(Material.BARRIER);
                    new Location(world, i + x, 154, j + z).getBlock().setType(Material.BARRIER);
                }
                for (int j = 151; j < 154; j++) {
                    new Location(world, i + x, j, z - 16).getBlock().setType(Material.BARRIER);
                    new Location(world, i + x, j, z + 16).getBlock().setType(Material.BARRIER);
                    new Location(world, x - 16, j, i + z).getBlock().setType(Material.BARRIER);
                    new Location(world, x + 16, j, i + z).getBlock().setType(Material.BARRIER);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void changeBorder(int mapRadius) {

        if (wft != null) {
            wft.cancel();
            wft = null;
            generateMap(mapRadius);
        }
    }

    @Override
    public void transportation(IPlayerWW playerWW, double d) {

        Player player = Bukkit.getPlayer(playerWW.getUUID());

        if (player != null) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setGameMode(GameMode.SURVIVAL);
        }

        WorldBorder wb = world.getWorldBorder();

        int x = (int) (Math.round(wb.getSize() / 3 * Math.cos(d) + world.getSpawnLocation().getX()));
        int z = (int) (Math.round(wb.getSize() / 3 * Math.sin(d) + world.getSpawnLocation().getZ()));

        playerWW.addPotionModifier(PotionModifier.add(PotionEffectType.WITHER, 400, 0,NO_FALL));
        playerWW.teleport(new Location(world, x, world.getHighestBlockYAt(x, z) + 100, z));
    }

    @Override
    public World getWorld() {
        return this.world;
    }


    @Override
    public double getPercentageGenerated() {
        if (wft == null) return 0;

        return wft.getPercentageCompleted();
    }

    public void setLobbyWorld() {

        Main main = JavaPlugin.getPlugin(Main.class);

        World world = Bukkit.getWorlds().get(0);
        world.setWeatherDuration(0);
        world.setThundering(false);
        VersionUtils.getVersionUtils().setGameRuleValue(world, "doFireTick", false);
        VersionUtils.getVersionUtils().setGameRuleValue(world, "reducedDebugInfo", true);
        VersionUtils.getVersionUtils().setGameRuleValue(world, "naturalRegeneration", false);
        VersionUtils.getVersionUtils().setGameRuleValue(world, "keepInventory", true);
        VersionUtils.getVersionUtils().setGameRuleValue(world, "announceAdvancements", false);
        int x = world.getSpawnLocation().getBlockX();
        int z = world.getSpawnLocation().getBlockZ();
        world.getWorldBorder().reset();

        if (main.getConfig().getBoolean("default_lobby")) {

            world.setSpawnLocation(x, 151, z);

            for (int i = -16; i <= 16; i++) {

                for (int j = -16; j <= 16; j++) {

                    new Location(world, i + x, 150, j + z).getBlock().setType(Material.BARRIER);
                    new Location(world, i + x, 154, j + z).getBlock().setType(Material.BARRIER);
                }
                for (int j = 151; j < 154; j++) {
                    new Location(world, i + x, j, z - 16).getBlock().setType(Material.BARRIER);
                    new Location(world, i + x, j, z + 16).getBlock().setType(Material.BARRIER);
                    new Location(world, x - 16, j, i + z).getBlock().setType(Material.BARRIER);
                    new Location(world, x + 16, j, i + z).getBlock().setType(Material.BARRIER);
                }
            }
        }
    }
}
