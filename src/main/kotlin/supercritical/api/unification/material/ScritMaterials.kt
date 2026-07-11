package supercritical.api.unification.material

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.fluids.FluidBuilder
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.common.data.GTMaterials
import net.minecraft.world.item.ItemStack
import net.minecraftforge.eventbus.api.SubscribeEvent
import supercritical.ScritAddon
import supercritical.api.nuclear.fission.CoolantRegistry
import supercritical.api.nuclear.fission.FissionFuelRegistry
import supercritical.api.nuclear.fission.ModeratorRegistry
import supercritical.api.unification.material.properties.CoolantProperty
import supercritical.api.unification.material.properties.FissionFuelProperty
import supercritical.api.unification.material.properties.ModeratorProperty
import supercritical.api.unification.material.properties.ScritPropertyKey
import supercritical.api.util.scId
import supercritical.common.ScritConfig
import supercritical.common.registry.ScritItems
import java.util.List

object ScritMaterials {
    private val FUEL_ITEM_ENTRIES: MutableList<FuelItemEntry> = arrayListOf()

    lateinit var Uranium239: Material
    lateinit var Neptunium235: Material
    lateinit var Neptunium236: Material
    lateinit var Neptunium237: Material
    lateinit var Neptunium239: Material
    lateinit var Plutonium238: Material
    lateinit var Plutonium240: Material
    lateinit var Plutonium242: Material
    lateinit var Plutonium244: Material

    lateinit var HighEnrichedUraniumDioxide: Material
    lateinit var DepletedUraniumDioxide: Material
    lateinit var HighPressureSteam: Material
    lateinit var FissilePlutoniumDioxide: Material
    lateinit var Zircaloy: Material
    lateinit var LowEnrichedUraniumDioxide: Material
    lateinit var Zircon: Material
    lateinit var ZirconiumDioxide: Material
    lateinit var ZirconiumTetrachloride: Material
    lateinit var HafniumDioxide: Material
    lateinit var HafniumTetrachloride: Material
    lateinit var Inconel: Material
    lateinit var HighEnrichedUraniumHexafluoride: Material
    lateinit var BoronTrioxide: Material
    lateinit var BoronCarbide: Material
    lateinit var HighPressureHeavyWater: Material
    lateinit var HeavyWater: Material

    lateinit var SpentUraniumFuelSolution: Material
    lateinit var RadonRichGasMixture: Material

    lateinit var Corium: Material
    lateinit var LEU235: Material
    lateinit var HEU235: Material
    lateinit var LowGradeMOX: Material
    lateinit var HighGradeMOX: Material

    private var registry: MaterialRegistry? = null

    @SubscribeEvent
    fun createRegistry(event: MaterialRegistryEvent?) {
        registry = GTCEuAPI.materialManager.createRegistry("supercritical")
    }

    @SubscribeEvent
    fun register(event: MaterialEvent?) {
        registerCoreMaterials()
        if (ScritConfig.INSTANCE.misc.disableAllMaterials) return
        registerElementMaterials()
        registerFirstDegreeMaterials()
        registerSecondDegreeMaterials()
        registerUnknownCompositionMaterials()
    }

    @SubscribeEvent
    fun modifyExistingMaterials(@Suppress("UNUSED_PARAMETER") event: PostMaterialEvent) {
        val misc = ScritConfig.INSTANCE.misc
        if (misc.enableMaterialModifications) {
            applyMaterialModifications(includeDistilledWaterCoolant = !misc.disableAllMaterials)
        }
    }

