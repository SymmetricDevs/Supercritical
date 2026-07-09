
package supercritical.common.registry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import supercritical.BuildConfig;
import supercritical.api.unification.material.SCMaterials;
import supercritical.api.unification.ore.SCOrePrefix;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SCItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BuildConfig.MOD_ID);

    public static final RegistryObject<Item> ANODE_BASKET = ITEMS.register("anode_basket",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FUEL_CLADDING = ITEMS.register("fuel_cladding",
            () -> new Item(new Item.Properties()));

    public static final Map<String, NuclearFuelItems> NUCLEAR_FUEL_ITEMS = registerNuclearFuelItems();

    private SCItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private static Map<String, NuclearFuelItems> registerNuclearFuelItems() {
        var items = new LinkedHashMap<String, NuclearFuelItems>();
        registerFuelItemSet(items, "uraninite", () -> GTMaterials.Uraninite);
        registerFuelItemSet(items, "leu_235", () -> SCMaterials.LEU235);
        registerFuelItemSet(items, "heu_235", () -> SCMaterials.HEU235);
        registerFuelItemSet(items, "low_grade_mox", () -> SCMaterials.LowGradeMOX);
        registerFuelItemSet(items, "high_grade_mox", () -> SCMaterials.HighGradeMOX);
        return Collections.unmodifiableMap(items);
    }

    private static void registerFuelItemSet(Map<String, NuclearFuelItems> items, String materialName,
                                            Supplier<Material> materialSupplier) {
        items.put(materialName, new NuclearFuelItems(
                itemSupplier(SCOrePrefix.fuelRod, materialSupplier),
                itemSupplier(SCOrePrefix.fuelRodDepleted, materialSupplier),
                itemSupplier(SCOrePrefix.fuelRodHotDepleted, materialSupplier),
                itemSupplier(SCOrePrefix.fuelPelletRaw, materialSupplier),
                itemSupplier(SCOrePrefix.fuelPellet, materialSupplier),
                itemSupplier(SCOrePrefix.fuelPelletDepleted, materialSupplier),
                itemSupplier(SCOrePrefix.dustSpentFuel, materialSupplier),
                itemSupplier(SCOrePrefix.dustBredFuel, materialSupplier),
                itemSupplier(SCOrePrefix.dustFissionByproduct, materialSupplier)));
    }

    private static Supplier<Item> itemSupplier(TagPrefix prefix, Supplier<Material> materialSupplier) {
        return () -> GTMaterialItems.MATERIAL_ITEMS.get(prefix, materialSupplier.get()).get();
    }

    public record NuclearFuelItems(Supplier<Item> fuelRod,
                                   Supplier<Item> depletedFuelRod,
                                   Supplier<Item> hotDepletedFuelRod,
                                   Supplier<Item> rawFuelPellet,
                                   Supplier<Item> fuelPellet,
                                   Supplier<Item> depletedFuelPellet,
                                   Supplier<Item> spentFuelDust,
                                   Supplier<Item> bredFuelDust,
                                   Supplier<Item> fissionByproductDust) {}
}
