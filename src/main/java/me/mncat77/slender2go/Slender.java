package me.mncat77.slender2go;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_5_R2.Entity;
import net.minecraft.server.v1_5_R2.EntityEnderman;
import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Slender extends EntityEnderman {
    
    private EntityHuman stalking;
    private Player stalkingB;
    
    public Slender(World world, Player player) {
        super(world);
        stalking = (EntityHuman)((CraftPlayer)player).getHandle();
        stalkingB = player;
    }
    
    @Override
    protected Entity findTarget() {
        return stalking;
    }
    
    @Override
    public void c() {
        try {
            Method method = EntityEnderman.class.getDeclaredMethod("e", EntityHuman.class);
            method.setAccessible(true);
            boolean sees = Boolean.valueOf(method.invoke((EntityEnderman)this, stalking).toString());
            if(sees) {
                stalkingB.damage(6);
            }
        }
        catch(Exception ex) {
            Logger.getLogger(Slender.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.c();
        
        Location oldLoc = new Location(world.getWorld(), lastX, lastY, lastZ);
        Location newLoc = new Location(world.getWorld(), locX, locY, locZ);
        Location toLoc = oldLoc.add(newLoc.subtract(oldLoc).toVector().normalize().multiply(0.1F));
        locX = toLoc.getX();
        locY = toLoc.getY();
        locZ = toLoc.getZ();
        setCarriedId(0);
        setHealth(40);
        if(new Location(world.getWorld(), locX, locY, locZ).distance(stalkingB.getLocation())<3D) {
            stalkingB.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,60,2));
        }
    }
    
    @Override
    protected boolean j(double d0, double d1, double d2) {
        Location eLoc = new Location(world.getWorld(), locX, locY, locZ);
        Location loc = stalkingB.getLocation();
        if((eLoc.distance(loc)) < 20D) {
            return false;
        }
        Vector dir = loc.getDirection();
        Vector vec = new Vector(-dir.getX(), 0, -dir.getZ());
        Location tLoc = loc.add(vec.multiply(20));
        locX = tLoc.getX();
        locY = tLoc.getY();
        locZ = tLoc.getZ();
        return true;
    }
    
}