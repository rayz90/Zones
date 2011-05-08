package com.zones.model;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class RevertBlock {
    
    private final int oldType,oldData;
    private final BlockState oldState;
    private final Block block;
    
    public RevertBlock(Block block) {
        this.block = block;
        oldState = block.getState();
        oldData = block.getData();
        oldType = block.getTypeId();
    }
    
    public void revert() {
        boolean unloadChunk = false;
        if(!block.getWorld().isChunkLoaded(block.getChunk())) {
            block.getWorld().loadChunk(block.getChunk());
            unloadChunk = true;
        }
        
        block.setTypeId(oldType);
        if(oldData != 0) block.setData((byte)oldData);
        
        
        if(oldState != null) {
            oldState.update(true);
        }
        if(unloadChunk) {
            block.getWorld().unloadChunk(block.getChunk().getX(),block.getChunk().getZ());
        }
    }
    
    public Block getBlock() { return block; }
    
    public boolean equals(Object o) {
        if(o instanceof RevertBlock) {
            RevertBlock rb = (RevertBlock) o;
            if(rb.getBlock().getX() == block.getX() && rb.getBlock().getY() == block.getY() && rb.getBlock().getZ() == block.getZ())
                return true;
        }
        return false;
    }
}