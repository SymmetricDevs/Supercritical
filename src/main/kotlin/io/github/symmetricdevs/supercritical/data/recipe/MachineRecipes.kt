package io.github.symmetricdevs.supercritical.data.recipe

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.*
import com.gregtechceu.gtceu.config.ConfigHolder
import com.gregtechceu.gtceu.data.recipe.CustomTags
import io.github.symmetricdevs.supercritical.common.data.ScritBlocks
import io.github.symmetricdevs.supercritical.common.data.ScritMachines
import io.github.symmetricdevs.supercritical.common.data.ScritMaterials
import io.github.symmetricdevs.supercritical.util.scId
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer

object MachineRecipes {
    fun init(provider: Consumer<FinishedRecipe>) {

        // MetaMachines
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("fission_reactor"))
            .inputItems(CustomTags.EV_CIRCUITS, 4)
            .inputItems(ScritBlocks.REACTOR_VESSEL)
            .inputItems(GTItems.SENSOR_EV)
            .inputItems(TagPrefix.rotor, GTMaterials.Steel, 2)
            .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.AnnealedCopper)
            .outputItems(ScritMachines.FISSION_REACTOR)
            .duration(400).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("heat_exchanger"))
            .inputItems(CustomTags.HV_CIRCUITS)
            .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel, 2)
            .inputItems(GTBlocks.CASING_STEEL_SOLID, 6)
            .outputItems(ScritMachines.HEAT_EXCHANGER)
            .duration(400).EUt(GTValues.VA[GTValues.HV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("gas_centrifuge"))
            .inputItems(CustomTags.EV_CIRCUITS, 3)
            .inputItems(TagPrefix.rotor, GTMaterials.Titanium)
            .inputItems(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE, 3)
            .inputItems(TagPrefix.wireGtQuadruple, GTMaterials.AnnealedCopper, 2)
            .outputItems(ScritMachines.GAS_CENTRIFUGE)
            .duration(400).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("spent_fuel_pool"))
            .inputItems(CustomTags.LV_CIRCUITS)
            .inputItems(GTBlocks.CASING_STAINLESS_CLEAN, 2)
            .inputItems(TagPrefix.plate, GTMaterials.StainlessSteel, 6)
            .outputItems(ScritMachines.SPENT_FUEL_POOL)
            .duration(400).EUt(GTValues.VA[GTValues.LV].toLong())
            .save(provider)

        // Hatches
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("coolant_input"))
            .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel)
            .inputItems(GTMachines.HULL[GTValues.EV])
            .inputFluids(GTMaterials.Polyethylene, 144)
            .circuitMeta(1)
            .outputItems(ScritMachines.COOLANT_INPUT)
            .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("coolant_output"))
            .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel)
            .inputItems(GTMachines.HULL[GTValues.EV])
            .inputFluids(GTMaterials.Polyethylene, 144)
            .circuitMeta(2)
            .outputItems(ScritMachines.COOLANT_OUTPUT)
            .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("fuel_rod_input"))
            .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 6)
            .inputItems(GTMachines.HULL[GTValues.EV])
            .inputFluids(GTMaterials.Polyethylene, 144)
            .circuitMeta(1)
            .outputItems(ScritMachines.FUEL_ROD_INPUT)
            .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("fuel_rod_output"))
            .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 6)
            .inputItems(GTMachines.HULL[GTValues.EV])
            .inputFluids(GTMaterials.Polyethylene, 144)
            .circuitMeta(2)
            .outputItems(ScritMachines.FUEL_ROD_OUTPUT)
            .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("control_rod"))
            .inputItems(TagPrefix.rodLong, GTMaterials.Hafnium)
            .inputItems(CustomTags.EV_CIRCUITS)
            .inputItems(GTMachines.HULL[GTValues.EV])
            .inputFluids(GTMaterials.Polyethylene, 144)
            .circuitMeta(1)
            .outputItems(ScritMachines.CONTROL_ROD)
            .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("control_rod_moderated"))
            .inputItems(TagPrefix.rodLong, GTMaterials.Hafnium)
            .inputItems(TagPrefix.dust, GTMaterials.Graphite)
            .inputItems(CustomTags.EV_CIRCUITS)
            .inputItems(GTMachines.HULL[GTValues.EV])
            .inputFluids(GTMaterials.Polyethylene, 144)
            .circuitMeta(2)
            .outputItems(ScritMachines.CONTROL_ROD_MODERATED)
            .duration(300).EUt(GTValues.VA[GTValues.EV].toLong())
            .save(provider)

        // Casings
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("reactor_vessel"))
            .inputItems(TagPrefix.plateDouble, ScritMaterials.Inconel)
            .inputItems(TagPrefix.plate, GTMaterials.Steel, 5)
            .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
            .outputItems(ScritBlocks.REACTOR_VESSEL, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
            .duration(280)
            .EUt(48)
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("coolant_channel"))
            .inputItems(TagPrefix.pipeLargeFluid, ScritMaterials.Inconel)
            .inputItems(TagPrefix.frameGt, GTMaterials.Steel)
            .outputItems(ScritBlocks.COOLANT_CHANNEL)
            .duration(280)
            .EUt(48)
            .save(provider)


        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("fuel_channel"))
            .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 6)
            .inputItems(TagPrefix.ring, ScritMaterials.Zircaloy)
            .circuitMeta(1)
            .outputItems(ScritBlocks.FUEL_CHANNEL)
            .duration(280)
            .EUt(48)
            .save(provider)


        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("control_rod_channel"))
            .inputItems(TagPrefix.rod, ScritMaterials.Zircaloy, 3)
            .inputItems(TagPrefix.ring, ScritMaterials.Zircaloy)
            .circuitMeta(2)
            .outputItems(ScritBlocks.CONTROL_ROD_CHANNEL)
            .duration(280)
            .EUt(48)
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("gas_centrifuge_heater"))
            .inputItems(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE)
            .inputItems(TagPrefix.wireGtSingle, GTMaterials.Nichrome, 4)
            .outputItems(ScritBlocks.GAS_CENTRIFUGE_HEATER)
            .duration(200)
            .EUt(48)
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("gas_centrifuge_column"))
            .inputItems(TagPrefix.pipeNormalFluid, GTMaterials.Steel)
            .inputItems(TagPrefix.pipeTinyFluid, GTMaterials.Steel, 3)
            .outputItems(ScritBlocks.GAS_CENTRIFUGE_COLUMN)
            .duration(200)
            .EUt(48)
            .save(provider)

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(scId("spent_fuel_casing"))
            .inputItems(TagPrefix.rod, ScritMaterials.BoronCarbide, 8)
            .outputItems(ScritBlocks.SPENT_FUEL_CASING, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
            .duration(200)
            .EUt(64)
            .save(provider)
    }
}
