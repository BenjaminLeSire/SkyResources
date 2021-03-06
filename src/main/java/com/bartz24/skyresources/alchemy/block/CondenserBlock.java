package com.bartz24.skyresources.alchemy.block;

import java.util.List;

import com.bartz24.skyresources.References;
import com.bartz24.skyresources.alchemy.tile.CondenserTile;
import com.bartz24.skyresources.config.ConfigOptions;
import com.bartz24.skyresources.registry.ModCreativeTabs;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CondenserBlock extends BlockContainer
{

	public CondenserBlock(String unlocalizedName, String registryName,
			float hardness, float resistance)
	{
		super(Material.ROCK);
		this.setUnlocalizedName(References.ModID + "." + unlocalizedName);
		this.setCreativeTab(ModCreativeTabs.tabAlchemy);
		this.setHardness(hardness);
		this.setResistance(resistance);
		this.setRegistryName(registryName);
		this.isBlockContainer = true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new CondenserTile();
	}

	public void addInformation(ItemStack stack, EntityPlayer par2EntityPlayer, List list,
			boolean par4)
	{
			if (ConfigOptions.easyMode)
				list.add(TextFormatting.RED + "Disabled in easy mode. Only used for crafting purposes.");
	}
}
