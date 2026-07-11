package supercritical.common.registry

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTMaterialItems
import com.gregtechceu.gtceu.common.data.GTMaterials
import net.minecraft.world.item.Item
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import supercritical.BuildConfig
import supercritical.api.unification.material.SCMaterials
import supercritical.api.unification.ore.SCOrePrefix
import java.util.*
import java.util.function.Supplier

object ScritItems {
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, BuildConfig.MOD_ID)

    val ANODE_BASKET: RegistryObject<Item> = ITEMS.register("anode_basket") { Item(Item.Properties()) }
    val FUEL_CLADDING: RegistryObject<Item> = ITEMS.register("fuel_cladding") { Item(Item.Properties()) }

    val NUCLEAR_FUEL_ITEMS: MutableMap<String, NuclearFuelItems> = registerNuclearFuelItems()

    fun register(modEventBus: IEventBus?) {
        ITEMS.register(modEventBus)
    }

    private fun registerNuclearFuelItems(): MutableMap<String, NuclearFuelItems> {
        val items = LinkedHashMap<String, NuclearFuelItems>()
        registerFuelItemSet(items, "uraninite") { GTMaterials.Uraninite }
        registerFuelItemSet(items, "leu_235") { SCMaterials.LEU235 }
        registerFuelItemSet(items, "heu_235") { SCMaterials.HEU235 }
        registerFuelItemSet(items, "low_grade_mox") { SCMaterials.LowGradeMOX }
        registerFuelItemSet(items, "high_grade_mox") { SCMaterials.HighGradeMOX }
        return Collections.unmodifiableMap(items)
    }

    private fun registerFuelItemSet(
        items: MutableMap<String, NuclearFuelItems>, materialName: String,
        materialSupplier: Supplier<Material>
    ) {
        items[materialName] = NuclearFuelItems(
            itemSupplier(SCOrePrefix.fuelRod, materialSupplier),
            itemSupplier(SCOrePrefix.fuelRodDepleted, materialSupplier),
            itemSupplier(SCOrePrefix.fuelRodHotDepleted, materialSupplier),
            itemSupplier(SCOrePrefix.fuelPelletRaw, materialSupplier),
            itemSupplier(SCOrePrefix.fuelPellet, materialSupplier),
            itemSupplier(SCOrePrefix.fuelPelletDepleted, materialSupplier),
            itemSupplier(SCOrePrefix.dustSpentFuel, materialSupplier),
            itemSupplier(SCOrePrefix.dustBredFuel, materialSupplier),
            itemSupplier(SCOrePrefix.dustFissionByproduct, materialSupplier)
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
