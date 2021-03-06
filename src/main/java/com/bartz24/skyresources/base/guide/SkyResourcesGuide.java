package com.bartz24.skyresources.base.guide;

import java.util.ArrayList;
import java.util.List;

import joptsimple.internal.Strings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class SkyResourcesGuide
{
	private static List<GuidePage> guidePages;

	private static List<GuidePageButton> blankButtonTypes;

	public SkyResourcesGuide()
	{
		guidePages = new ArrayList<GuidePage>();
		blankButtonTypes = new ArrayList<GuidePageButton>();
	}

	public static List<String> getCategories()
	{
		List<String> cats = new ArrayList<String>();

		for (GuidePage p : guidePages)
		{
			if (!cats.contains(p.pageCategory))
				cats.add(p.pageCategory);
		}
		return cats;
	}

	public static GuidePage getPage(String id)
	{
		for (GuidePage p : guidePages)
		{
			if (p.pageId.equals(id))
				return p;
		}
		return null;
	}

	public static List<GuidePage> getPages(String category, String filter)
	{
		if (Strings.isNullOrEmpty(category) && Strings.isNullOrEmpty(filter.trim()))
			return guidePages;
		List<GuidePage> pages = new ArrayList<GuidePage>();
		for (GuidePage p : guidePages)
		{
			if ((category == null || p.pageCategory.equals(category))
					&& (Strings.isNullOrEmpty(filter.trim())
							|| p.pageDisplay.toLowerCase().contains(filter.trim().toLowerCase())))
				pages.add(p);
		}
		return pages;
	}

	public static List<GuidePage> getPages(String category)
	{
		if (Strings.isNullOrEmpty(category))
			return guidePages;
		List<GuidePage> pages = new ArrayList<GuidePage>();
		for (GuidePage p : guidePages)
		{
			if (p.pageCategory.equals(category))
				pages.add(p);
		}
		return pages;
	}

	public static void addPage(String id, String category, ItemStack displayStack)
	{
		String base = category + "." + id;

		guidePages.add(new GuidePage(id, category,
				I18n.translateToLocal(base + ".title").replace("\\n", "\n"), displayStack,
				I18n.translateToLocal(base + ".text").replace("\\n", "\n")));
	}

	public static void addPage(String id, String category, ItemStack displayStack, String info)
	{
		String base = category + "." + id;

		guidePages.add(new GuidePage(id, category,
				I18n.translateToLocal(base + ".title").replace("\\n", "\n"), displayStack, info));
	}

	public static void addButtonType(GuidePageButton button)
	{
		for (GuidePageButton b : blankButtonTypes)
		{
			if (b.getClass() == button.getClass())
			{
				return;
			}
		}
		blankButtonTypes.add(button);
	}

	public static GuidePageButton getBlankButton(String buttonType)
	{
		for (GuidePageButton b : blankButtonTypes)
		{
			if (b.getIdentifier().equals(buttonType))
				return b.clone();
		}
		return null;
	}
}
