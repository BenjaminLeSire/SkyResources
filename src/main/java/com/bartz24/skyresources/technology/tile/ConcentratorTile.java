package com.bartz24.skyresources.technology.tile;

import java.util.ArrayList;
import java.util.List;

import com.bartz24.skyresources.config.ConfigOptions;
import com.bartz24.skyresources.technology.concentrator.ConcentratorRecipe;
import com.bartz24.skyresources.technology.concentrator.ConcentratorRecipes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class ConcentratorTile extends TileEntity
		implements IInventory, ITickable
{
	int timeCondense;
	private ItemStack[] inventory = new ItemStack[1];

	public int currentHeatValue;

	private int fuelBurnTime;
	private int heatPerTick;

	private int currentItemBurnTime;

	public int getMaxHeat()
	{
		return 1538;
	}

	public int getMaxHeatPerTick()
	{
		return 16;
	}

	public int getHeatPerTick(ItemStack stack)
	{
		int fuelTime = TileEntityFurnace.getItemBurnTime(stack);
		if (fuelTime > 0)
		{
			return (int) Math.cbrt(
					(float) fuelTime * ConfigOptions.combustionHeatMultiplier * 0.5F);
		}

		return 0;
	}

	public int getFuelBurnTime(ItemStack stack)
	{
		if ((float) getHeatPerTick(stack) <= 0)
			return 0;

		return (int) ((float) Math.pow(TileEntityFurnace.getItemBurnTime(stack),
				0.75F) / (getHeatPerTick(stack) * 2));
	}

	public boolean isValidFuel(ItemStack stack)
	{
		if (TileEntityFurnace.getItemBurnTime(stack) <= 0
				|| getHeatPerTick(stack) <= 0
				|| getHeatPerTick(stack) > getMaxHeatPerTick()
				|| getFuelBurnTime(stack) <= 0)
			return false;

		return true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);

		compound.setInteger("heat", currentHeatValue);
		compound.setInteger("fuel", fuelBurnTime);
		compound.setInteger("item", currentItemBurnTime);
		compound.setInteger("hpt", heatPerTick);
		compound.setInteger("time", timeCondense);

		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			if (this.getStackInSlot(i) != null)
			{
				NBTTagCompound stackTag = new NBTTagCompound();
				stackTag.setByte("Slot", (byte) i);
				this.getStackInSlot(i).writeToNBT(stackTag);
				list.appendTag(stackTag);
			}
		}
		compound.setTag("Items", list);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		currentHeatValue = compound.getInteger("heat");
		fuelBurnTime = compound.getInteger("fuel");
		currentItemBurnTime = compound.getInteger("item");
		heatPerTick = compound.getInteger("hpt");
		timeCondense = compound.getInteger("time");

		NBTTagList list = compound.getTagList("Items", 10);
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);
			int slot = stackTag.getByte("Slot") & 255;
			this.setInventorySlotContents(slot,
					ItemStack.loadItemStackFromNBT(stackTag));
		}
	}

	@Override
	public void update()
	{
		craftItem();

		if (fuelBurnTime > 0)
		{
			this.fuelBurnTime--;
		} else
			this.currentItemBurnTime = this.heatPerTick = this.fuelBurnTime = 0;

		if (!this.worldObj.isRemote)
		{
			if (fuelBurnTime > 0 || this.inventory[0] != null)
			{
				if (fuelBurnTime == 0 && currentHeatValue < getMaxHeat()
						&& isValidFuel(inventory[0]))
				{
					this.currentItemBurnTime = this.fuelBurnTime = getFuelBurnTime(
							inventory[0]);
					heatPerTick = getHeatPerTick(inventory[0]);

					if (fuelBurnTime > 0)
					{
						if (this.inventory[0] != null)
						{
							this.inventory[0].stackSize--;

							if (this.inventory[0].stackSize == 0)
							{
								this.inventory[0] = inventory[0].getItem()
										.getContainerItem(inventory[0]);
							}
						}
					}
				}

				if (fuelBurnTime > 0 && currentHeatValue < getMaxHeat())
				{
					currentHeatValue += heatPerTick;
				}
			}

			if (currentHeatValue > getMaxHeat())
				currentHeatValue = getMaxHeat();
		}
	}

	void craftItem()
	{
		ConcentratorRecipe recipe = recipeToCraft();
		if (recipe != null)
		{
			this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
					pos.getX() + worldObj.rand.nextFloat(), pos.getY() + 1D,
					pos.getZ() + worldObj.rand.nextFloat(), 0.0D, 0.0D, 0.0D,
					new int[0]);
			if (!worldObj.isRemote)
			{				
				if (timeCondense >= 500)
				{
					List<EntityItem> list = worldObj.getEntitiesWithinAABB(
							EntityItem.class,
							new AxisAlignedBB(pos.getX(), pos.getY() + 1,
									pos.getZ(), pos.getX() + 1,
									pos.getY() + 1.5F, pos.getZ() + 1));

					for (EntityItem i : list)
					{
						ItemStack stack = i.getEntityItem();
						if (stack.isItemEqual(recipe.getInputStacks()))
						{
							stack.stackSize -= recipe
									.getInputStacks().stackSize;
							break;
						}
					}

					currentHeatValue *= 0.5F;

					IBlockState block = recipe.getOutput();

					worldObj.setBlockState(pos.down(), block);
				}
				else
				{
					timeCondense++;		
					if(timeCondense % 4 == 0)
					currentHeatValue--;
				}
			}
		}
		else
			timeCondense=0;
	}

	public ConcentratorRecipe recipeToCraft()
	{
		List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class,
				new AxisAlignedBB(pos.getX(), pos.getY() + 1, pos.getZ(),
						pos.getX() + 1, pos.getY() + 1.5F, pos.getZ() + 1));

		List<ItemStack> items = new ArrayList<ItemStack>();

		for (EntityItem i : list)
		{
			items.add(i.getEntityItem());
		}

		for (ItemStack stack : items)
		{
			ConcentratorRecipe recipe = ConcentratorRecipes.getRecipe(stack,
					worldObj.getBlockState(pos.down()), currentHeatValue);
			if (recipe != null)
				return recipe;
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		if (index < 0 || index >= this.getSizeInventory())
			return null;
		return this.inventory[index];
	}

	@Override
	public String getName()
	{
		return "container.skyresources.concentrator";
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
		return 1;
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
					this.setInventorySlotContents(index,
							this.getStackInSlot(index));
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
		if (index < 0 || index >= this.getSizeInventory())
			return;

		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
			stack.stackSize = this.getInventoryStackLimit();

		if (stack != null && stack.stackSize == 0)
			stack = null;

		this.inventory[index] = stack;
		this.markDirty();

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
		return this.isValidFuel(stack);
	}

	@Override
	public int getField(int id)
	{
		switch (id)
		{
		case 0:
			return fuelBurnTime;
		case 1:
			return currentItemBurnTime;
		case 2:
			return currentHeatValue;
		case 3:
			return heatPerTick;
		}
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
		switch (id)
		{
		case 0:
			fuelBurnTime = value;
		case 1:
			currentItemBurnTime = value;
		case 2:
			currentHeatValue = value;
		case 3:
			heatPerTick = value;
		}
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
}
