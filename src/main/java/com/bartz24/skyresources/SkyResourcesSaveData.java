package com.bartz24.skyresources;

import com.bartz24.skyresources.config.ConfigOptions;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class SkyResourcesSaveData extends WorldSavedData
{
	private static SkyResourcesSaveData INSTANCE;
	public static final String dataName = "SkyResources-SaveData";

	public SkyResourcesSaveData(String s)
	{
		super(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		References.CurrentIslandsList.clear();
		References.spawnedPlayers.clear();
		References.worldOneChunk = false;
		References.initialIslandDistance = ConfigOptions.islandDistance;
		NBTTagList list = nbt.getTagList("Positions",
				Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);

			IslandPos pos = new IslandPos(0, 0);
			pos.readFromNBT(stackTag);
			References.CurrentIslandsList.add(pos);
		}

		list = nbt.getTagList("SpawnedPlayers", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);

			String name = stackTag.getString("name");

			References.spawnedPlayers.add(name);
		}
		if (nbt.hasKey("oneChunkWorld"))
			References.worldOneChunk = nbt.getBoolean("oneChunkWorld");
		if (nbt.hasKey("initialDist"))
			References.initialIslandDistance = nbt.getInteger("initialDist");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < References.CurrentIslandsList.size(); i++)
		{
			NBTTagCompound stackTag = new NBTTagCompound();

			References.CurrentIslandsList.get(i).writeToNBT(stackTag);

			list.appendTag(stackTag);
		}
		nbt.setTag("Positions", list);
		NBTTagList list2 = new NBTTagList();
		for (int i = 0; i < References.spawnedPlayers.size(); i++)
		{
			NBTTagCompound stackTag = new NBTTagCompound();

			stackTag.setString("name", References.spawnedPlayers.get(i));

			list2.appendTag(stackTag);
		}
		nbt.setTag("SpawnedPlayers", list2);

		if (References.worldOneChunk)
			nbt.setBoolean("oneChunkWorld", true);

		nbt.setInteger("initialDist", References.initialIslandDistance);

		return nbt;
	}

	public static void setDirty(int dimension)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
				&& INSTANCE != null)
			INSTANCE.markDirty();
	}

	public static void setInstance(int dimension, SkyResourcesSaveData in)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			INSTANCE = in;
	}
}
