package com.zones.model.types.normal;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.zones.ZonesConfig;
import com.zones.accessresolver.interfaces.PlayerLocationResolver;
import com.zones.model.ZoneBase;
import com.zones.model.ZonesAccess.Rights;
import com.zones.model.types.ZoneNormal;
import com.zones.util.Log;

public class NormalPlayerEnterResolver implements PlayerLocationResolver {

    @Override
    public void sendDeniedMessage(ZoneBase zone, Player player) {
        zone.sendMarkupMessage(ZonesConfig.PLAYER_CANT_ENTER_INTO_ZONE, player);
    }

    @Override
    public boolean isAllowed(ZoneBase zone, Player player, Location from, Location to) {
        Log.info(player, "trigger enter '" + zone.getName() + "'[" + zone.getId() + "] (" + from.getX() + "," + from.getY() + "," + from.getZ() + ") " + "(" + to.getX() + "," + to.getY() + "," + to.getZ() + ")" );

        return ((ZoneNormal)zone).canModify(player, Rights.ENTER);
    }

}
