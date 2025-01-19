package gregicality.nuclear.common.materials;

import gregicality.nuclear.api.unification.material.properties.CoolantProperty;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregtech.api.GTValues;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.*;
import static gregicality.nuclear.api.util.GCYNUtility.gcynId;
import static gregtech.api.GTValues.EV;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class GCYNFirstDegreeMaterials {

    public static void register() {

        HighEnrichedUraniumDioxide = new Material.Builder(452, gcynId("high_enriched_uranium_dioxide"))
                .dust(3)
                .color(0x53E353).iconSet(DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        DepletedUraniumDioxide = new Material.Builder(453, gcynId("depleted_uranium_dioxide"))
                .dust(3)
                .color(0x335323).iconSet(DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        HighPressureSteam = new Material.Builder(454, gcynId("high_pressure_steam"))
                .gas(new FluidBuilder()
                        .temperature(500)
                        .customStill())
                .color(0xC4C4C4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        FissilePlutoniumDioxide = new Material.Builder(455, gcynId("fissile_plutonium_dioxide"))
                .dust(3)
                .color(0xF03232).iconSet(DULL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Plutonium, 1, Oxygen, 2)
                .build();

        Zircaloy = new Material.Builder(456, gcynId("zircaloy"))
                .ingot()
                .color(0x566570).iconSet(METALLIC)
                .flags(GENERATE_RING, GENERATE_PLATE)
                .components(Zirconium, 16, Tin, 2, Chrome, 1)
                .blast(1700, BlastProperty.GasTier.LOW)
                .build();

        LowEnrichedUraniumDioxide = new Material.Builder(457, gcynId("low_enriched_uranium_dioxide"))
                .dust()
                .color(0x43A333)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        Zircon = new Material.Builder(458, gcynId("zircon"))
                .gem().ore()
                .color(0x6E0909)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zirconium, 1, Silicon, 1, Oxygen, 4)
                .iconSet(SHINY)
                .build();


        ZirconiumDioxide = new Material.Builder(459, gcynId("zirconium_dioxide"))
                .dust()
                .color(0x689F9F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zirconium, 1, Oxygen, 2)
                .build();

        ZirconiumTetrachloride = new Material.Builder(460, gcynId("zirconium_tetrachloride"))
                .dust()
                .color(0x689FBF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zirconium, 1, Chlorine, 4)
                .iconSet(SHINY)
                .build();

        HafniumDioxide = new Material.Builder(461, gcynId("hafnium_dioxide"))
                .dust()
                .color(0x39393A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hafnium, 1, Oxygen, 2)
                .build();

        HafniumTetrachloride = new Material.Builder(462, gcynId("hafnium_tetrachloride"))
                .dust()
                .color(0x69699A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hafnium, 1, Chlorine, 4)
                .iconSet(SHINY)
                .build();

        Inconel = new Material.Builder(463, gcynId("inconel"))
                .ingot().fluid()
                .color(0x7F8F75).iconSet(SHINY)
                .flags(GENERATE_DOUBLE_PLATE, GENERATE_SPRING, DISABLE_DECOMPOSITION)
                .components(Nickel, 5, Chrome, 2, Iron, 2, Niobium, 1, Molybdenum, 1)
                .blast(b -> b.temp(1610, BlastProperty.GasTier.MID).blastStats(GTValues.VA[EV], 200))
                .fluidPipeProperties(2010, 175, true, true, true, false)
                .build();

        HighEnrichedUraniumHexafluoride = new Material.Builder(464, gcynId("high_enriched_uranium_hexafluoride"))
                .gas()
                .color(0x5BF93A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Fluorine, 6)
                .build();

        BoronTrioxide = new Material.Builder(465, gcynId("boron_trioxide"))
                .dust()
                .color(0xC1E9E1)
                .components(Boron, 2, Oxygen, 3)
                .iconSet(METALLIC)
                .build();

        BoronCarbide = new Material.Builder(466, gcynId("boron_carbide"))
                .ingot()
                .flags(GENERATE_ROD, DISABLE_DECOMPOSITION)
                .blast(2620)
                .color(0xC1E9C1)
                .components(Boron, 4, Carbon, 1)
                .iconSet(METALLIC)
                .build();

        HighPressureHeavyWater = new Material.Builder(468, gcynId("high_pressure_heavy_water"))
                .gas(new FluidBuilder().temperature(500))
                .color(0xCCD9F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Deuterium, 2, Oxygen, 1)
                .build();

        HeavyWater = new Material.Builder(467, gcynId("heavy_water"))
                .fluid()
                .color(0x3673D6)
                .components(Deuterium, 2, Oxygen, 1)
                .build();

        HeavyWater.setProperty(GCYNPropertyKey.COOLANT,
                new CoolantProperty(HeavyWater, HighPressureHeavyWater, FluidStorageKeys.LIQUID, 4., 1000,
                        374.4, 2064000, 4228.)
                        .setAccumulatesHydrogen(true));

    }
}
