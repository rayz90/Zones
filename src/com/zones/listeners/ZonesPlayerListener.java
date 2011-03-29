package com.zones.listeners;

import java.util.Arrays;
import java.util.List;

import com.zones.World;
import com.zones.ZoneManager;
import com.zones.ZoneBase;
import com.zones.Zones;
import com.zones.ZonesAccess;
import com.zones.ZonesCommandsHandler;
import com.zones.ZonesConfig;
import com.zones.ZonesDummyZone;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * 
 * @author Meaglin
 */
public class ZonesPlayerListener extends PlayerListener {

    private Zones plugin;

    public ZonesPlayerListener(Zones plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player joins a server
     * 
     * @param event
     *            Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerEvent event) {
        World.getInstance().getRegion(event.getPlayer()).revalidateZones(event.getPlayer());
    }

    /**
     * Called when a player leaves a server
     * 
     * @param event
     *            Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerEvent event) {
        Player player = event.getPlayer();
        ZonesDummyZone dummy = ZoneManager.getInstance().getDummy(player.getName());
        if (dummy != null) {
            dummy.setConfirm("stop");
            dummy.confirm(player);
        }
    }

    /**
     * Called when a player attempts to move location in a world
     * 
     * @param event
     *            Relevant event details
     */
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        //do nothing when we don't actually change from 1 block to another.
        if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
            return;
        
