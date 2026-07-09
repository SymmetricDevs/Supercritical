package supercritical.api.unification.material;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.unification.material.properties.CoolantProperty;
import supercritical.api.unification.material.properties.FissionFuelProperty;
import supercritical.api.unification.material.properties.ModeratorProperty;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.common.SCConfigHolder;
import supercritical.common.registry.SCItems;

import java.util.ArrayList;
import java.util.List;

import static supercritical.api.util.SCUtility.scId;

public final class SCMaterials {

    private record FuelItemEntry(Material material, String fuelItemKey, FissionFuelProperty property) {}

    private static final List<FuelItemEntry> FUEL_ITEM_ENTRIES = new ArrayList<>();

    public static Material Uranium239;
    public static Material Neptunium235;
    public static Material Neptunium236;
    public static Material Neptunium237;
    public static Material Neptunium239;
    public static Material Plutonium238;
    public static Material Plutonium240;
    public static Material Plutonium242;
    public static Material Plutonium244;

    public static Material HighEnrichedUraniumDioxide;
    public static Material DepletedUraniumDioxide;
    public static Material HighPressureSteam;
    public static Material FissilePlutoniumDioxide;
    public static Material Zircaloy;
    public static Material LowEnrichedUraniumDioxide;
    public static Material Zircon;
    public static Material ZirconiumDioxide;
    public static Material ZirconiumTetrachloride;
    public static Material HafniumDioxide;
    public static Material HafniumTetrachloride;
    public static Material Inconel;
    public static Material HighEnrichedUraniumHexafluoride;
    public static Material BoronTrioxide;
    public static Material BoronCarbide;
    public static Material HighPressureHeavyWater;
    public static Material HeavyWater;

    public static Material SpentUraniumFuelSolution;
    public static Material RadonRichGasMixture;

    public static Material Corium;
    public static Material LEU235;
    public static Material HEU235;
    public static Material LowGradeMOX;
    public static Material HighGradeMOX;

    private static MaterialRegistry registry;

    private SCMaterials() {}

    @SubscribeEvent
    public static void createRegistry(MaterialRegistryEvent event) {
        registry = GTCEuAPI.materialManager.createRegistry("supercritical");
    }

    @SubscribeEvent
    public static void register(MaterialEvent event) {
        registerCoreMaterials();
        // Config values are not available during MaterialEvent; defer conditional material registration.
        registerElementMaterials();
        registerFirstDegreeMaterials();
        registerSecondDegreeMaterials();
        registerUnknownCompositionMaterials();
        applyMaterialModifications();
    }

    private static void registerCoreMaterials() {
        Corium = builder("corium")
                .liquid(new FluidBuilder()
                        .temperature(2500)
                        .density(8.0D)
                        .viscosity(10000))
                .color(0x7A6B50)
                .iconSet(MaterialIconSet.DULL)
                .flags(MaterialFlags.NO_UNIFICATION, MaterialFlags.STICKY)
                .buildAndRegister();
    }

