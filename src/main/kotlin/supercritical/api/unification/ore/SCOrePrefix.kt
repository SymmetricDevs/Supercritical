package supercritical.api.unification.ore

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import net.minecraft.network.chat.Component
import supercritical.api.unification.material.info.SCMaterialIconType
import supercritical.api.unification.material.properties.FissionFuelProperty
import supercritical.api.unification.material.properties.SCPropertyKey
import supercritical.api.unification.tag.TagPrefixExtension
import java.util.function.BiConsumer
import java.util.function.Function
import java.util.function.Predicate

object SCOrePrefix {
    val fuelRod: TagPrefix = TagPrefix("fuelRod")
        .materialIconType(SCMaterialIconType.fuelRod)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })
        .tooltip(radioactiveTooltip())

    val fuelRodDepleted: TagPrefix = TagPrefix("fuelRodDepleted")
        .materialIconType(SCMaterialIconType.fuelRodDepleted)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })
        .tooltip(radioactiveTooltip())

    val fuelRodHotDepleted: TagPrefix = TagPrefix("fuelRodHotDepleted")
        .materialIconType(SCMaterialIconType.fuelRodHotDepleted)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })
        .tooltip(radioactiveTooltip())

    val fuelPelletRaw: TagPrefix = TagPrefix("fuelPelletRaw")
        .materialIconType(SCMaterialIconType.fuelPelletRaw)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })
        .tooltip(radioactiveTooltip())

    val fuelPellet: TagPrefix = TagPrefix("fuelPellet")
        .materialIconType(SCMaterialIconType.fuelPellet)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })
        .tooltip(radioactiveTooltip())

    val fuelPelletDepleted: TagPrefix = TagPrefix("fuelPelletDepleted")
        .materialIconType(SCMaterialIconType.fuelPelletDepleted)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })
        .tooltip(radioactiveTooltip())

    val dustSpentFuel: TagPrefix = TagPrefix("dustSpentFuel")
        .materialIconType(SCMaterialIconType.dustSpentFuel)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })

    val dustBredFuel: TagPrefix = TagPrefix("dustBredFuel")
        .materialIconType(SCMaterialIconType.dustBredFuel)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })

    val dustFissionByproduct: TagPrefix = TagPrefix("dustFissionByproduct")
        .materialIconType(SCMaterialIconType.dustFissionByproduct)
        .unificationEnabled(true)
        .generateItem(true)
        .generationCondition(Predicate { obj: Material? -> SCOrePrefix.hasFissionFuel() })

    fun init() {
        TagPrefixExtension.Companion.setRadiationDamageFunction(
            fuelRod,
            Function { neutrons: Double? -> neutrons!! / 10e23 })
        TagPrefixExtension.Companion.setRadiationDamageFunction(
            fuelPelletRaw,
            Function { neutrons: Double? -> neutrons!! / 160e23 })
        TagPrefixExtension.Companion.setRadiationDamageFunction(
            fuelPellet,
            Function { neutrons: Double? -> neutrons!! / 160e23 })
        TagPrefixExtension.Companion.setRadiationDamageFunction(
            fuelRodDepleted,
            Function { neutrons: Double? -> neutrons!! / 1.5e23 })
        TagPrefixExtension.Companion.setRadiationDamageFunction(
            fuelRodHotDepleted,
            Function { neutrons: Double? -> neutrons!! / 1e23 })
        TagPrefixExtension.Companion.setRadiationDamageFunction(
            fuelPelletDepleted,
            Function { neutrons: Double? -> neutrons!! / 24e23 })
    }

    private fun hasFissionFuel(material: Material): Boolean {
        return material.hasProperty<FissionFuelProperty?>(SCPropertyKey.FISSION_FUEL)
    }

    private fun radioactiveTooltip(): BiConsumer<Material?, MutableList<Component?>?> {
        return BiConsumer { material: Material?, tooltip: MutableList<Component?>? ->
            tooltip!!.add(
                Component.translatable(
                    "metaitem.nuclear.tooltip.radioactive"
                )
            )
        }
    }
}
