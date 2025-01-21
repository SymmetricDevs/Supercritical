package gregicality.nuclear.common.materials;

import static gregicality.nuclear.api.unification.material.GCYNMaterials.*;
import static gregicality.nuclear.api.util.GCYNUtility.gcynId;
import static gregtech.api.unification.material.Materials.EXT_METAL;
import static gregtech.api.unification.material.info.MaterialIconSet.METALLIC;
import static gregtech.api.unification.material.info.MaterialIconSet.SHINY;

import gregicality.nuclear.api.unification.GCYNElements;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;

public class GCYNElementMaterials {

    public static void register() {
        Uranium = new Material.Builder(131, gcynId("uranium"))
                .ingot(3)
                .liquid(new FluidBuilder().temperature(1405))
                .color(0x32F032).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.U)
                .build();

        Uranium239 = new Material.Builder(132, gcynId("uranium_239"))
                .color(0x46FA46).iconSet(SHINY)
                .element(GCYNElements.U239)
                .build();

        Neptunium235 = new Material.Builder(133, gcynId("neptunium_235"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(GCYNElements.Np235)
                .build();

        Neptunium236 = new Material.Builder(134, gcynId("neptunium_236"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(GCYNElements.Np236)
                .build();

        Neptunium237 = new Material.Builder(135, gcynId("neptunium_237"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(GCYNElements.Np237)
                .build();

        Neptunium239 = new Material.Builder(136, gcynId("neptunium_239"))
                .color(0x284D7B).iconSet(METALLIC)
                .element(GCYNElements.Np239)
                .build();

        Plutonium238 = new Material.Builder(137, gcynId("plutonium_238"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(GCYNElements.Pu238)
                .build();

        Plutonium240 = new Material.Builder(138, gcynId("plutonium_240"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(GCYNElements.Pu240)
                .build();

        Plutonium242 = new Material.Builder(139, gcynId("plutonium_242"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(GCYNElements.Pu242)
                .build();

        Plutonium244 = new Material.Builder(140, gcynId("plutonium_244"))
                .liquid(new FluidBuilder().temperature(913))
                .color(0xF03232).iconSet(METALLIC)
                .element(GCYNElements.Pu244)
                .build();

        Plutonium = new Material.Builder(141, gcynId("plutonium"))
                .color(0xF03232).iconSet(METALLIC)
                .element(Elements.Pu)
                .build();
    }
}