    private fun registerCoreMaterials() {
        Corium = builder("corium")
            .liquid(
                FluidBuilder()
                    .temperature(2500)
                    .density(8.0)
                    .viscosity(10000)
                    .block()
            )
            .color(0x7A6B50)
            .iconSet(MaterialIconSet.DULL)
            // DISABLE_MATERIAL_RECIPES is the modern (non-deprecated) replacement for NO_UNIFICATION
            // (both disable auto recipe generation for the material; Corium is byproduct-only).
            .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES, MaterialFlags.STICKY, MaterialFlags.PHOSPHORESCENT)
            .buildAndRegister()
    }

    private fun registerElementMaterials() {
        Uranium239 = builder("uranium_239")
            .color(0x46FA46).iconSet(MaterialIconSet.SHINY)
            .element(ScritAddon.Uranium239)
            .buildAndRegister()

        Neptunium235 = builder("neptunium_235")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Neptunium235)
            .buildAndRegister()
        Neptunium236 = builder("neptunium_236")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Neptunium236)
            .buildAndRegister()
        Neptunium237 = builder("neptunium_237")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Neptunium237)
            .buildAndRegister()
        Neptunium239 = builder("neptunium_239")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Neptunium239)
            .buildAndRegister()

        Plutonium238 = builder("plutonium_238")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Plutonium238)
            .buildAndRegister()
        Plutonium240 = builder("plutonium_240")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Plutonium240)
            .buildAndRegister()
        Plutonium242 = builder("plutonium_242")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Plutonium242)
            .buildAndRegister()
        Plutonium244 = builder("plutonium_244")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(ScritAddon.Plutonium244)
            .buildAndRegister()
    }

    private fun registerFirstDegreeMaterials() {
        HighEnrichedUraniumDioxide = builder("high_enriched_uranium_dioxide")
            .dust(3)
            .color(0x53E353).iconSet(MaterialIconSet.DULL)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Uranium235, 1, GTMaterials.Oxygen, 2)
            .buildAndRegister()
            .setFormula("UO2", true)

        DepletedUraniumDioxide = builder("depleted_uranium_dioxide")
            .dust(3)
            .color(0x335323).iconSet(MaterialIconSet.DULL)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Uranium238, 1, GTMaterials.Oxygen, 2)
            .buildAndRegister()
            .setFormula("UO2", true)

        HighPressureSteam = builder("high_pressure_steam")
            .gas(FluidBuilder().temperature(500).customStill())
            .color(0xC4C4C4)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Hydrogen, 2, GTMaterials.Oxygen, 1)
            .buildAndRegister()

        FissilePlutoniumDioxide = builder("fissile_plutonium_dioxide")
            .dust(3)
            .color(0xF03232).iconSet(MaterialIconSet.DULL)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Plutonium239, 1, GTMaterials.Oxygen, 2)
            .buildAndRegister()

        Zircaloy = builder("zircaloy")
            .ingot()
            .color(0x566570).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.GENERATE_RING, MaterialFlags.GENERATE_PLATE)
            .components(GTMaterials.Zirconium, 16, GTMaterials.Tin, 2, GTMaterials.Chromium, 1)
            .blast(1700, BlastProperty.GasTier.LOW)
            .buildAndRegister()

        LowEnrichedUraniumDioxide = builder("low_enriched_uranium_dioxide")
            .dust()
            .color(0x43A333)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Uranium235, 1, GTMaterials.Oxygen, 2)
            .buildAndRegister()
            .setFormula("UO2", true)

        Zircon = builder("zircon")
            .gem().ore()
            .color(0x6E0909)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Zirconium, 1, GTMaterials.Silicon, 1, GTMaterials.Oxygen, 4)
            .iconSet(MaterialIconSet.SHINY)
            .buildAndRegister()

        ZirconiumDioxide = builder("zirconium_dioxide")
            .dust()
            .color(0x689F9F)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Zirconium, 1, GTMaterials.Oxygen, 2)
            .buildAndRegister()

        ZirconiumTetrachloride = builder("zirconium_tetrachloride")
            .dust()
            .color(0x689FBF)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Zirconium, 1, GTMaterials.Chlorine, 4)
            .iconSet(MaterialIconSet.SHINY)
            .buildAndRegister()

        HafniumDioxide = builder("hafnium_dioxide")
            .dust()
            .color(0x39393A)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Hafnium, 1, GTMaterials.Oxygen, 2)
            .buildAndRegister()

        HafniumTetrachloride = builder("hafnium_tetrachloride")
            .dust()
            .color(0x69699A)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Hafnium, 1, GTMaterials.Chlorine, 4)
            .iconSet(MaterialIconSet.SHINY)
            .buildAndRegister()

        Inconel = builder("inconel")
            .ingot().fluid()
            .color(0x7F8F75).iconSet(MaterialIconSet.SHINY)
            // GENERATE_PLATE already auto-generates the legacy 2x "plateDouble" (modern plate);
            // GENERATE_DENSE is intentionally omitted — it would generate a 9x "plateDense" that
            // legacy never had.
            .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_SPRING, MaterialFlags.DISABLE_DECOMPOSITION)
            .components(
                GTMaterials.Nickel,
                5,
                GTMaterials.Chromium,
                2,
                GTMaterials.Iron,
                2,
                GTMaterials.Niobium,
                1,
                GTMaterials.Molybdenum,
                1
            )
            .blast { b: BlastProperty.Builder ->
                b.temp(1610, BlastProperty.GasTier.MID).blastStats(2048, 200)
            }
            .fluidPipeProperties(2010, 175, true, true, true, false)
            .buildAndRegister()

        HighEnrichedUraniumHexafluoride = builder("high_enriched_uranium_hexafluoride")
            .gas()
            .color(0x5BF93A)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Uranium235, 1, GTMaterials.Fluorine, 6)
            .buildAndRegister()

        BoronTrioxide = builder("boron_trioxide")
            .dust()
            .color(0xC1E9E1)
            .components(GTMaterials.Boron, 2, GTMaterials.Oxygen, 3)
            .iconSet(MaterialIconSet.METALLIC)
            .buildAndRegister()

        BoronCarbide = builder("boron_carbide")
            .ingot()
            .flags(MaterialFlags.GENERATE_ROD, MaterialFlags.DISABLE_DECOMPOSITION)
            .blast(2620)
            .color(0xC1E9C1)
            .components(GTMaterials.Boron, 4, GTMaterials.Carbon, 1)
            .iconSet(MaterialIconSet.METALLIC)
            .buildAndRegister()

        HighPressureHeavyWater = builder("high_pressure_heavy_water")
            .gas(FluidBuilder().temperature(500))
            .color(0xCCD9F0)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(GTMaterials.Deuterium, 2, GTMaterials.Oxygen, 1)
            .buildAndRegister()

        HeavyWater = builder("heavy_water")
            .fluid()
            .color(0x3673D6)
            .components(GTMaterials.Deuterium, 2, GTMaterials.Oxygen, 1)
            .buildAndRegister()
        val heavyWaterCoolant = CoolantProperty(
            HeavyWater, HighPressureHeavyWater, FluidStorageKeys.LIQUID,
            4.0, 1000.0, 374.4, 2064000.0, 4228.0
        ).setAccumulatesHydrogen(true)
        HeavyWater.setProperty<CoolantProperty?>(ScritPropertyKey.COOLANT, heavyWaterCoolant)
    }

    private fun registerSecondDegreeMaterials() {
        LEU235 = builder("leu_235")
            .dust(3)
            .color(0x232323).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 19)
            .buildAndRegister()
            .setFormula("UO2", true)
        registerFuel(LEU235, "leu_235", 1500, 75000, 3.5, 0.4, 1.8, 1.8, 2.5, 0.01, 0.025)

        HEU235 = builder("heu_235")
            .dust(3)
            .color(0x424845).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
            .buildAndRegister()
            .setFormula("UO2", true)
        registerFuel(HEU235, "heu_235", 1800, 60000, 2.5, 0.3, 2.0, 2.0, 2.5, 0.01, 0.05)

        LowGradeMOX = builder("low_grade_mox")
            .dust(3)
            .color(0x62C032).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(FissilePlutoniumDioxide, 1, GTMaterials.Uraninite, 19)
            .buildAndRegister()
            .setFormula("(U,Pu)O2", true)
        registerFuel(LowGradeMOX, "low_grade_mox", 1600, 50000, 1.5, 0.5, 2.2, 2.2, 2.60, 0.02, 0.1)

        HighGradeMOX = builder("high_grade_mox")
            .dust(3)
            .color(0x7EA432).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(FissilePlutoniumDioxide, 1, GTMaterials.Uraninite, 4)
            .buildAndRegister()
            .setFormula("(U,Pu)O2", true)
        registerFuel(HighGradeMOX, "high_grade_mox", 2000, 80000, 1.0, 0.5, 2.4, 2.4, 2.80, 0.02, 0.2)
    }

    private fun registerUnknownCompositionMaterials() {
        SpentUraniumFuelSolution = builder("spent_uranium_fuel_solution")
            .liquid()
            .color(0x384536)
            .buildAndRegister()

        RadonRichGasMixture = builder("radon_rich_gas_mixture")
            .gas()
            .color(0xd78dd9)
            .buildAndRegister()
    }

    private fun applyMaterialModifications(includeDistilledWaterCoolant: Boolean) {
        GTMaterials.Zirconium.setProperty<DustProperty?>(PropertyKey.DUST, DustProperty())
        GTMaterials.Hafnium.setProperty<DustProperty?>(PropertyKey.DUST, DustProperty())
        GTMaterials.Hafnium.addFlags(MaterialFlags.GENERATE_LONG_ROD)
        GTMaterials.Hafnium.setProperty<BlastProperty?>(PropertyKey.BLAST, BlastProperty(2227))
        GTMaterials.Plutonium239.getProperty(PropertyKey.ORE).setOreByProducts()
        // Restore legacy SC Uranium/Plutonium overrides (ElementMaterials.java).
        // In modern GTCEu the "uranium" material is GTMaterials.Uranium238 and the "plutonium" material is
        // GTMaterials.Plutonium239 (registered under those ids), so applying the legacy SC color/iconset to
        // them is the parity equivalent of overriding CEu's Uranium/Plutonium. Legacy values:
        //   uranium   -> color 0x32F032, METALLIC (legacy liquid 1405K already matches GTCEu's Uranium238)
        //   plutonium -> color 0xF03232, METALLIC
        // Conscious choice (stage-3 review): legacy had SEPARATE materials for natural-U and the U-238
        // isotope, each with its own palette (natural-U 0x32F032/METALLIC vs isotope 0x46FA46/SHINY).
        // Modern collapses both into the single GTMaterials.Uranium238 material, so only one palette can
        // win. We keep the natural-uranium 0x32F032/METALLIC look because DUO2 (depleted_uranium_dioxide,
        // color 0x335323) is derived from this Uranium238 and its visual identity depends on it; the
        // isotope's 0x46FA46/SHINY is sacrificed.
        GTMaterials.Uranium238.materialARGB = 0x32F032
        GTMaterials.Uranium238.materialIconSet = MaterialIconSet.METALLIC
        GTMaterials.Plutonium239.materialARGB = 0xF03232
        GTMaterials.Plutonium239.materialIconSet = MaterialIconSet.METALLIC
        GTMaterials.Salt.setProperty<FluidProperty?>(
            PropertyKey.FLUID,
            FluidProperty(FluidStorageKeys.LIQUID, FluidBuilder().translation("gregtech.fluid.molten"))
        )
        GTMaterials.StainlessSteel.addFlags(MaterialFlags.GENERATE_ROUND)

        val uraniniteFuel: FissionFuelProperty =
            FissionFuelProperty.builder(GTMaterials.Uraninite.resourceLocation, 1800, 60000, 2.4)
                .fastNeutronCaptureCrossSection(0.5)
                .slowNeutronCaptureCrossSection(1.0)
                .slowNeutronFissionCrossSection(1.0)
                .requiredNeutrons(1.0)
                .releasedNeutrons(2.5)
                .releasedHeatEnergy(0.01)
                .decayRate(0.001)
                .build()
        GTMaterials.Uraninite.setProperty<FissionFuelProperty?>(ScritPropertyKey.FISSION_FUEL, uraniniteFuel)
        FUEL_ITEM_ENTRIES.add(FuelItemEntry("uraninite", uraniniteFuel))

        if (includeDistilledWaterCoolant) {
            val distilledWaterCoolant = CoolantProperty(
                GTMaterials.DistilledWater, HighPressureSteam, FluidStorageKeys.LIQUID,
                2.0, 1000.0, 373.0, 2260000.0, 4168.0
            )
                .setAccumulatesHydrogen(true)
                .setSlowAbsorptionFactor(0.1875)
                .setFastAbsorptionFactor(0.0625)
            GTMaterials.DistilledWater.setProperty<CoolantProperty?>(ScritPropertyKey.COOLANT, distilledWaterCoolant)
        }

        GTMaterials.Graphite.setProperty<ModeratorProperty?>(
            ScritPropertyKey.MODERATOR, ModeratorProperty.builder()
                .maxTemperature(3650)
                .absorptionFactor(0.0625)
                .moderationFactor(3.0)
                .build()
        )
        GTMaterials.Graphite.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK)

        GTMaterials.Beryllium.setProperty<ModeratorProperty?>(
            ScritPropertyKey.MODERATOR, ModeratorProperty.builder()
                .maxTemperature(1500)
                .absorptionFactor(0.015625)
                .moderationFactor(5.0)
                .build()
        )
        GTMaterials.Beryllium.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK)
    }

    private fun registerFuel(
        material: Material, fuelItemKey: String?, maxTemperature: Int, duration: Int, neutronGenerationTime: Double,
        fastCapture: Double, slowCapture: Double, slowFission: Double, releasedNeutrons: Double,
        releasedHeatEnergy: Double, decayRate: Double
    ) {
        val property: FissionFuelProperty = FissionFuelProperty.builder(
            material.resourceLocation, maxTemperature, duration,
            neutronGenerationTime
        )
            .fastNeutronCaptureCrossSection(fastCapture)
            .slowNeutronCaptureCrossSection(slowCapture)
            .slowNeutronFissionCrossSection(slowFission)
            .fastNeutronFissionCrossSection(0.0)
            .requiredNeutrons(1.0)
            .releasedNeutrons(releasedNeutrons)
            .releasedHeatEnergy(releasedHeatEnergy)
            .decayRate(decayRate)
            .build()
        FUEL_ITEM_ENTRIES.add(FuelItemEntry(fuelItemKey, property))
        material.setProperty<FissionFuelProperty?>(ScritPropertyKey.FISSION_FUEL, property)
    }

    fun registerFuelItems() {
        for (entry in FUEL_ITEM_ENTRIES) {
            val fuelItems = ScritItems.NUCLEAR_FUEL_ITEMS[entry.fuelItemKey]
            if (fuelItems != null) {
                // Legacy parity (CommonProxy.java:124-133): every fuel emits only the hot depleted fuel rod.
                // The hot -> cold cooling happens via the spent-fuel-pool recipe, not direct emission.
                entry.property.setDepletedFuelSupplier { _: Double? ->
                    fuelItems.hotDepletedFuelRod.get().defaultInstance
                }
                    .setAllDepletedFuels {
                        List.of<ItemStack?>(fuelItems.hotDepletedFuelRod.get().defaultInstance)
                    }
                FissionFuelRegistry.registerFuel(fuelItems.fuelRod.get().defaultInstance, entry.property)
            } else {
                FissionFuelRegistry.registerFuel(entry.property)
            }
        }
        FUEL_ITEM_ENTRIES.clear()
    }

    fun registerCoolants() {
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (material.hasProperty<CoolantProperty?>(ScritPropertyKey.COOLANT)) {
                val property = material.getProperty<CoolantProperty>(ScritPropertyKey.COOLANT)
                val fluid =
                    material.getFluid(requireNotNull(property.coolantKey) { "Coolant fluid key for ${material.name} is not initialized" })
                if (fluid != null) {
                    CoolantRegistry.registerCoolant(fluid, property)
                }
            }
        }
    }

    fun registerModerators() {
        for (material in GTCEuAPI.materialManager.registeredMaterials) {
            if (material.hasProperty<ModeratorProperty?>(ScritPropertyKey.MODERATOR)) {
                registerModerator(material)
            }
        }
    }

    private fun registerModerator(material: Material) {
        val property = requireNotNull(material.getProperty<ModeratorProperty?>(ScritPropertyKey.MODERATOR)) {
            "${material.name} is missing its moderator property"
        }
        val block = requireNotNull(ChemicalHelper.getBlock(TagPrefix.block, material)) {
            "${material.name} has no storage block"
        }
        ModeratorRegistry.registerModerator(block, property)
    }

    private fun builder(name: String): Material.Builder {
        return Material.Builder(scId(name))
    }

    fun registry(): MaterialRegistry? {
        return registry
    }

    @JvmRecord
    private data class FuelItemEntry(
        val fuelItemKey: String?,
        val property: FissionFuelProperty
    )
}
