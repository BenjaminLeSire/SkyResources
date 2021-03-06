package com.bartz24.skyresources.jei.waterextractor.extract;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class WaterExtractorExtractRecipeJEI extends BlankRecipeWrapper
{
	private final IBlockState inputBlock;

	private final IBlockState output;

	private final boolean fuzzyInput;
	private final int fluidOut;

	public WaterExtractorExtractRecipeJEI(int outAmt, boolean fuzzy,
			IBlockState inputState, IBlockState outputState)
	{
		inputBlock = inputState;
		output = outputState;
		fuzzyInput = fuzzy;
		fluidOut = outAmt;
	}

	@Override
	public List getInputs()
	{
		return Collections.singletonList(new ItemStack(inputBlock.getBlock(), 1,
				fuzzyInput ? OreDictionary.WILDCARD_VALUE
						: inputBlock.getBlock().getMetaFromState(inputBlock)));
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight,
			int mouseX, int mouseY)
	{
		FontRenderer fontRendererObj = minecraft.fontRendererObj;
		fontRendererObj.drawString("Extracting", 65, 0, java.awt.Color.gray.getRGB());
	}

	@Override
	public List getOutputs()
	{
		return output == null ? Collections.EMPTY_LIST : Collections.singletonList(new ItemStack(output.getBlock(), 1,
				output.getBlock().getMetaFromState(output)));
	}

    public List getFluidOutputs()
    {
        return Collections.singletonList(new FluidStack(FluidRegistry.WATER, fluidOut));
    }

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		return null;
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputs(ItemStack.class, getInputs());
		ingredients.setOutputs(ItemStack.class, getOutputs());
		ingredients.setOutputs(FluidStack.class, getFluidOutputs());
	}
}
