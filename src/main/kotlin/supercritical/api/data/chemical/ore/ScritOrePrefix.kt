package supercritical.api.data.chemical.ore

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import net.minecraft.network.chat.Component
import supercritical.api.data.chemical.material.info.ScritMaterialIconType
import supercritical.api.data.chemical.material.property.FissionFuelProperty
import supercritical.api.data.chemical.material.property.ScritPropertyKey
import supercritical.api.data.chemical.tag.TagPrefixExtension
import java.util.function.BiConsumer

object ScritOrePrefix {
    val fuelRod: TagPrefix = TagPrefix("fuelRod")
        .materialIconType(ScritMaterialIconType.fuelRod)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }
        .tooltip(radioactiveTooltip())

    val fuelRodDepleted: TagPrefix = TagPrefix("fuelRodDepleted")
        .materialIconType(ScritMaterialIconType.fuelRodDepleted)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }
        .tooltip(radioactiveTooltip())

    val fuelRodHotDepleted: TagPrefix = TagPrefix("fuelRodHotDepleted")
        .materialIconType(ScritMaterialIconType.fuelRodHotDepleted)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }
        .tooltip(radioactiveTooltip())

    val fuelPelletRaw: TagPrefix = TagPrefix("fuelPelletRaw")
        .materialIconType(ScritMaterialIconType.fuelPelletRaw)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }
        .tooltip(radioactiveTooltip())

    val fuelPellet: TagPrefix = TagPrefix("fuelPellet")
        .materialIconType(ScritMaterialIconType.fuelPellet)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }
        .tooltip(radioactiveTooltip())

    val fuelPelletDepleted: TagPrefix = TagPrefix("fuelPelletDepleted")
        .materialIconType(ScritMaterialIconType.fuelPelletDepleted)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }
        .tooltip(radioactiveTooltip())

    val dustSpentFuel: TagPrefix = TagPrefix("dustSpentFuel")
        .materialIconType(ScritMaterialIconType.dustSpentFuel)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }

    val dustBredFuel: TagPrefix = TagPrefix("dustBredFuel")
        .materialIconType(ScritMaterialIconType.dustBredFuel)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }

    val dustFissionByproduct: TagPrefix = TagPrefix("dustFissionByproduct")
        .materialIconType(ScritMaterialIconType.dustFissionByproduct)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition { material -> material != null && hasFissionFuel(material) }

    fun init() {
        TagPrefixExtension.setRadiationDamageFunction(
            fuelRod
        ) { neutrons: Double -> neutrons / 10e23 }
        TagPrefixExtension.setRadiationDamageFunction(
            fuelPelletRaw
        ) { neutrons: Double -> neutrons / 160e23 }
        TagPrefixExtension.setRadiationDamageFunction(
            fuelPellet
        ) { neutrons: Double -> neutrons / 160e23 }
        TagPrefixExtension.setRadiationDamageFunction(
            fuelRodDepleted
        ) { neutrons: Double -> neutrons / 1.5e23 }
        TagPrefixExtension.setRadiationDamageFunction(
            fuelRodHotDepleted
        ) { neutrons: Double -> neutrons / 1e23 }
        TagPrefixExtension.setRadiationDamageFunction(
            fuelPelletDepleted
        ) { neutrons: Double -> neutrons / 24e23 }
    }

    private fun hasFissionFuel(material: Material): Boolean {
        return material.hasProperty<FissionFuelProperty?>(ScritPropertyKey.FISSION_FUEL)
    }

    private fun radioactiveTooltip(): BiConsumer<Material, MutableList<Component>> {
        return BiConsumer { _: Material, tooltip: MutableList<Component> ->
            tooltip.add(
                Component.translatable(
                    "metaitem.nuclear.tooltip.radioactive"
                )
            )
        }
    }
}
