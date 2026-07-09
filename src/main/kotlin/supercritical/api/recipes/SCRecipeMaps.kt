package supercritical.api.recipes;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SCRecipeMaps {

    public static final String HEAT_EXCHANGER_ID = "heat_exchanger";
    public static final String SPENT_FUEL_POOL_ID = "spent_fuel_pool";
    public static final String GAS_CENTRIFUGE_ID = "gas_centrifuge";

    public static GTRecipeType HEAT_EXCHANGER_RECIPES;
    public static GTRecipeType SPENT_FUEL_POOL_RECIPES;
    public static GTRecipeType GAS_CENTRIFUGE_RECIPES;

    private static final Map<String, RecipeMapInfo> RECIPE_MAPS = new LinkedHashMap<>();

    private static boolean initialized;

    private SCRecipeMaps() {}

    public static synchronized void init() {
        if (initialized) return;

        HEAT_EXCHANGER_RECIPES = GTRecipeTypes.register(HEAT_EXCHANGER_ID, GTRecipeTypes.MULTIBLOCK)
                .setMaxIOSize(1, 0, 2, 2)
                .setSound(GTSoundEntries.COOLING);

        SPENT_FUEL_POOL_RECIPES = GTRecipeTypes.register(SPENT_FUEL_POOL_ID, GTRecipeTypes.MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1);

        GAS_CENTRIFUGE_RECIPES = GTRecipeTypes.register(GAS_CENTRIFUGE_ID, GTRecipeTypes.MULTIBLOCK)
                .setMaxIOSize(0, 0, 1, 2)
                .setSound(GTSoundEntries.CENTRIFUGE);

        register(HEAT_EXCHANGER_ID, HEAT_EXCHANGER_RECIPES, 1, 0, 2, 2);
        register(SPENT_FUEL_POOL_ID, SPENT_FUEL_POOL_RECIPES, 1, 1, 1, 1);
        register(GAS_CENTRIFUGE_ID, GAS_CENTRIFUGE_RECIPES, 0, 0, 1, 2);

        initialized = true;
    }

    private static void register(String id, GTRecipeType recipeType, int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs) {
        RECIPE_MAPS.put(id, new RecipeMapInfo(id, recipeType, itemInputs, itemOutputs, fluidInputs, fluidOutputs));
    }

    public static Map<String, RecipeMapInfo> all() {
        return Collections.unmodifiableMap(RECIPE_MAPS);
    }

    public record RecipeMapInfo(String id, GTRecipeType recipeType, int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs) {}
}
