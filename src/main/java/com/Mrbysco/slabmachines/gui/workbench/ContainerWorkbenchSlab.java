package com.mrbysco.slabmachines.gui.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerWorkbenchSlab extends ContainerWorkbench{
    private final BlockPos pos;
    
	public ContainerWorkbenchSlab(InventoryPlayer playerInventory, World worldIn, BlockPos posIn) {
		super(playerInventory, worldIn, posIn);
		this.pos = posIn;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}
}
