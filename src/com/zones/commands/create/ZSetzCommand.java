package com.zones.commands.create;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.zones.Zones;
import com.zones.ZonesDummyZone;
import com.zones.commands.ZoneCommand;

public class ZSetzCommand extends ZoneCommand {
    
    public ZSetzCommand(Zones plugin) {
        super("zsetz", plugin);
        this.setRequiresDummy(true);
    }

    @Override
    public boolean run(Player player, String[] vars) {
        ZonesDummyZone dummy = getDummy(player);
        if (vars.length < 2 || Integer.parseInt(vars[0]) < 0 || Integer.parseInt(vars[0]) > 130 || Integer.parseInt(vars[1]) < 0 || Integer.parseInt(vars[1]) > 130) {
            player.sendMessage(ChatColor.YELLOW.toString() + "Usage: /zsetz [min Z] [max Z]");
        } else {
            dummy.setZ(Integer.parseInt(vars[0]),Integer.parseInt(vars[1]) );
            player.sendMessage(ChatColor.GREEN.toString() + "Min z and Max z now changed to : " + dummy.getMin() + " and " + dummy.getMax());
        }
        return true;
    }
}
