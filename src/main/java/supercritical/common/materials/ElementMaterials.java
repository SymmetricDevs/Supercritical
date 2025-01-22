package supercritical.common.materials;

import static gregtech.api.unification.material.Materials.EXT_METAL;
import static gregtech.api.unification.material.info.MaterialIconSet.METALLIC;
import static gregtech.api.unification.material.info.MaterialIconSet.SHINY;
import static supercritical.api.unification.material.SCMaterials.*;
import static supercritical.api.util.SCUtility.scId;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import supercritical.api.unification.SCElements;

/*
 * Ranges 1-499
 */
public class ElementMaterials {

    public static void register() {
        Uranium = new Material.Builder(1, scId("uranium"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x32F032).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.U)
                .build();

        Uranium239 = new Material.Builder(2, scId("uranium_239"))
                .color(0x46FA46).iconSet(SHINY)
                .element(SCElements.U239)
                .build();

        Neptunium235 = new Material.Builder(3, scId("neptunium_235"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(SCElements.Np235)
                .build();

        Neptunium236 = new Material.Builder(4, scId("neptunium_236"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(SCElements.Np236)
                .build();

        Neptunium237 = new Material.Builder(5, scId("neptunium_237"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(SCElements.Np237)
                .build();

        Neptunium239 = new Material.Builder(6, scId("neptunium_239"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(SCElements.Np239)
                .build();

        Plutonium238 = new Material.Builder(7, scId("plutonium_238"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(SCElements.Pu238)
                .build();

        Plutonium240 = new Material.Builder(8, scId("plutonium_240"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(SCElements.Pu240)
                .build();

        Plutonium242 = new Material.Builder(9, scId("plutonium_242"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(SCElements.Pu242)
                .build();

        Plutonium244 = new Material.Builder(10, scId("plutonium_244"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(SCElements.Pu244)
                .build();

        Plutonium = new Material.Builder(11, scId("plutonium"))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu)
                .build();
    }
}
