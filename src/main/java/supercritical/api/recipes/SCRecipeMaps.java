package supercritical.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;
import supercritical.api.recipes.builders.NoEnergyRecipeBuilder;
import supercritical.common.SCConfigHolder;

public class SCRecipeMaps {

    public static final RecipeMap<NoEnergyRecipeBuilder> HEAT_EXCHANGER_RECIPES = new RecipeMap<>("heat_exchanger", 1,
            0, 2, 2, new NoEnergyRecipeBuilder(), SCConfigHolder.misc.enableHX)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> SPENT_FUEL_POOL_RECIPES = new RecipeMap<>("spent_fuel_pool", 1,
            1, 1, 1, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, ProgressWidget.MoveType.HORIZONTAL);

    public static final RecipeMap<SimpleRecipeBuilder> GAS_CENTRIFUGE_RECIPES = new RecipeMap<>("gas_centrifuge", 0, 0,
            1, 2, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    public static void init() {}
}
