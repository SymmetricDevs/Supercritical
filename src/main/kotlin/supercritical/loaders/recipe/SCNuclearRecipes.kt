package supercritical.loaders.recipe

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.unification.material.SCMaterials
import supercritical.api.unification.ore.SCOrePrefix
import supercritical.common.registry.ScritItems

object SCNuclearRecipes {
    fun init() {
        chemistryAndMaterials()
        gasCentrifugeRecipes()
        fuelCycleRecipes()
        radonRecipes()
        componentRecipes()
    }

    private fun chemistryAndMaterials() {
        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES, GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("boron_trioxide")
                .inputItems(TagPrefix.dust, GTMaterials.Boron, 2)
                .inputFluids(GTMaterials.Oxygen.getFluid(3000))
                .outputItems(TagPrefix.dust, SCMaterials.BoronTrioxide, 5)
                .EUt(GTValues.VA[GTValues.LV].toLong()).duration(200)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES, GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("boron_carbide")
                .inputItems(TagPrefix.dust, SCMaterials.BoronTrioxide, 10)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon, 7)
                .outputItems(TagPrefix.dust, SCMaterials.BoronCarbide, 5)
                .outputFluids(GTMaterials.CarbonMonoxide.getFluid(6000))
                .EUt(GTValues.VA[GTValues.MV].toLong()).duration(400)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("low_grade_mox")
                .inputItems(TagPrefix.dust, SCMaterials.FissilePlutoniumDioxide)
                .inputItems(TagPrefix.dust, GTMaterials.Uraninite, 19)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, SCMaterials.LowGradeMOX, 20)
                .duration(400).EUt(GTValues.VA[GTValues.HV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("high_grade_mox")
                .inputItems(TagPrefix.dust, SCMaterials.FissilePlutoniumDioxide)
                .inputItems(TagPrefix.dust, GTMaterials.Uraninite, 4)
                .circuitMeta(2)
                .outputItems(TagPrefix.dust, SCMaterials.HighGradeMOX, 5)
                .duration(400).EUt(GTValues.VA[GTValues.HV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder("zircon_processing")
                .inputItems(TagPrefix.dust, SCMaterials.Zircon)
                .outputItems(TagPrefix.dust, GTMaterials.SiliconDioxide, 3)
                .chancedOutput(TagPrefix.dust, SCMaterials.ZirconiumDioxide, 3, 9000, 0)
                .chancedOutput(TagPrefix.dust, SCMaterials.HafniumDioxide, 3, 1000, 0)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong()).blastFurnaceTemp(2100)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder("zirconium_tetrachloride")
                .inputItems(TagPrefix.dust, SCMaterials.ZirconiumDioxide, 3)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon)
                .inputFluids(GTMaterials.Chlorine.getFluid(4000))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(1000))
                .outputItems(TagPrefix.dust, SCMaterials.ZirconiumTetrachloride, 5)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong()).blastFurnaceTemp(1400)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder("hafnium_tetrachloride")
                .inputItems(TagPrefix.dust, SCMaterials.HafniumDioxide, 3)
                .inputItems(TagPrefix.dust, GTMaterials.Carbon)
                .inputFluids(GTMaterials.Chlorine.getFluid(4000))
                .outputFluids(GTMaterials.CarbonDioxide.getFluid(1000))
                .outputItems(TagPrefix.dust, SCMaterials.HafniumTetrachloride, 5)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong()).blastFurnaceTemp(1250)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder("zirconium_reduction")
                .inputItems(TagPrefix.dust, SCMaterials.ZirconiumTetrachloride, 5)
                .inputItems(TagPrefix.dust, GTMaterials.Magnesium, 2)
                .outputItems(TagPrefix.dust, GTMaterials.Zirconium)
                .outputItems(TagPrefix.dust, GTMaterials.MagnesiumChloride, 6)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong()).blastFurnaceTemp(1150)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.BLAST_RECIPES, GTRecipeTypes.BLAST_RECIPES.recipeBuilder("hafnium_reduction")
                .inputItems(TagPrefix.dust, SCMaterials.HafniumTetrachloride, 5)
                .inputItems(TagPrefix.dust, GTMaterials.Magnesium, 2)
                .outputItems(TagPrefix.dust, GTMaterials.Hafnium)
                .outputItems(TagPrefix.dust, GTMaterials.MagnesiumChloride, 6)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong()).blastFurnaceTemp(1150)
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("zircaloy")
                .inputItems(TagPrefix.dust, GTMaterials.Zirconium, 16)
                .inputItems(TagPrefix.dust, GTMaterials.Tin, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Chromium)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, SCMaterials.Zircaloy, 19)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("inconel_718")
                .inputItems(TagPrefix.dust, GTMaterials.Nickel, 5)
                .inputItems(TagPrefix.dust, GTMaterials.Chromium, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Iron, 2)
                .inputItems(TagPrefix.dust, GTMaterials.Niobium)
                .inputItems(TagPrefix.dust, GTMaterials.Molybdenum)
                .circuitMeta(4)
                .outputItems(TagPrefix.dust, SCMaterials.Inconel, 11)
                .duration(200).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )
    }

    private fun gasCentrifugeRecipes() {
        SCRecipeUtils.addRecipe(
            SCRecipeMaps.GAS_CENTRIFUGE_RECIPES,
            SCRecipeMaps.GAS_CENTRIFUGE_RECIPES.recipeBuilder("uranium_hexafluoride_enrichment")
                .inputFluids(GTMaterials.UraniumHexafluoride.getFluid(1000))
                .outputFluids(
                    GTMaterials.EnrichedUraniumHexafluoride.getFluid(100),
                    GTMaterials.DepletedUraniumHexafluoride.getFluid(900)
                )
                .duration(800).EUt(GTValues.VA[GTValues.HV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            SCRecipeMaps.GAS_CENTRIFUGE_RECIPES,
            SCRecipeMaps.GAS_CENTRIFUGE_RECIPES.recipeBuilder("enriched_uranium_hexafluoride_enrichment")
                .inputFluids(GTMaterials.EnrichedUraniumHexafluoride.getFluid(1000))
                .outputFluids(
                    SCMaterials.HighEnrichedUraniumHexafluoride.getFluid(100),
                    GTMaterials.DepletedUraniumHexafluoride.getFluid(900)
                )
                .duration(800).EUt(GTValues.VA[GTValues.HV].toLong())
                .buildRawRecipe()
        )
    }

    private fun fuelCycleRecipes() {
        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES, GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("depleted_uranium_dioxide")
                .inputFluids(
                    GTMaterials.DepletedUraniumHexafluoride.getFluid(1000),
                    GTMaterials.Water.getFluid(2000),
                    GTMaterials.Hydrogen.getFluid(2000)
                )
                .outputItems(TagPrefix.dust, SCMaterials.DepletedUraniumDioxide, 3)
                .outputFluids(GTMaterials.HydrofluoricAcid.getFluid(6000))
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("leu_235_from_heu")
                .inputItems(TagPrefix.dust, SCMaterials.HighEnrichedUraniumDioxide)
                .inputItems(TagPrefix.dust, SCMaterials.DepletedUraniumDioxide, 19)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, SCMaterials.LEU235, 20)
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("leu_235_from_low_enriched")
                .inputItems(TagPrefix.dust, SCMaterials.LowEnrichedUraniumDioxide)
                .inputItems(TagPrefix.dust, SCMaterials.DepletedUraniumDioxide, 3)
                .circuitMeta(1)
                .outputItems(TagPrefix.dust, SCMaterials.LEU235, 4)
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.MIXER_RECIPES.recipeBuilder("heu_235")
                .inputItems(TagPrefix.dust, SCMaterials.HighEnrichedUraniumDioxide)
                .inputItems(TagPrefix.dust, SCMaterials.DepletedUraniumDioxide, 4)
                .circuitMeta(2)
                .outputItems(TagPrefix.dust, SCMaterials.HEU235, 5)
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("fissile_plutonium_dioxide_239")
                .inputItems(TagPrefix.dust, GTMaterials.Plutonium239)
                .inputFluids(GTMaterials.Oxygen.getFluid(2000))
                .outputItems(TagPrefix.dust, SCMaterials.FissilePlutoniumDioxide, 3)
                .duration(40).EUt(GTValues.VA[GTValues.ULV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder("fissile_plutonium_dioxide_241")
                .inputItems(TagPrefix.dust, GTMaterials.Plutonium241)
                .inputFluids(GTMaterials.Oxygen.getFluid(2000))
                .outputItems(TagPrefix.dust, SCMaterials.FissilePlutoniumDioxide, 3)
                .duration(40).EUt(GTValues.VA[GTValues.ULV].toLong())
                .buildRawRecipe()
        )

        fuelReprocessing(
            "leu_235",
            SCMaterials.LEU235,
            GTMaterials.UraniumHexafluoride,
            633,
            intArrayOf(282, 132, 84, 18),
            arrayOf<Any?>(
                GTMaterials.Zirconium,
                1645,
                GTMaterials.Molybdenum,
                1169,
                GTMaterials.Neodymium,
                1030,
                GTMaterials.Lead,
                659,
                GTMaterials.Ruthenium,
                609,
                GTMaterials.Technetium,
                297
            ),
            16,
            111,
            125
        )
        fuelReprocessing(
            "heu_235",
            SCMaterials.HEU235,
            GTMaterials.EnrichedUraniumHexafluoride,
            821,
            intArrayOf(235, 110, 70, 15),
            arrayOf<Any?>(
                GTMaterials.Zirconium,
                1645,
                GTMaterials.Molybdenum,
                1182,
                GTMaterials.Neodymium,
                1031,
                GTMaterials.Ruthenium,
                600,
                GTMaterials.Technetium,
                300,
                GTMaterials.Yttrium,
                211
            ),
            16,
            110,
            129
        )
        fuelReprocessing(
            "low_grade_mox",
            SCMaterials.LowGradeMOX,
            GTMaterials.DepletedUraniumHexafluoride,
            565,
            intArrayOf(0, 165, 5, 15),
            arrayOf<Any?>(
                GTMaterials.Neodymium,
                1015,
                GTMaterials.Molybdenum,
                937,
                GTMaterials.Zirconium,
                863,
                GTMaterials.Palladium,
                738,
                GTMaterials.Bismuth,
                300,
                GTMaterials.Tellurium,
                188
            ),
            6,
            126,
            118
        )
        fuelReprocessing(
            "high_grade_mox",
            SCMaterials.HighGradeMOX,
            GTMaterials.DepletedUraniumHexafluoride,
            1141,
            intArrayOf(0, 724, 192, 3),
            arrayOf<Any?>(
                GTMaterials.Neodymium,
                1020,
                GTMaterials.Molybdenum,
                937,
                GTMaterials.Zirconium,
                863,
                GTMaterials.Samarium,
                319,
                GTMaterials.Tellurium,
                187,
                GTMaterials.Promethium,
                119
            ),
            6,
            126,
            114
        )
    }

    private fun fuelReprocessing(
        id: String?, material: Material,
        fluorideOutput: Material,
        fissionByproductChance: Int, bredChances: IntArray, byproducts: Array<Any?>,
        krypton: Int, xenon: Int, radon: Int
    ) {
        SCRecipeUtils.addRecipe(
            GTRecipeTypes.ELECTROLYZER_RECIPES,
            GTRecipeTypes.ELECTROLYZER_RECIPES.recipeBuilder(id + "_electrolytic_reprocessing")
                .notConsumable(ScritItems.ANODE_BASKET.get())
                .notConsumableFluid(GTMaterials.Salt.getFluid(1000))
                .inputItems(SCOrePrefix.fuelPelletDepleted, material)
                .outputItems(SCOrePrefix.dustSpentFuel, material)
                .outputItems(SCOrePrefix.dustBredFuel, material)
                .chancedOutput(SCOrePrefix.dustFissionByproduct, material, fissionByproductChance, 0)
                .duration(800).EUt(GTValues.VA[GTValues.EV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES.recipeBuilder(id + "_spent_fuel_fluorination")
                .inputItems(SCOrePrefix.dustSpentFuel, material)
                .inputFluids(GTMaterials.HydrofluoricAcid.getFluid(4000), GTMaterials.Fluorine.getFluid(2000))
                .outputFluids(fluorideOutput.getFluid(1000), GTMaterials.Water.getFluid(2000))
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        val bred = GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(id + "_bred_fuel_centrifuging")
            .inputItems(SCOrePrefix.dustBredFuel, material)
            .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
        if (bredChances[0] > 0) bred.chancedOutput(TagPrefix.dust, GTMaterials.Plutonium239, bredChances[0], 0)
        if (bredChances[1] > 0) bred.chancedOutput(TagPrefix.dust, SCMaterials.Plutonium240, bredChances[1], 0)
        if (bredChances[2] > 0) bred.chancedOutput(TagPrefix.dust, GTMaterials.Plutonium241, bredChances[2], 0)
        if ("high_grade_mox" == id) bred.chancedOutput(TagPrefix.dust, SCMaterials.Plutonium242, 59, 0)
        bred.chancedOutput(TagPrefix.dust, SCMaterials.Neptunium239, bredChances[3], 0)
        SCRecipeUtils.addRecipe(GTRecipeTypes.CENTRIFUGE_RECIPES, bred.buildRawRecipe())

        val fission = GTRecipeTypes.CENTRIFUGE_RECIPES.recipeBuilder(id + "_fission_byproduct_centrifuging")
            .inputItems(SCOrePrefix.dustFissionByproduct, material)
            .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
        var i = 0
        while (i < byproducts.size) {
            fission.chancedOutput(
                TagPrefix.dust,
                byproducts[i] as Material?, byproducts[i + 1] as Int, 0
            )
            i += 2
        }
        fission.outputFluids(
            GTMaterials.Krypton.getFluid(krypton),
            GTMaterials.Xenon.getFluid(xenon),
            GTMaterials.Radon.getFluid(radon)
        )
        SCRecipeUtils.addRecipe(GTRecipeTypes.CENTRIFUGE_RECIPES, fission.buildRawRecipe())
    }

    private fun radonRecipes() {
        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_BATH_RECIPES,
            GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder("radon_rich_gas_from_uraninite")
                .inputItems(TagPrefix.crushed, GTMaterials.Uraninite)
                .inputFluids(GTMaterials.DilutedHydrochloricAcid.getFluid(100))
                .outputItems(TagPrefix.crushedPurified, GTMaterials.Uraninite)
                .outputFluids(SCMaterials.RadonRichGasMixture.getFluid(1000))
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.CHEMICAL_BATH_RECIPES,
            GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder("radon_rich_gas_from_pitchblende")
                .inputItems(TagPrefix.crushed, GTMaterials.Pitchblende)
                .inputFluids(GTMaterials.DilutedHydrochloricAcid.getFluid(150))
                .outputItems(TagPrefix.crushedPurified, GTMaterials.Pitchblende)
                .outputFluids(SCMaterials.RadonRichGasMixture.getFluid(1500))
                .duration(200).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.DISTILLATION_RECIPES,
            GTRecipeTypes.DISTILLATION_RECIPES.recipeBuilder("radon_rich_gas_mixture_distillation")
                .inputFluids(SCMaterials.RadonRichGasMixture.getFluid(3000))
                .outputFluids(GTMaterials.Radon.getFluid(1000), GTMaterials.Helium.getFluid(2000))
                .duration(1000).EUt(GTValues.VHA[GTValues.HV].toLong())
                .buildRawRecipe()
        )
    }

    private fun componentRecipes() {
        SCRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("anode_basket")
                .inputItems(TagPrefix.ring, GTMaterials.Titanium, 2)
                .inputItems(TagPrefix.rod, GTMaterials.Titanium, 16)
                .outputItems(ScritItems.ANODE_BASKET.get())
                .duration(400).EUt(GTValues.VA[GTValues.LV].toLong())
                .buildRawRecipe()
        )

        SCRecipeUtils.addRecipe(
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("fuel_cladding")
                .inputItems(TagPrefix.plate, SCMaterials.Zircaloy, 4)
                .inputItems(TagPrefix.spring, SCMaterials.Inconel)
                .inputItems(TagPrefix.round, GTMaterials.StainlessSteel, 2)
                .outputItems(ScritItems.FUEL_CLADDING.get())
                .duration(200).EUt(GTValues.VA[GTValues.MV].toLong())
                .buildRawRecipe()
        )
    }
}
