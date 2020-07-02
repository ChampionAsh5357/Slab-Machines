package com.mrbysco.slabmachines.tileentity.compat.tinkers;

import com.mrbysco.slabmachines.blocks.compat.tinkers.BlockToolStationSlab;
import com.mrbysco.slabmachines.gui.compat.tcon.ContainerToolStationSlab;
import com.mrbysco.slabmachines.gui.compat.tcon.GuiToolStationSlab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.shared.block.PropertyTableItem;
import slimeknights.tconstruct.tools.common.client.GuiButtonRepair;
import slimeknights.tconstruct.tools.common.tileentity.TileToolStation;

public class TileToolStationSlab extends TileToolStation{

	@Override
	public Container createContainer(InventoryPlayer inventoryplayer, World world, BlockPos pos) {
		return new ContainerToolStationSlab(inventoryplayer, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiContainer createGui(InventoryPlayer inventoryplayer, World world, BlockPos pos) {
	    return new GuiToolStationSlab(inventoryplayer, world, pos, this);
	}
	
	@Override
	public IExtendedBlockState writeExtendedBlockState(IExtendedBlockState state) {
	    state = setInventoryDisplay(state);
	    
		return state;
	}
	
	@Override
	protected IExtendedBlockState setInventoryDisplay(IExtendedBlockState state) {
		PropertyTableItem.TableItems toDisplay = new PropertyTableItem.TableItems();

	    ToolBuildGuiInfo info = GuiButtonRepair.info;
	    float s = 0.46875f;

	    for(int i = 0; i < info.positions.size(); i++) {
	    	ItemStack stackInSlot = getStackInSlot(i);
		    PropertyTableItem.TableItem item = getTableItem(stackInSlot, this.getWorld(), null);
		    if(item != null) {
		    	item.x = (33 - info.positions.get(i).getX()) / 61f;
			    item.z = (42 - info.positions.get(i).getY()) / 61f;
			    item.s *= s;
	
				if(i == 0 || info != GuiButtonRepair.info) {
					item.s *= 1.3f;
				}
				
				  // correct itemblock because scaling
				if(stackInSlot.getItem() instanceof ItemBlock && !(Block.getBlockFromItem(stackInSlot.getItem())  instanceof BlockPane)) {
					item.y = -(1f - item.s) / 2f;
				}

				//item.s *= 2/5f;
				toDisplay.items.add(item);
		    }
	    }

	    // add inventory if needed
	    return state.withProperty(BlockToolStationSlab.INVENTORY, toDisplay);
	}

	@Override public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) { return oldState.getBlock() != newState.getBlock(); }
}
