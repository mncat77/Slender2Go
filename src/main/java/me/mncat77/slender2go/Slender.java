package me.mncat77.slender2go;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityEnderman;
import net.minecraft.server.v1_6_R3.EntityHuman;
import net.minecraft.server.v1_6_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Slender extends EntityEnderman {
    
    private EntityHuman stalking;
    private Player stalkingB;
    private boolean s;
    
    public Slender(World world) {
        super(world);
        this.s = false;
    }
    
    public Slender(World world, Player player) {
        super(world);
        this.s = true;
        stalking = (EntityHuman)((CraftPlayer)player).getHandle();
        stalkingB = player;
    }
    
    @Override
    protected Entity findTarget() {
        if(s) {
            return stalking;
        }
        return super.findTarget();
    }
    
    @Override
    public void c() {
        super.c();
        if(s) {
            try {
                Method method = EntityEnderman.class.getDeclaredMethod("f", EntityHuman.class);
                method.setAccessible(true);
                boolean sees = Boolean.valueOf(method.invoke((EntityEnderman)this, stalking).toString());
                if(sees) {
                    stalkingB.damage(6.0);
                }
            }
            catch(Exception ex) {
                Logger.getLogger(Slender.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
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
    }
    
    @Override
    protected boolean j(double d0, double d1, double d2) {
        if(s) {
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
        return super.j(d0, d1, d2);
    }

}