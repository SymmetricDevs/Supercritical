package supercritical.api.unification.material

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry
import com.gregtechceu.gtceu.api.fluids.FluidBuilder
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.common.data.GTMaterials
import net.minecraft.world.item.ItemStack
import net.minecraftforge.eventbus.api.SubscribeEvent
import supercritical.SCGTAddon
import supercritical.api.nuclear.fission.CoolantRegistry
import supercritical.api.nuclear.fission.FissionFuelRegistry
import supercritical.api.unification.material.properties.CoolantProperty
import supercritical.api.unification.material.properties.FissionFuelProperty
import supercritical.api.unification.material.properties.ModeratorProperty
import supercritical.api.unification.material.properties.SCPropertyKey
import supercritical.api.util.SCUtility
import supercritical.common.registry.SCItems
import java.util.List
import java.util.function.Function
import java.util.function.Supplier
import java.util.function.UnaryOperator

object SCMaterials {
    private val FUEL_ITEM_ENTRIES: MutableList<FuelItemEntry> = ArrayList<FuelItemEntry>()

    var Uranium239: Material? = null
    var Neptunium235: Material? = null
    var Neptunium236: Material? = null
    var Neptunium237: Material? = null
    var Neptunium239: Material? = null
    var Plutonium238: Material? = null
    var Plutonium240: Material? = null
    var Plutonium242: Material? = null
    var Plutonium244: Material? = null

    var HighEnrichedUraniumDioxide: Material? = null
    var DepletedUraniumDioxide: Material? = null
    var HighPressureSteam: Material? = null
    var FissilePlutoniumDioxide: Material? = null
    var Zircaloy: Material? = null
    var LowEnrichedUraniumDioxide: Material? = null
    var Zircon: Material? = null
    var ZirconiumDioxide: Material? = null
    var ZirconiumTetrachloride: Material? = null
    var HafniumDioxide: Material? = null
    var HafniumTetrachloride: Material? = null
    var Inconel: Material? = null
    var HighEnrichedUraniumHexafluoride: Material? = null
    var BoronTrioxide: Material? = null
    var BoronCarbide: Material? = null
    var HighPressureHeavyWater: Material? = null
    var HeavyWater: Material? = null

    var SpentUraniumFuelSolution: Material? = null
    var RadonRichGasMixture: Material? = null

    var Corium: Material? = null
    var LEU235: Material? = null
    var HEU235: Material? = null
    var LowGradeMOX: Material? = null
    var HighGradeMOX: Material? = null

    private var registry: MaterialRegistry? = null

    @SubscribeEvent
    fun createRegistry(event: MaterialRegistryEvent?) {
        registry = GTCEuAPI.materialManager.createRegistry("supercritical")
    }

    @SubscribeEvent
    fun register(event: MaterialEvent?) {
        registerCoreMaterials()
        // Config values are not available during MaterialEvent; defer conditional material registration.
        registerElementMaterials()
        registerFirstDegreeMaterials()
        registerSecondDegreeMaterials()
        registerUnknownCompositionMaterials()
        applyMaterialModifications()
    }

    private fun registerCoreMaterials() {
        Corium = builder("corium")
            .liquid(
                FluidBuilder()
                    .temperature(2500)
                    .density(8.0)
                    .viscosity(10000)
            )
            .color(0x7A6B50)
            .iconSet(MaterialIconSet.DULL)
            .flags(MaterialFlags.NO_UNIFICATION, MaterialFlags.STICKY)
            .buildAndRegister()
    }

