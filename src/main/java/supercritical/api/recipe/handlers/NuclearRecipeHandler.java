package supercritical.api.recipe.handlers;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.api.unification.ore.SCOrePrefix;
import supercritical.common.registry.SCItems;
import supercritical.loaders.recipe.SCRecipeUtils;

public final class NuclearRecipeHandler {

    private NuclearRecipeHandler() {}

    public static void register() {
        // GTCEu Modern no longer exposes the old CEu addProcessingHandler hook. Generate the same recipes eagerly for
        // every material that already carries Supercritical's fission-fuel property.
        for (Material material : com.gregtechceu.gtceu.api.GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(SCPropertyKey.FISSION_FUEL)) {
                processFuelRod(material, material.getProperty(SCPropertyKey.FISSION_FUEL));
            }
        }
    }

    private static void processFuelRod(Material material, FissionFuelProperty property) {
        String name = material.getName();
        SCItems.NuclearFuelItems fuelItems = SCItems.NUCLEAR_FUEL_ITEMS.get(name);

        SCRecipeUtils.addRecipe(SCRecipeMaps.SPENT_FUEL_POOL_RECIPES, SCRecipeMaps.SPENT_FUEL_POOL_RECIPES
                .recipeBuilder(name + "_spent_fuel_pool_cooling")
                .duration(10000).EUt(20)
                .inputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.hotDepletedFuelRod().get(),
                        SCOrePrefix.fuelRodHotDepleted, material))
                .outputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.depletedFuelRod().get(),
                        SCOrePrefix.fuelRodDepleted, material))
                .buildRawRecipe());

        SCRecipeUtils.addRecipe(GTRecipeTypes.CANNER_RECIPES, GTRecipeTypes.CANNER_RECIPES.recipeBuilder(name + "_depleted_fuel_rod_unpacking")
                .duration(200).EUt(GTValues.VA[GTValues.HV])
                .inputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.depletedFuelRod().get(),
                        SCOrePrefix.fuelRodDepleted, material))
                .outputItems(SCItems.FUEL_CLADDING.get())
                .outputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.depletedFuelPellet().get(),
                        SCOrePrefix.fuelPelletDepleted, material, 16))
                .buildRawRecipe());

        SCRecipeUtils.addRecipe(GTRecipeTypes.FORMING_PRESS_RECIPES, GTRecipeTypes.FORMING_PRESS_RECIPES.recipeBuilder(name + "_raw_fuel_pellet")
                .duration(25).EUt(GTValues.VA[GTValues.EV])
                .inputItems(TagPrefix.dust, material)
                .notConsumable(GTItems.SHAPE_MOLD_CYLINDER)
                .outputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.rawFuelPellet().get(),
                        SCOrePrefix.fuelPelletRaw, material))
                .buildRawRecipe());

        SCRecipeUtils.addRecipe(GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder(name + "_fuel_pellet")
                .duration(15).EUt(GTValues.VA[GTValues.HV])
                .blastFurnaceTemp(2000)
                .inputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.rawFuelPellet().get(),
                        SCOrePrefix.fuelPelletRaw, material))
                .outputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.fuelPellet().get(),
                        SCOrePrefix.fuelPellet, material))
                .buildRawRecipe());

        SCRecipeUtils.addRecipe(GTRecipeTypes.CANNER_RECIPES, GTRecipeTypes.CANNER_RECIPES.recipeBuilder(name + "_fuel_rod")
                .duration(300).EUt(GTValues.VA[GTValues.HV])
                .inputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.fuelPellet().get(),
                        SCOrePrefix.fuelPellet, material, 16))
                .inputItems(SCItems.FUEL_CLADDING.get())
                .outputItems(itemOrPrefix(fuelItems == null ? null : fuelItems.fuelRod().get(),
                        SCOrePrefix.fuelRod, material))
                .buildRawRecipe());
    }

    private static net.minecraft.world.item.ItemStack itemOrPrefix(net.minecraft.world.item.Item item, TagPrefix prefix,
                                                                    Material material) {
        return itemOrPrefix(item, prefix, material, 1);
    }

    private static net.minecraft.world.item.ItemStack itemOrPrefix(net.minecraft.world.item.Item item, TagPrefix prefix,
                                                                    Material material, int count) {
        if (item != null) return new net.minecraft.world.item.ItemStack(item, count);
        return ChemicalHelper.get(prefix, material, count);
    }
}
