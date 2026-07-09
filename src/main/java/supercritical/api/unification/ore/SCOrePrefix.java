
package supercritical.api.unification.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import net.minecraft.network.chat.Component;
import supercritical.api.unification.material.info.SCMaterialIconType;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.api.unification.tag.TagPrefixExtension;

import java.util.List;
import java.util.function.BiConsumer;

public final class SCOrePrefix {

    public static final TagPrefix fuelRod = new TagPrefix("fuelRod")
            .materialIconType(SCMaterialIconType.fuelRod)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel)
            .tooltip(radioactiveTooltip());

    public static final TagPrefix fuelRodDepleted = new TagPrefix("fuelRodDepleted")
            .materialIconType(SCMaterialIconType.fuelRodDepleted)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel)
            .tooltip(radioactiveTooltip());

    public static final TagPrefix fuelRodHotDepleted = new TagPrefix("fuelRodHotDepleted")
            .materialIconType(SCMaterialIconType.fuelRodHotDepleted)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel)
            .tooltip(radioactiveTooltip());

    public static final TagPrefix fuelPelletRaw = new TagPrefix("fuelPelletRaw")
            .materialIconType(SCMaterialIconType.fuelPelletRaw)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel)
            .tooltip(radioactiveTooltip());

    public static final TagPrefix fuelPellet = new TagPrefix("fuelPellet")
            .materialIconType(SCMaterialIconType.fuelPellet)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel)
            .tooltip(radioactiveTooltip());

    public static final TagPrefix fuelPelletDepleted = new TagPrefix("fuelPelletDepleted")
            .materialIconType(SCMaterialIconType.fuelPelletDepleted)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel)
            .tooltip(radioactiveTooltip());

    public static final TagPrefix dustSpentFuel = new TagPrefix("dustSpentFuel")
            .materialIconType(SCMaterialIconType.dustSpentFuel)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel);

    public static final TagPrefix dustBredFuel = new TagPrefix("dustBredFuel")
            .materialIconType(SCMaterialIconType.dustBredFuel)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel);

    public static final TagPrefix dustFissionByproduct = new TagPrefix("dustFissionByproduct")
            .materialIconType(SCMaterialIconType.dustFissionByproduct)
            .unificationEnabled(true)
            .generateItem(true)
            .generationCondition(SCOrePrefix::hasFissionFuel);

    public static void init() {
        TagPrefixExtension.setRadiationDamageFunction(fuelRod, neutrons -> neutrons / 10e23D);
        TagPrefixExtension.setRadiationDamageFunction(fuelPelletRaw, neutrons -> neutrons / 160e23D);
        TagPrefixExtension.setRadiationDamageFunction(fuelPellet, neutrons -> neutrons / 160e23D);
        TagPrefixExtension.setRadiationDamageFunction(fuelRodDepleted, neutrons -> neutrons / 1.5e23D);
        TagPrefixExtension.setRadiationDamageFunction(fuelRodHotDepleted, neutrons -> neutrons / 1e23D);
        TagPrefixExtension.setRadiationDamageFunction(fuelPelletDepleted, neutrons -> neutrons / 24e23D);
    }

    private SCOrePrefix() {}

    private static boolean hasFissionFuel(Material material) {
        return material.hasProperty(SCPropertyKey.FISSION_FUEL);
    }

    private static BiConsumer<Material, List<Component>> radioactiveTooltip() {
        return (material, tooltip) -> tooltip.add(Component.translatable("metaitem.nuclear.tooltip.radioactive"));
    }
}
