package com.zones;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.zones.model.ZoneBase;
import com.zones.model.ZoneForm;
import com.zones.model.types.ZoneInherit;

/**
 * 
 * @author Meaglin
 *
 */
public class Region {

    private ArrayList<ZoneBase> _zones;
    private int                 x, y;

    public Region(int x, int y) {
        this.x = x;
        this.y = y;
        _zones = new ArrayList<ZoneBase>();
    }

    public void addZone(ZoneBase zone) {
        if (_zones.contains(zone))
            return;

        
        for(ZoneBase b : _zones) {
            if(b instanceof ZoneInherit && !((ZoneInherit)b).containsInherited(zone) && zone.getForm().contains(b.getForm())) {
                ((ZoneInherit)b).addInherited(zone);
                if(zone instanceof ZoneInherit && !((ZoneInherit)zone).containsSub(b))
                    ((ZoneInherit)zone).addSub(b);
            } 
            if(zone instanceof ZoneInherit && !((ZoneInherit)zone).containsInherited(b) && b.getForm().contains(zone.getForm())) {
                ((ZoneInherit)zone).addInherited(b);
                if(b instanceof ZoneInherit && !((ZoneInherit)b).containsSub(zone))
                    ((ZoneInherit)b).addSub(zone);
            }
        }
        _zones.add(zone);
    }

    public void removeZone(ZoneBase zone) {
        _zones.remove(zone);
        for(ZoneBase b : _zones) {
            if(b instanceof ZoneInherit) {
                ((ZoneInherit)b).removeInherited(zone);
                ((ZoneInherit)b).removeSub(zone);
            }
        }
    }

    public ArrayList<ZoneBase> getZones() {
        return _zones;
    }

    public ZoneBase getActiveZone(Player player)                             {return getActiveZone(player.getLocation());}
    public ZoneBase getActiveZone(Location loc)                              {return getActiveZone(loc.getX(), loc.getZ(), loc.getY());}
    public ZoneBase getActiveZone(double x, double y, double z)              {return getActiveZone(WorldManager.toInt(x), WorldManager.toInt(y), WorldManager.toInt(z));}

    public ArrayList<ZoneBase> getActiveZones(Player player)                 {return getActiveZones(player.getLocation());}
    public ArrayList<ZoneBase> getActiveZones(Location loc)                  {return getActiveZones(loc.getX(), loc.getZ(), loc.getY());}
    public ArrayList<ZoneBase> getActiveZones(double x, double y, double z)  {return getActiveZones(WorldManager.toInt(x), WorldManager.toInt(y), WorldManager.toInt(z));}

    public ZoneBase getActiveZone(int x, int y, int z) {
        ZoneBase primary = null;

        for (ZoneBase zone : getZones())
            if (zone.isInsideZone(x, y, z) && (primary == null || primary.getForm().getSize() > zone.getForm().getSize()))
                primary = zone;

        return primary;
    }
    
    public ArrayList<ZoneBase> getActiveZones(int x, int y, int z) {
        ArrayList<ZoneBase> zones = new ArrayList<ZoneBase>();

        for (ZoneBase zone : getZones())
            if (zone.isInsideZone(x, y, z))
                zones.add(zone);

        return zones;
    }

    public ArrayList<ZoneBase> getAdminZones(Player player) {
        ArrayList<ZoneBase> zones = new ArrayList<ZoneBase>();

        for (ZoneBase zone : getZones())
            if (zone.isInsideZone(player) && zone.canAdministrate(player))
                zones.add(zone);

        return zones;
    }

    public ArrayList<ZoneBase> getAdminZones(Player player, Location loc) {
        ArrayList<ZoneBase> zones = new ArrayList<ZoneBase>();

        for (ZoneBase zone : getZones())
            if (zone.isInsideZone(loc) && zone.canAdministrate(player))
                zones.add(zone);

        return zones;
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public void revalidateZones(Player player) {
        for (ZoneBase z : getZones()) {
            if (z != null)
                z.revalidateInZone(player);
        }
    }
    
    public void revalidateZones(Player player, Location loc) {
        for (ZoneBase z : getZones()) {
            if (z != null)
                z.revalidateInZone(player, loc);
        }
    }

    public void revalidateOutZones(Player player, Location from) {
        for (ZoneBase z : getZones()) {
            if (z != null)
                z.removePlayer(player);
        }
    }
    
    public boolean contains(ZoneBase zone) {
        ZoneForm form = zone.getForm();
        int ax = getX() << WorldManager.SHIFT_SIZE;
        int bx = (getX() + 1) << WorldManager.SHIFT_SIZE;
        int ay = getY() << WorldManager.SHIFT_SIZE;
        int by = (getY() + 1) << WorldManager.SHIFT_SIZE;
        return form.intersectsRectangle(ax, bx, ay, by);
    }
    
    @Override
    public boolean equals(Object object) {
        if(!(object instanceof Region))
            return false;
        
        Region r = (Region)object;
        
        return (r.getX() == getX() && r.getY() == getY());
    }

    @Override
    public int hashCode() {
        return getX() ^ getY();
    }
}
