package io.github.symmetricdevs.supercritical.common.data

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMaterialItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.tterrag.registrate.util.entry.ItemEntry
import net.minecraft.world.item.Item
import io.github.symmetricdevs.supercritical.api.data.chemical.ore.ScritOrePrefix
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import java.util.*
import java.util.function.Supplier

/**
 * Supercritical's plain items, registered via GTRegistrate (like GTCEu's `GTItems`). Registrate
 * auto-generates the `item/generated` model and title-cased English lang (`"anode_basket"` →
 * `"Anode Basket"`). `ItemEntry<Item>` implements `Supplier<Item>`, so `ScritItems.X.get()` call
 * sites are unchanged.
 *
 * [NUCLEAR_FUEL_ITEMS] is preserved verbatim: it is a lazy lookup table of `Supplier<Item>` into
 * GTCEu's own [GTMaterialItems] (per material/TagPrefix material items), not Supercritical-
 * registered items, so it is untouched by the Registrate migration.
 */
object ScritItems {
    private val REGISTRATE = ScritRegistration.REGISTRATE

    val ANODE_BASKET: ItemEntry<Item> = REGISTRATE.item("anode_basket") { Item(it) }.register()
    val FUEL_CLADDING: ItemEntry<Item> = REGISTRATE.item("fuel_cladding") { Item(it) }.register()

    val NUCLEAR_FUEL_ITEMS: MutableMap<String, NuclearFuelItems> = registerNuclearFuelItems()

    /** Forces object initialization so every item entry registers via GTRegistrate. */
    fun init() {}

    private fun registerNuclearFuelItems(): MutableMap<String, NuclearFuelItems> {
        val items = LinkedHashMap<String, NuclearFuelItems>()
        registerFuelItemSet(items, "uraninite") { GTMaterials.Uraninite }
        registerFuelItemSet(items, "leu_235") { ScritMaterials.LEU235 }
        registerFuelItemSet(items, "heu_235") { ScritMaterials.HEU235 }
        registerFuelItemSet(items, "low_grade_mox") { ScritMaterials.LowGradeMOX }
        registerFuelItemSet(items, "high_grade_mox") { ScritMaterials.HighGradeMOX }
        return Collections.unmodifiableMap(items)
    }

    private fun registerFuelItemSet(
        items: MutableMap<String, NuclearFuelItems>, materialName: String,
        materialSupplier: Supplier<Material>
    ) {
        items[materialName] = NuclearFuelItems(
            itemSupplier(ScritOrePrefix.fuelRod, materialSupplier),
            itemSupplier(ScritOrePrefix.fuelRodDepleted, materialSupplier),
            itemSupplier(ScritOrePrefix.fuelRodHotDepleted, materialSupplier),
            itemSupplier(ScritOrePrefix.fuelPelletRaw, materialSupplier),
            itemSupplier(ScritOrePrefix.fuelPellet, materialSupplier),
            itemSupplier(ScritOrePrefix.fuelPelletDepleted, materialSupplier),
            itemSupplier(ScritOrePrefix.dustSpentFuel, materialSupplier),
            itemSupplier(ScritOrePrefix.dustBredFuel, materialSupplier),
            itemSupplier(ScritOrePrefix.dustFissionByproduct, materialSupplier)
        )
    }

    private fun itemSupplier(prefix: TagPrefix, materialSupplier: Supplier<Material>): Supplier<Item> {
        return Supplier {
            val material = materialSupplier.get()
            requireNotNull(GTMaterialItems.MATERIAL_ITEMS.get(prefix, material)?.get()) {
                "Material item for ${prefix.name}/${material.name} is not registered"
            }
        }
    }

    @JvmRecord
    data class NuclearFuelItems(
        val fuelRod: Supplier<Item>,
        val depletedFuelRod: Supplier<Item>,
        val hotDepletedFuelRod: Supplier<Item>,
        val rawFuelPellet: Supplier<Item>,
        val fuelPellet: Supplier<Item>,
        val depletedFuelPellet: Supplier<Item>,
        val spentFuelDust: Supplier<Item>,
        val bredFuelDust: Supplier<Item>,
        val fissionByproductDust: Supplier<Item>
    )
}