        ZoneBase aZone = World.getInstance().getActiveZone(from);
        ZoneBase bZone = World.getInstance().getActiveZone(to);
        if (bZone != null && ((aZone != null && aZone.getId() != bZone.getId() && !bZone.canModify(player, ZonesAccess.Rights.ENTER)) || (aZone == null && !bZone.canModify(player, ZonesAccess.Rights.ENTER)))) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED.toString() + "You can't enter " + bZone.getName() + ".");
            // we don't have to do overall revalidation if the player gets
            // warped back to his previous location.
            return;
        }
        if (bZone != null && !bZone.canModify(player, ZonesAccess.Rights.ENTER)) {
            event.setFrom(player.getWorld().getSpawnLocation());
            event.setCancelled(true);
            //player.teleportTo(player.getWorld().getSpawnLocation());
            player.sendMessage(ChatColor.RED.toString() + "You were moved to spawn because you were in an illigal position.");
        }

        World.getInstance().revalidateZones(player, from, to);
    }

    /**
     * Called when a player attempts to teleport to a new location in a world
     * 
     * @param event
     *            Relevant event details
     */
    @Override
    public void onPlayerTeleport(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        ZoneBase aZone = World.getInstance().getActiveZone(from);
        ZoneBase bZone = World.getInstance().getActiveZone(to);
        if (bZone != null && ((aZone != null && aZone.getId() != bZone.getId() && !bZone.canModify(player, ZonesAccess.Rights.ENTER)) || (aZone == null && !bZone.canModify(player, ZonesAccess.Rights.ENTER)))) {
            player.sendMessage(ChatColor.RED.toString() + "You cannot warp into " + bZone.getName() + ", since it is a protected area.");
            event.setCancelled(true);
        }

        World.getInstance().revalidateZones(player, from, to);

    }

    /**
     * Called when a player uses an item
     * 
     * @param event
     *            Relevant event details
     */
    private static List<Integer> items = Arrays.asList(
            Material.FLINT_AND_STEEL.getId(),
            Material.BUCKET.getId(),
            Material.LAVA_BUCKET.getId(),
            Material.WATER_BUCKET.getId(),
            Material.MINECART.getId(),
            Material.BOAT.getId(),
            Material.SEEDS.getId(),
            Material.SIGN.getId(),
            Material.REDSTONE.getId()
            );
    private static List<Integer> blocks = Arrays.asList(
            Material.NOTE_BLOCK.getId()
            );
    private static List<Integer> containers = Arrays.asList(
            Material.FURNACE.getId(),
            Material.BURNING_FURNACE.getId(),
            Material.CHEST.getId(),
            Material.DISPENSER.getId()
            );
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        switch(event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                Block blockPlaced = event.getClickedBlock().getRelative(event.getBlockFace());
                ZoneBase zone = World.getInstance().getActiveZone(blockPlaced.getLocation());
                int type = event.getItem().getTypeId();
                Player player = event.getPlayer();
                if (items.contains(type)){
                    if (zone != null && !zone.canModify(player, ZonesAccess.Rights.BUILD)) {
                        player.sendMessage(ChatColor.RED.toString() + "You cannot place blocks in '" + zone.getName() + "' !");
                        event.setCancelled(true);
                    } else if (!ZonesConfig.LIMIT_BY_BUILD_ENABLED || plugin.getP().permission(player, "zones.build") || (zone != null && zone.canModify(player, ZonesAccess.Rights.BUILD)))
                        return;
                    else
                        event.setCancelled(true);
                } else if(containers.contains(type) || type == Material.NOTE_BLOCK.getId()) {
                    if(zone != null && !zone.canModify(player, ZonesAccess.Rights.MODIFY)) {
                        if (type == Material.CHEST.getId())
                            player.sendMessage(ChatColor.RED.toString() + "You cannot change chests in '" + zone.getName() + "' !");
                        else if (type == Material.FURNACE.getId() || type == Material.BURNING_FURNACE.getId())
                            player.sendMessage(ChatColor.RED.toString() + "You cannot change furnaces in '" + zone.getName() + "' !");
                        else if (type == Material.DISPENSER.getId())
                            player.sendMessage(ChatColor.RED.toString() + "You cannot change dispensers in '" + zone.getName() + "' !");
                            
                        event.setCancelled(true);
                    } else if (zone != null && type == Material.NOTE_BLOCK.getId() && !zone.canModify(player, ZonesAccess.Rights.HIT)) {
                        
                        player.sendMessage(ChatColor.RED.toString() + "You cannot change Note blocks in '" + zone.getName() + "' !");

                        event.setCancelled(true);
                    } else if (!ZonesConfig.LIMIT_BY_BUILD_ENABLED || plugin.getP().permission(player, "zones.build") || (zone != null && zone.canModify(player, ZonesAccess.Rights.MODIFY)))
                        return;
                    else {

                        if (type == Material.CHEST.getId())
                            player.sendMessage(ChatColor.RED.toString() + "You cannot change chests in the world !");
                        else if (type == Material.FURNACE.getId() || type == Material.BURNING_FURNACE.getId())
                            player.sendMessage(ChatColor.RED.toString() + "You cannot change furnaces in the world !");
                        else if (type == Material.DISPENSER.getId())
                            player.sendMessage(ChatColor.RED.toString() + "You cannot change dispensers in the world !");
                        
                        event.setCancelled(true);
                    }
                }
                break;
            case LEFT_CLICK_BLOCK:
                
                break;
        }
        
    }

    /**
     * 
     * 
     * FUCK U BUKKIT I WILL JUST USE THIS INSTEAD OF THE OVER COMPLICATED CRAP SYSTEM
     * K THX BAI.
     * 
     * 
     */
    @Override
    public void onPlayerCommandPreprocess(PlayerChatEvent event) {
        if(ZonesCommandsHandler.onCommand(plugin, event.getPlayer(), event.getMessage().split(" ")))
            event.setCancelled(true);
    }

    /**
     * Called when a player gets kicked from the server
     * 
     * @param event Relevant event details
     */
    public void onPlayerKick(PlayerKickEvent event) {
    }

    /**
     * Called when a player sends a chat message
     *
     * @param event Relevant event details
     */
    public void onPlayerChat(PlayerChatEvent event) {
    }

    /**
     * Called when a player respawns
     * 
     * @param event Relevant event details
     */
    public void onPlayerRespawn(PlayerRespawnEvent event) {
    }

    /**
     * Called when a player attempts to log in to the server
     *
     * @param event Relevant event details
     */
    public void onPlayerLogin(PlayerLoginEvent event) {
    }

    /**
     * Called when a player throws an egg and it might hatch
     *
     * @param event Relevant event details
     */
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
    }

    /**
     * Called when a player plays an animation, such as an arm swing
     *
     * @param event Relevant event details
     */
    public void onPlayerAnimation(PlayerAnimationEvent event) {
    }

    /**
     * Called when a player opens an inventory
     *
     * @param event Relevant event details
     */
    public void onInventoryOpen(PlayerInventoryEvent event) {
    }

    /**
     * Called when a player changes their held item
     *
     * @param event Relevant event details
     */
    public void onItemHeldChange(PlayerItemHeldEvent event) {
    }

    /**
     * Called when a player drops an item from their inventory
     *
     * @param event Relevant event details
     */
    public void onPlayerDropItem(PlayerDropItemEvent event) {
    }

    /**
     * Called when a player picks an item up off the ground
     *
     * @param event Relevant event details
     */
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    }

    /**
     * Called when a player toggles sneak mode
     *
     * @param event Relevant event details
     */
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    }

    /**
     * Called when a player fills a bucket
     * 
     * @param event Relevant event details
     */
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
    }

    /**
     * Called when a player empties a bucket
     * 
     * @param event Relevant event details
     */
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    }

}