package me.mncat77.slender2go;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Slender2Go extends JavaPlugin implements Listener {
    
    private World world;
    private net.minecraft.server.v1_5_R2.World NMSWorld;
    
    private FileConfiguration config;
    
    private boolean[] fields = new boolean[50];
    
    private Vector spawnVec;
    private Vector[] signsVec = new Vector[8];
    
    
    private boolean disableChat;
    private int fieldSpace;
    private int fieldWidth;
    private int fieldChunksX;
    private int fieldChunksZ;
    private int fieldCount;
    
    private String worldName;
    private String lossMessage;
    private String winMessage;
    private String startMessage;
    
    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();
        try {
            Class[] args = new Class[3];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = int.class;
            Method a = net.minecraft.server.v1_5_R2.EntityTypes.class.getDeclaredMethod("a", args);
            a.setAccessible(true);
            a.invoke(a, Slender.class, "Enderman", 58);
        }
        catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "[Slender2Go] Unable to add mob to NMS enum, disabling the plugin");
            this.setEnabled(false);
        }
        for(int i=0;i<8;i++) {
            signsVec[i] = new Vector(config.getInt("Map.pages." + (i+1) + ".x"), config.getInt("Map.pages." + (i+1) + ".y"), config.getInt("Map.pages." + (i+1) + ".z"));
        }
        spawnVec = new Vector(config.getInt("Map.spawn.x"), config.getInt("Map.spawn.y"), config.getInt("Map.spawn.z"));
        startMessage = config.getString("Messages.join");
        winMessage = config.getString("Messages.win");
        lossMessage = config.getString("Messages.loss");
        worldName = config.getString("Map.world");
        fieldWidth = config.getInt("Map.fieldSizeZ");
        fieldSpace = config.getInt("Map.fieldSpace");
        fieldChunksX = config.getInt("Map.fieldChunksX");
        fieldChunksZ = config.getInt("Map.fieldChunksZ");
        fieldCount = config.getInt("Map.fieldCount");
        fields = new boolean[fieldCount];
        for(int d=0;d<fieldCount;d++) {
            fields[d] = true;
        }
        disableChat = config.getBoolean("Misc.disableChat");
        getServer().getPluginManager().registerEvents(this, this);
        world = Bukkit.getWorld(worldName);
        NMSWorld = ((CraftWorld)world).getHandle();
        Logger logger = Bukkit.getLogger();
        logger.log(Level.INFO, "[Slender2Go] Taken control over time");
        if(disableChat) {
            logger.log(Level.INFO, "[Slender2Go] Taken control over chat");
        }
        world.setFullTime(0);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                world.setTime(17000);
            }
        }, 0, 2000);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                Player[] onlinePlayers = getServer().getOnlinePlayers();
                for(Player player : onlinePlayers) {
                    if(!player.isOp() && (player.getWorld() == world)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,9999,0));
                    }
                }
            }
        }, 0, 4500);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if(sender instanceof Player) {
            player = (Player)sender;
        }
        else {
            sender.sendMessage("Command must be used ingame!");
            return true;
        }
        if(player.getWorld() != world) {
            player.sendMessage(ChatColor.RED + "Invalid location, change world in config.");
        }
        if(args.length == 1) {
            if(!player.hasPermission("slender2go.setpage")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to set the page location.");
                return true;
            }
            int p;
            try {
                p = Integer.parseInt(args[0]);
            }
            catch(Exception e) {
                return false;
            }
            Block block = player.getTargetBlock(null, 10);
            if(block == null) {
                player.sendMessage(ChatColor.RED + "No block in range");
                return true;
            }
            else {
                config.set("Map.pages." + p + ".x", block.getX());
                config.set("Map.pages." + p + ".y", block.getY());
                config.set("Map.pages." + p + ".z", block.getZ());
                this.saveConfig();
                sender.sendMessage(ChatColor.YELLOW + "Page " + p + " succesfully placed.");
                return true;
            }
        }
        else {
            return false;
        }
    }
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if(event.getFrom() == world) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        else if(player.getWorld() == world) {
            startGame(player);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld() == world) {
            event.setJoinMessage(null);
            startGame(player);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(player.getWorld() == world) {
            event.setDeathMessage(null);
            player.setHealth(20);
            event.getEntity().kickPlayer(lossMessage);
        }
    }
    
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if(!event.getPlayer().isOp() && disableChat && (event.getPlayer().getWorld() == world))  {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void startGame(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(startMessage);
        int f = -1;
        for(int i=0;i<fieldCount;i++) {
            if(fields[i]) {
                f = i;
                break;
            }
        }
        fields[f] = false;
        int a = f*fieldChunksZ;
        for(int x=0;x<fieldChunksX;x++) {
            for(int z=0;z<fieldChunksZ;z++) {
                Entity[] entities = world.getChunkAt(x, z+a).getEntities();
                for(int e=0;e<entities.length;e++) {
                    entities[e].remove();
                }
            }
        }
        player.teleport(new Location(world, 0, 0, (fieldWidth+fieldSpace)*f).add(spawnVec));
        for(int i=0;i<8;i++) {
            try {
                Entity iF = world.spawnEntity(new Location(world, 0, 0, (fieldWidth+fieldSpace)*f).add(signsVec[i]), EntityType.ITEM_FRAME);
                ((ItemFrame)iF).setItem(new ItemStack(Material.MAP, 1, (short)i));
            }
            catch(Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Unable to place pages; config not matching with map, make sure the referenced Locations contain solid blocks!");
            }
        }
        player.setMetadata("field", new FixedMetadataValue(this, f));
        player.setMetadata("pages", new FixedMetadataValue(this, 0));
        PlayerInventory inv = player.getInventory();
        inv.clear();
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,9999,0));
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld() == world) {
            fields[player.getMetadata("field").get(0).asInt()] = true;
            event.setQuitMessage(null);
        }
    }
    
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if(event.getRemover().getWorld() == world) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if(entity.getWorld() != world) {
            return;
        }
        if(entity instanceof ItemFrame) {
            entity.remove();
            Player player = event.getPlayer();
            if(player.isOp()) {
                return;
            }
            int pages = player.getMetadata("pages").get(0).asInt();
            player.setMetadata("pages", new FixedMetadataValue(this, pages+1));
            player.sendMessage(ChatColor.GRAY + "Pages " + (pages+1)+"/8");
            if(pages == 0) {
                Location loc = player.getLocation();
                Vector dir = loc.getDirection();
                Vector vec = new Vector(-dir.getX(), 0, -dir.getZ()).normalize();
                Slender slender = new Slender(NMSWorld, player);
                Location add = loc.add(vec.multiply(20));
                slender.setPosition(add.getX(), add.getY(), add.getZ());
                NMSWorld.addEntity(slender, CreatureSpawnEvent.SpawnReason.CUSTOM);
                slender.getBukkitEntity().setMetadata("stalking", new FixedMetadataValue(this, player));
                ((CraftLivingEntity)slender.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,9999,5));
                player.damage(0);
                player.playSound(loc, Sound.DIG_WOOL, 1, 1);
                player.playSound(loc, Sound.ENDERMAN_STARE, 20, 1);
                return;
            }
            else if(pages == 7) {
                player.playSound(player.getLocation(), Sound.DIG_WOOL, 1, 1);
                player.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1, 1);
                player.kickPlayer(winMessage);
            }
            player.playSound(player.getLocation(), Sound.DIG_WOOL, 1, 1);
            player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1, 1);
        }
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if(event.getEntity().getWorld() == world) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if(event.getWorld() == world) {
            event.setCancelled(true);
        }
    }
}