    private static void registerElementMaterials() {
        Uranium239 = builder("uranium_239")
                .color(0x46FA46).iconSet(MaterialIconSet.SHINY)
                .element(SCGTAddon.Uranium239)
                .buildAndRegister();

        Neptunium235 = builder("neptunium_235")
                .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Neptunium235)
                .buildAndRegister();
        Neptunium236 = builder("neptunium_236")
                .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Neptunium236)
                .buildAndRegister();
        Neptunium237 = builder("neptunium_237")
                .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Neptunium237)
                .buildAndRegister();
        Neptunium239 = builder("neptunium_239")
                .color(0x284D7B).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Neptunium239)
                .buildAndRegister();

        Plutonium238 = builder("plutonium_238")
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Plutonium238)
                .buildAndRegister();
        Plutonium240 = builder("plutonium_240")
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Plutonium240)
                .buildAndRegister();
        Plutonium242 = builder("plutonium_242")
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Plutonium242)
                .buildAndRegister();
        Plutonium244 = builder("plutonium_244")
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(MaterialIconSet.METALLIC)
                .element(SCGTAddon.Plutonium244)
                .buildAndRegister();
    }

    private static void registerFirstDegreeMaterials() {
        HighEnrichedUraniumDioxide = builder("high_enriched_uranium_dioxide")
                .dust(3)
                .color(0x53E353).iconSet(MaterialIconSet.DULL)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Uranium235, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister()
                .setFormula("UO2", true);

        DepletedUraniumDioxide = builder("depleted_uranium_dioxide")
                .dust(3)
                .color(0x335323).iconSet(MaterialIconSet.DULL)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Uranium238, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister()
                .setFormula("UO2", true);

        HighPressureSteam = builder("high_pressure_steam")
                .gas(new FluidBuilder().temperature(500).customStill())
                .color(0xC4C4C4)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Hydrogen, 2, GTMaterials.Oxygen, 1)
                .buildAndRegister();

        FissilePlutoniumDioxide = builder("fissile_plutonium_dioxide")
                .dust(3)
                .color(0xF03232).iconSet(MaterialIconSet.DULL)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Plutonium239, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister();

        Zircaloy = builder("zircaloy")
                .ingot()
                .color(0x566570).iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.GENERATE_RING, MaterialFlags.GENERATE_PLATE)
                .components(GTMaterials.Zirconium, 16, GTMaterials.Tin, 2, GTMaterials.Chromium, 1)
                .blast(1700, BlastProperty.GasTier.LOW)
                .buildAndRegister();

        LowEnrichedUraniumDioxide = builder("low_enriched_uranium_dioxide")
                .dust()
                .color(0x43A333)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Uranium235, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister()
                .setFormula("UO2", true);

        Zircon = builder("zircon")
                .gem().ore()
                .color(0x6E0909)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Zirconium, 1, GTMaterials.Silicon, 1, GTMaterials.Oxygen, 4)
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        ZirconiumDioxide = builder("zirconium_dioxide")
                .dust()
                .color(0x689F9F)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Zirconium, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister();

        ZirconiumTetrachloride = builder("zirconium_tetrachloride")
                .dust()
                .color(0x689FBF)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Zirconium, 1, GTMaterials.Chlorine, 4)
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        HafniumDioxide = builder("hafnium_dioxide")
                .dust()
                .color(0x39393A)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Hafnium, 1, GTMaterials.Oxygen, 2)
                .buildAndRegister();

        HafniumTetrachloride = builder("hafnium_tetrachloride")
                .dust()
                .color(0x69699A)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Hafnium, 1, GTMaterials.Chlorine, 4)
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();

        Inconel = builder("inconel")
                .ingot().fluid()
                .color(0x7F8F75).iconSet(MaterialIconSet.SHINY)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_SPRING, MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Nickel, 5, GTMaterials.Chromium, 2, GTMaterials.Iron, 2, GTMaterials.Niobium, 1, GTMaterials.Molybdenum, 1)
                .blast(b -> b.temp(1610, BlastProperty.GasTier.MID).blastStats(2048, 200))
                .fluidPipeProperties(2010, 175, true, true, true, false)
                .buildAndRegister();

        HighEnrichedUraniumHexafluoride = builder("high_enriched_uranium_hexafluoride")
                .gas()
                .color(0x5BF93A)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Uranium235, 1, GTMaterials.Fluorine, 6)
                .buildAndRegister();

        BoronTrioxide = builder("boron_trioxide")
                .dust()
                .color(0xC1E9E1)
                .components(GTMaterials.Boron, 2, GTMaterials.Oxygen, 3)
                .iconSet(MaterialIconSet.METALLIC)
                .buildAndRegister();

        BoronCarbide = builder("boron_carbide")
                .ingot()
                .flags(MaterialFlags.GENERATE_ROD, MaterialFlags.DISABLE_DECOMPOSITION)
                .blast(2620)
                .color(0xC1E9C1)
                .components(GTMaterials.Boron, 4, GTMaterials.Carbon, 1)
                .iconSet(MaterialIconSet.METALLIC)
                .buildAndRegister();

        HighPressureHeavyWater = builder("high_pressure_heavy_water")
                .gas(new FluidBuilder().temperature(500))
                .color(0xCCD9F0)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(GTMaterials.Deuterium, 2, GTMaterials.Oxygen, 1)
                .buildAndRegister();

        HeavyWater = builder("heavy_water")
                .fluid()
                .color(0x3673D6)
                .components(GTMaterials.Deuterium, 2, GTMaterials.Oxygen, 1)
                .buildAndRegister();
        var heavyWaterCoolant = new CoolantProperty(HeavyWater, HighPressureHeavyWater, FluidStorageKeys.LIQUID,
                4D, 1000D, 374.4D, 2064000D, 4228D).setAccumulatesHydrogen(true);
        HeavyWater.setProperty(SCPropertyKey.COOLANT, heavyWaterCoolant);
    }

    private static void registerSecondDegreeMaterials() {
        LEU235 = builder("leu_235")
                .dust(3)
                .color(0x232323).iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 19)
                .buildAndRegister()
                .setFormula("UO2", true);
        registerFuel(LEU235, "leu_235", 1500, 75000, 3.5D, 0.4D, 1.8D, 1.8D, 2.5D, 0.01D, 0.025D);

        HEU235 = builder("heu_235")
                .dust(3)
                .color(0x424845).iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(HighEnrichedUraniumDioxide, 1, DepletedUraniumDioxide, 4)
                .buildAndRegister()
                .setFormula("UO2", true);
        registerFuel(HEU235, "heu_235", 1800, 60000, 2.5D, 0.3D, 2D, 2D, 2.5D, 0.01D, 0.05D);

        LowGradeMOX = builder("low_grade_mox")
                .dust(3)
                .color(0x62C032).iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, GTMaterials.Uraninite, 19)
                .buildAndRegister()
                .setFormula("(U,Pu)O2", true);
        registerFuel(LowGradeMOX, "low_grade_mox", 1600, 50000, 1.5D, 0.5D, 2.2D, 2.2D, 2.60D, 0.02D, 0.1D);

        HighGradeMOX = builder("high_grade_mox")
                .dust(3)
                .color(0x7EA432).iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .components(FissilePlutoniumDioxide, 1, GTMaterials.Uraninite, 4)
                .buildAndRegister()
                .setFormula("(U,Pu)O2", true);
        registerFuel(HighGradeMOX, "high_grade_mox", 2000, 80000, 1D, 0.5D, 2.4D, 2.4D, 2.80D, 0.02D, 0.2D);
    }

    private static void registerUnknownCompositionMaterials() {
        SpentUraniumFuelSolution = builder("spent_uranium_fuel_solution")
                .liquid()
                .color(0x384536)
                .buildAndRegister();

        RadonRichGasMixture = builder("radon_rich_gas_mixture")
                .gas()
                .color(0xd78dd9)
                .buildAndRegister();
    }

    private static void applyMaterialModifications() {
        GTMaterials.Zirconium.setProperty(PropertyKey.DUST, new DustProperty());
        GTMaterials.Hafnium.addFlags(MaterialFlags.GENERATE_LONG_ROD);
        GTMaterials.Hafnium.setProperty(PropertyKey.BLAST, new com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty(2227));
        GTMaterials.Salt.setProperty(PropertyKey.FLUID,
                new FluidProperty(FluidStorageKeys.LIQUID, new FluidBuilder().translation("gregtech.fluid.molten")));
        GTMaterials.StainlessSteel.addFlags(MaterialFlags.GENERATE_ROUND);

        var uraniniteFuel = FissionFuelProperty.builder(GTMaterials.Uraninite.getResourceLocation(), 1800, 60000, 2.4D)
                .fastNeutronCaptureCrossSection(0.5D)
                .slowNeutronCaptureCrossSection(1D)
                .slowNeutronFissionCrossSection(1D)
                .requiredNeutrons(1D)
                .releasedNeutrons(2.5D)
                .releasedHeatEnergy(0.01D)
                .decayRate(0.001D)
                .build();
        GTMaterials.Uraninite.setProperty(SCPropertyKey.FISSION_FUEL, uraniniteFuel);
        FUEL_ITEM_ENTRIES.add(new FuelItemEntry(GTMaterials.Uraninite, "uraninite", uraniniteFuel));

        var distilledWaterCoolant = new CoolantProperty(GTMaterials.DistilledWater, HighPressureSteam, FluidStorageKeys.LIQUID,
                2D, 1000D, 373D, 2260000D, 4168D)
                .setAccumulatesHydrogen(true)
                .setSlowAbsorptionFactor(0.1875D)
                .setFastAbsorptionFactor(0.0625D);
        GTMaterials.DistilledWater.setProperty(SCPropertyKey.COOLANT, distilledWaterCoolant);

        GTMaterials.Graphite.setProperty(SCPropertyKey.MODERATOR, ModeratorProperty.builder()
                .maxTemperature(3650)
                .absorptionFactor(0.0625D)
                .moderationFactor(3D)
                .build());
        GTMaterials.Graphite.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);

        GTMaterials.Beryllium.setProperty(SCPropertyKey.MODERATOR, ModeratorProperty.builder()
                .maxTemperature(1500)
                .absorptionFactor(0.015625D)
                .moderationFactor(5D)
                .build());
        GTMaterials.Beryllium.addFlags(MaterialFlags.FORCE_GENERATE_BLOCK);
    }

    private static void registerFuel(Material material, String fuelItemKey, int maxTemperature, int duration, double neutronGenerationTime,
                                     double fastCapture, double slowCapture, double slowFission, double releasedNeutrons,
                                     double releasedHeatEnergy, double decayRate) {
        var property = FissionFuelProperty.builder(material.getResourceLocation(), maxTemperature, duration,
                        neutronGenerationTime)
                .fastNeutronCaptureCrossSection(fastCapture)
                .slowNeutronCaptureCrossSection(slowCapture)
                .slowNeutronFissionCrossSection(slowFission)
                .fastNeutronFissionCrossSection(0D)
                .requiredNeutrons(1D)
                .releasedNeutrons(releasedNeutrons)
                .releasedHeatEnergy(releasedHeatEnergy)
                .decayRate(decayRate)
                .build();
        FUEL_ITEM_ENTRIES.add(new FuelItemEntry(material, fuelItemKey, property));
        material.setProperty(SCPropertyKey.FISSION_FUEL, property);
    }

    public static void registerFuelItems() {
        for (var entry : FUEL_ITEM_ENTRIES) {
            var fuelItems = SCItems.NUCLEAR_FUEL_ITEMS.get(entry.fuelItemKey);
            if (fuelItems != null) {
                entry.property.setDepletedFuelSupplier(thermalRatio -> (thermalRatio > 0.5D
                                        ? fuelItems.hotDepletedFuelRod().get()
                                        : fuelItems.depletedFuelRod().get()).getDefaultInstance())
                        .setAllDepletedFuels(() -> List.of(
                                fuelItems.depletedFuelRod().get().getDefaultInstance(),
                                fuelItems.hotDepletedFuelRod().get().getDefaultInstance()));
                FissionFuelRegistry.registerFuel(fuelItems.fuelRod().get().getDefaultInstance(), entry.property);
            } else {
                FissionFuelRegistry.registerFuel(entry.property);
            }
        }
        // For materials registered outside the fuel registry, just add them if they have a property.
        registerUraniniteFuel();
        FUEL_ITEM_ENTRIES.clear();
    }

    private static void registerUraniniteFuel() {
        var property = (FissionFuelProperty) GTMaterials.Uraninite.getProperty(SCPropertyKey.FISSION_FUEL);
        if (property == null) return;
        var fuelItems = SCItems.NUCLEAR_FUEL_ITEMS.get("uraninite");
        if (fuelItems != null) {
            property.setDepletedFuelSupplier(thermalRatio -> (thermalRatio > 0.5D
                                ? fuelItems.hotDepletedFuelRod().get()
                                : fuelItems.depletedFuelRod().get()).getDefaultInstance())
                    .setAllDepletedFuels(() -> List.of(
                            fuelItems.depletedFuelRod().get().getDefaultInstance(),
                            fuelItems.hotDepletedFuelRod().get().getDefaultInstance()));
            FissionFuelRegistry.registerFuel(fuelItems.fuelRod().get().getDefaultInstance(), property);
        } else {
            FissionFuelRegistry.registerFuel(property);
        }
    }

    public static void registerCoolants() {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(SCPropertyKey.COOLANT)) {
                CoolantProperty property = material.getProperty(SCPropertyKey.COOLANT);
                Fluid fluid = material.getFluid(property.getCoolantKey());
                if (fluid != null) {
                    CoolantRegistry.registerCoolant(fluid, property);
                }
            }
        }
    }

    private static Material.Builder builder(String name) {
        return new Material.Builder(scId(name));
    }

    public static MaterialRegistry registry() {
        return registry;
    }
}