    private fun registerElementMaterials() {
        Uranium239 = builder("uranium_239")
            .color(0x46FA46).iconSet(MaterialIconSet.SHINY)
            .element(SCGTAddon.Companion.Uranium239)
            .buildAndRegister()

        Neptunium235 = builder("neptunium_235")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Neptunium235)
            .buildAndRegister()
        Neptunium236 = builder("neptunium_236")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Neptunium236)
            .buildAndRegister()
        Neptunium237 = builder("neptunium_237")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Neptunium237)
            .buildAndRegister()
        Neptunium239 = builder("neptunium_239")
            .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Neptunium239)
            .buildAndRegister()

        Plutonium238 = builder("plutonium_238")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Plutonium238)
            .buildAndRegister()
        Plutonium240 = builder("plutonium_240")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Plutonium240)
            .buildAndRegister()
        Plutonium242 = builder("plutonium_242")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Plutonium242)
            .buildAndRegister()
        Plutonium244 = builder("plutonium_244")
            .liquid(FluidBuilder().temperature(913))
            .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
            .element(SCGTAddon.Companion.Plutonium244)
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
            .blast(UnaryOperator { b: BlastProperty.Builder? ->
                b!!.temp(1610, BlastProperty.GasTier.MID).blastStats(2048, 200)
            })
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
        HeavyWater!!.setProperty<CoolantProperty?>(SCPropertyKey.COOLANT, heavyWaterCoolant)
    }

    private fun registerSecondDegreeMaterials() {
        LEU235 = builder("leu_235")
            .dust(3)
            .color(0x232323).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 19)
            .buildAndRegister()
            .setFormula("UO2", true)
        SCMaterials.registerFuel(LEU235!!, "leu_235", 1500, 75000, 3.5, 0.4, 1.8, 1.8, 2.5, 0.01, 0.025)

        HEU235 = builder("heu_235")
            .dust(3)
            .color(0x424845).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
            .buildAndRegister()
            .setFormula("UO2", true)
        SCMaterials.registerFuel(HEU235!!, "heu_235", 1800, 60000, 2.5, 0.3, 2.0, 2.0, 2.5, 0.01, 0.05)

        LowGradeMOX = builder("low_grade_mox")
            .dust(3)
            .color(0x62C032).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(FissilePlutoniumDioxide, 1, GTMaterials.Uraninite, 19)
            .buildAndRegister()
            .setFormula("(U,Pu)O2", true)
        SCMaterials.registerFuel(LowGradeMOX!!, "low_grade_mox", 1600, 50000, 1.5, 0.5, 2.2, 2.2, 2.60, 0.02, 0.1)

        HighGradeMOX = builder("high_grade_mox")
            .dust(3)
            .color(0x7EA432).iconSet(MaterialIconSet.METALLIC)
            .flags(MaterialFlags.DISABLE_DECOMPOSITION)
            .components(FissilePlutoniumDioxide, 1, GTMaterials.Uraninite, 4)
            .buildAndRegister()
            .setFormula("(U,Pu)O2", true)
        SCMaterials.registerFuel(HighGradeMOX!!, "high_grade_mox", 2000, 80000, 1.0, 0.5, 2.4, 2.4, 2.80, 0.02, 0.2)
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

    private fun applyMaterialModifications() {
        GTMaterials.Zirconium.setProperty<DustProperty?>(PropertyKey.DUST, DustProperty())
        GTMaterials.Hafnium.addFlags(MaterialFlags.GENERATE_LONG_ROD)
        GTMaterials.Hafnium.setProperty<BlastProperty?>(PropertyKey.BLAST, BlastProperty(2227))
        GTMaterials.Salt.setProperty<FluidProperty?>(
            PropertyKey.FLUID,
            FluidProperty(FluidStorageKeys.LIQUID, FluidBuilder().translation("gregtech.fluid.molten"))
        )
        GTMaterials.StainlessSteel.addFlags(MaterialFlags.GENERATE_ROUND)

        val uraniniteFuel: FissionFuelProperty? =
            FissionFuelProperty.Companion.builder(GTMaterials.Uraninite.getResourceLocation(), 1800, 60000, 2.4)
                .fastNeutronCaptureCrossSection(0.5)
                .slowNeutronCaptureCrossSection(1.0)
                .slowNeutronFissionCrossSection(1.0)
                .requiredNeutrons(1.0)
                .releasedNeutrons(2.5)
                .releasedHeatEnergy(0.01)
                .decayRate(0.001)
                .build()
        GTMaterials.Uraninite.setProperty<FissionFuelProperty?>(SCPropertyKey.FISSION_FUEL, uraniniteFuel)
        FUEL_ITEM_ENTRIES.add(FuelItemEntry(GTMaterials.Uraninite, "uraninite", uraniniteFuel))

        val distilledWaterCoolant = CoolantProperty(
            GTMaterials.DistilledWater, HighPressureSteam, FluidStorageKeys.LIQUID,
            2.0, 1000.0, 373.0, 2260000.0, 4168.0
        )
            .setAccumulatesHydrogen(true)
            .setSlowAbsorptionFactor(0.1875)
            .setFastAbsorptionFactor(0.0625)
        GTMaterials.DistilledWater.setProperty<CoolantProperty?>(SCPropertyKey.COOLANT, distilledWaterCoolant)

        GTMaterials.Graphite.setProperty<ModeratorProperty?>(
            SCPropertyKey.MODERATOR, ModeratorProperty.Companion.builder()
                .maxTemperature(3650)
                .absorptionFactor(0.0625)
                .moderationFactor(3.0)
                .build()
        )
        GTMaterials.Graphite.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK)

        GTMaterials.Beryllium.setProperty<ModeratorProperty?>(
            SCPropertyKey.MODERATOR, ModeratorProperty.Companion.builder()
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
        val property: FissionFuelProperty? = FissionFuelProperty.Companion.builder(
            material.getResourceLocation(), maxTemperature, duration,
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
        FUEL_ITEM_ENTRIES.add(FuelItemEntry(material, fuelItemKey, property))
        material.setProperty<FissionFuelProperty?>(SCPropertyKey.FISSION_FUEL, property)
    }

    fun registerFuelItems() {
        for (entry in FUEL_ITEM_ENTRIES) {
            val fuelItems = SCItems.NUCLEAR_FUEL_ITEMS.get(entry.fuelItemKey)
            if (fuelItems != null) {
                entry.property!!.setDepletedFuelSupplier(Function { thermalRatio: Double? ->
                    (if (thermalRatio!! > 0.5)
                        fuelItems.hotDepletedFuelRod.get()
                    else
                        fuelItems.depletedFuelRod.get()).getDefaultInstance()
                })
                    .setAllDepletedFuels(Supplier {
                        List.of<ItemStack?>(
                            fuelItems.depletedFuelRod.get().getDefaultInstance(),
                            fuelItems.hotDepletedFuelRod.get().getDefaultInstance()
                        )
                    })
                FissionFuelRegistry.registerFuel(fuelItems.fuelRod.get().getDefaultInstance(), entry.property)
            } else {
                FissionFuelRegistry.registerFuel(entry.property)
            }
        }
        // For materials registered outside the fuel registry, just add them if they have a property.
        registerUraniniteFuel()
        FUEL_ITEM_ENTRIES.clear()
    }

    private fun registerUraniniteFuel() {
        val property = GTMaterials.Uraninite.getProperty<FissionFuelProperty?>(SCPropertyKey.FISSION_FUEL)
        if (property == null) return
        val fuelItems = SCItems.NUCLEAR_FUEL_ITEMS.get("uraninite")
        if (fuelItems != null) {
            property.setDepletedFuelSupplier(Function { thermalRatio: Double? ->
                (if (thermalRatio!! > 0.5)
                    fuelItems.hotDepletedFuelRod.get()
                else
                    fuelItems.depletedFuelRod.get()).getDefaultInstance()
            })
                .setAllDepletedFuels(Supplier {
                    List.of<ItemStack?>(
                        fuelItems.depletedFuelRod.get().getDefaultInstance(),
                        fuelItems.hotDepletedFuelRod.get().getDefaultInstance()
                    )
                })
            FissionFuelRegistry.registerFuel(fuelItems.fuelRod.get().getDefaultInstance(), property)
        } else {
            FissionFuelRegistry.registerFuel(property)
        }
    }

    fun registerCoolants() {
        for (material in GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty<CoolantProperty?>(SCPropertyKey.COOLANT)) {
                val property = material.getProperty<CoolantProperty>(SCPropertyKey.COOLANT)
                val fluid = material.getFluid(property.getCoolantKey())
                if (fluid != null) {
                    CoolantRegistry.registerCoolant(fluid, property)
                }
            }
        }
    }

    private fun builder(name: String?): Material.Builder {
        return Material.Builder(SCUtility.scId(name))
    }

    fun registry(): MaterialRegistry? {
        return registry
    }

    @JvmRecord
    private data class FuelItemEntry(
        val material: Material?,
        val fuelItemKey: String?,
        val property: FissionFuelProperty?
    )
}
