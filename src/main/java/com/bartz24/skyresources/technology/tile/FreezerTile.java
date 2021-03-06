package com.bartz24.skyresources.technology.tile;

import com.bartz24.skyresources.RandomHelper;
import com.bartz24.skyresources.registry.ModBlocks;
import com.bartz24.skyresources.technology.block.BlockFreezer;
import com.bartz24.skyresources.technology.block.BlockMiniFreezer;
import com.bartz24.skyresources.technology.freezer.FreezerRecipe;
import com.bartz24.skyresources.technology.freezer.FreezerRecipes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class FreezerTile extends TileEntity implements IInventory, ITickable
{
	float[] timeFreeze;
	private ItemStack[] inventory;

	public float getFreezerSpeed()
	{
		if (worldObj.getBlockState(pos).getBlock() == ModBlocks.miniFreezer)
			return 0.25f;
		else if (worldObj.getBlockState(pos).getBlock() == ModBlocks.ironFreezer)
			return 1f;

		return 1;

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);

		NBTTagList list = new NBTTagList();
		if (inventory != null)
		{
			for (int i = 0; i < inventory.length; ++i)
			{
				if (this.getStackInSlot(i) != null)
				{
					NBTTagCompound stackTag = new NBTTagCompound();
					stackTag.setByte("Slot", (byte) i);
					this.getStackInSlot(i).writeToNBT(stackTag);
					list.appendTag(stackTag);
				} else
				{
					NBTTagCompound stackTag = new NBTTagCompound();
					stackTag.setByte("Slot", (byte) i);
					list.appendTag(stackTag);
				}
			}
			compound.setTag("Items", list);
		}

		if (timeFreeze != null)
		{
			for (int i = 0; i<timeFreeze.length;i++)
				compound.setFloat("time" + i, timeFreeze[i]);
		}
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		NBTTagList list = compound.getTagList("Items", 10);
		if(list.tagCount() > 0)
		inventory = new ItemStack[list.tagCount()];
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);
			int slot = stackTag.getByte("Slot") & 255;
			System.out.println(stackTag.hasKey("id"));
			if (stackTag.hasKey("id"))
				this.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(stackTag));
		}

		if (inventory != null)
		{
			timeFreeze = new float[inventory.length];
			for (int i = 0; i < this.inventory.length; i++)
			{
				timeFreeze[i] = compound.getFloat("time" + i);
			}
		}
	}

	@Override
	public void update()
	{
		if (inventory == null)
		{
			inventory = new ItemStack[this.getSizeInventory()];
			return;
		}
		else  if(timeFreeze == null)
		{
			timeFreeze = new float[this.getSizeInventory()];
			return;
		}
		updateMulti2x1();
		if (!hasValidMulti())
			return;

		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			FreezerRecipe recipe = recipeToCraft(i);

			if (recipe != null)
			{
				if (timeFreeze[i] >= getTimeReq(recipe, this.inventory[i]))
				{
					for (int amt = 0; amt < getGroupsFreezing(recipe, this.inventory[i]); amt++)
					{
						ejectResultSlot(recipe.getOutput().copy());
					}
					this.inventory[i].stackSize -= recipe.getInput().stackSize
							* getGroupsFreezing(recipe, this.inventory[i]);
					if (this.inventory[i].stackSize <= 0)
						this.inventory[i] = null;
					timeFreeze[i] = 0;
				} else
					timeFreeze[i] += getFreezerSpeed();
			} else
				timeFreeze[i] = 0;

		}
	}

	int getGroupsFreezing(FreezerRecipe recipe, ItemStack input)
	{
		return (int) Math.floor((float) input.stackSize / (float) recipe.getInput().stackSize);
	}

	public int getTimeReq(FreezerRecipe recipe, ItemStack input)
	{
		return recipe.getTimeReq() * getGroupsFreezing(recipe, input);
	}

	void ejectResultSlot(ItemStack output)
	{
		if (!worldObj.isRemote)
		{
			BlockPos[] checkPoses = new BlockPos[]
			{ getPos().north(), getPos().south(), getPos().west(), getPos().east(), getPos().up(),
					getPos().down() };

			EnumFacing facing = worldObj.getBlockState(pos).getValue(BlockFreezer.FACING);

			BlockPos facingPos = getPos().add(facing.getDirectionVec());

			TileEntity tile = worldObj.getTileEntity(facingPos);
			if (tile instanceof IInventory)
			{
				output = RandomHelper.fillInventory((IInventory) tile, output);
			}

			for (BlockPos pos : checkPoses)
			{
				TileEntity tile2 = worldObj.getTileEntity(pos);
				if (pos.equals(facingPos) || tile2 instanceof FreezerTile)
					continue;
				if (tile2 instanceof IInventory)
				{
					output = RandomHelper.fillInventory((IInventory) tile2, output);
				}

			}

			if (output != null && output.stackSize > 0)
				RandomHelper.spawnItemInWorld(worldObj, output, facingPos);
		}
	}

	void updateMulti2x1()
	{
		IBlockState state = this.worldObj.getBlockState(pos);
		IBlockState stateUp = this.worldObj.getBlockState(pos.up());
		IBlockState stateDown = this.worldObj.getBlockState(pos.down());
		if (state.getBlock() instanceof BlockFreezer)
		{
			if (stateUp.getBlock() instanceof BlockFreezer
					&& state.getProperties()
							.get(BlockFreezer.PART) == BlockFreezer.EnumPartType.BOTTOM
					&& stateUp.getProperties()
							.get(BlockFreezer.PART) == BlockFreezer.EnumPartType.BOTTOM)
			{
				worldObj.setBlockState(pos.up(),
						stateUp.withProperty(BlockFreezer.PART, BlockFreezer.EnumPartType.TOP));
			} else if (!(stateDown.getBlock() instanceof BlockFreezer) && state.getProperties()
					.get(BlockFreezer.PART) == BlockFreezer.EnumPartType.TOP)
			{
				worldObj.setBlockState(pos,
						state.withProperty(BlockFreezer.PART, BlockFreezer.EnumPartType.BOTTOM));
			}
		}
	}

	public boolean hasValidMulti()
	{
		IBlockState state = this.worldObj.getBlockState(pos);

		if (state.getBlock() instanceof BlockMiniFreezer)
			return true;
		else if (state.getBlock() instanceof BlockFreezer)
			return validMulti2x1();
		return false;
	}

	boolean validMulti2x1()
	{
		IBlockState state = this.worldObj.getBlockState(pos);
		IBlockState stateUp = this.worldObj.getBlockState(pos.up());

		if (!(state.getBlock() instanceof BlockFreezer))
			return false;

		if (!(stateUp.getBlock() instanceof BlockFreezer))
			return false;

		if (state.getProperties().get(BlockFreezer.FACING) != stateUp.getProperties()
				.get(BlockFreezer.FACING))
			return false;

		if (state.getProperties().get(BlockFreezer.PART) != BlockFreezer.EnumPartType.BOTTOM
				|| stateUp.getProperties().get(BlockFreezer.PART) != BlockFreezer.EnumPartType.TOP)
			return false;
		return true;
	}

	public FreezerRecipe recipeToCraft(int slot)
	{
		if (slot >= inventory.length)
			return null;
		FreezerRecipe recipe = FreezerRecipes.getRecipe(inventory[slot]);

		return recipe;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		IBlockState state = this.worldObj.getBlockState(pos);

		FreezerTile tile = this;

		if (worldObj.getBlockState(pos).getBlock() instanceof BlockFreezer
				&& state.getProperties().get(BlockFreezer.PART) == BlockFreezer.EnumPartType.TOP)
			tile = (FreezerTile) worldObj.getTileEntity(pos.down());

		if (tile.getInv() == null || index < 0 || index >= this.getSizeInventory())
			return null;
		return tile.getInv()[index];
	}

	@Override
	public String getName()
	{
		return "container.skyresources.freezer";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation(this.getName());
	}

	@Override
	public int getSizeInventory()
	{
		if (worldObj.getBlockState(pos).getBlock() == ModBlocks.miniFreezer)
			return 1;
		else if (worldObj.getBlockState(pos).getBlock() == ModBlocks.ironFreezer)
			return 3;

		return 0;
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		if (this.getStackInSlot(index) != null)
		{
			ItemStack itemstack;

			if (this.getStackInSlot(index).stackSize <= count)
			{
				itemstack = this.getStackInSlot(index);
				this.setInventorySlotContents(index, null);
				this.markDirty();
				return itemstack;
			} else
			{
				itemstack = this.getStackInSlot(index).splitStack(count);

				if (this.getStackInSlot(index).stackSize <= 0)
				{
					this.setInventorySlotContents(index, null);
				} else
				{
					this.setInventorySlotContents(index, this.getStackInSlot(index));
				}

				this.markDirty();
				return itemstack;
			}
		} else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		IBlockState state = null;
		if (this.worldObj != null && this.pos != null)
			state = this.worldObj.getBlockState(pos);

		FreezerTile tile = this;
		if (tile.inventory != null)
		{

			if (state != null && state.getBlock() instanceof BlockFreezer && state.getProperties()
					.get(BlockFreezer.PART) == BlockFreezer.EnumPartType.TOP)
				tile = (FreezerTile) worldObj.getTileEntity(pos.down());

			if (index < 0 || index >= tile.inventory.length)
				return;

			if (stack != null && stack.stackSize > this.getInventoryStackLimit())
				stack.stackSize = this.getInventoryStackLimit();

			if (stack != null && stack.stackSize == 0)
				stack = null;

			tile.inventory[index] = stack;
			tile.markDirty();
		}

	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return this.worldObj.getTileEntity(this.getPos()) == this
				&& player.getDistanceSq(this.pos.add(0.5, 0.5, 0.5)) <= 64;
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		IBlockState state = this.worldObj.getBlockState(pos);

		if (worldObj.getBlockState(pos).getBlock() instanceof BlockFreezer
				&& state.getProperties().get(BlockFreezer.PART) == BlockFreezer.EnumPartType.TOP)
			return worldObj.getTileEntity(pos.down()) instanceof FreezerTile
					&& ((FreezerTile) worldObj.getTileEntity(pos.down())).isItemValidForSlot(index,
							stack);

		return true;
	}

	@Override
	public int getField(int id)
	{
		if (timeFreeze == null || id >= timeFreeze.length)
			return 0;
		return (int) timeFreeze[id];
	}

	@Override
	public void setField(int id, int value)
	{
		if (timeFreeze == null || id >= timeFreeze.length)
			return;
		timeFreeze[id] = value;
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < this.getSizeInventory(); i++)
			this.setInventorySlotContents(i, null);
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStackHelper.getAndRemove(this.inventory, index);
	}

	public ItemStack[] getInv()
	{
		return inventory;
	}
}
