package supercritical.integration.jei.basic;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import supercritical.api.nuclear.fission.IModeratorStats;
import supercritical.api.nuclear.fission.ModeratorRegistry;

public class ModeratorInfo implements IRecipeWrapper {

    public IBlockState block;
    public ItemStack stack;

    private final String maxTemp;
    private final String moderationFactor;
    private final String absorptionFactor;

    public ModeratorInfo(ResourceLocation name, int meta) {
        Block blockFull = Block.REGISTRY.getObject(name);
        this.block = blockFull.getStateFromMeta(meta);
        stack = new ItemStack(blockFull, 1, meta);
        IModeratorStats prop = ModeratorRegistry.getModerator(this.block);

        maxTemp = I18n.format("metaitem.nuclear.tooltip.temperature", prop.getMaxTemperature());
        moderationFactor = I18n.format("metaitem.nuclear.tooltip.moderation_factor", prop.getModerationFactor());
        absorptionFactor = I18n.format("metaitem.nuclear.tooltip.absorption_factor", prop.getAbsorptionFactor());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack blockItem = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
        ingredients.setInput(VanillaTypes.ITEM, blockItem);
        ingredients.setOutput(VanillaTypes.ITEM, blockItem);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int fontHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

        int start = 40;
        int linesDrawn = 0;
        minecraft.fontRenderer.drawString(maxTemp, 0, fontHeight * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(moderationFactor, 0, fontHeight * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(absorptionFactor, 0, fontHeight * linesDrawn + start, 0x111111);
    }
}
