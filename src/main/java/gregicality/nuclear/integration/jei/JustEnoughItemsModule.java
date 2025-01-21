package gregicality.nuclear.integration.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

import gregicality.nuclear.GCYNValues;
import gregicality.nuclear.api.nuclear.fission.CoolantRegistry;
import gregicality.nuclear.api.nuclear.fission.FissionFuelRegistry;
import gregicality.nuclear.common.metatileentities.GCYNMetaTileEntities;
import gregicality.nuclear.integration.jei.basic.CoolantCategory;
import gregicality.nuclear.integration.jei.basic.CoolantInfo;
import gregicality.nuclear.integration.jei.basic.FissionFuelCategory;
import gregicality.nuclear.integration.jei.basic.FissionFuelInfo;
import gregicality.nuclear.modules.GCYNModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@JEIPlugin
@GregTechModule(
                moduleID = GCYNModules.MODULE_JEI,
                containerID = GCYNValues.MODID,
                modDependencies = Mods.Names.JUST_ENOUGH_ITEMS,
                name = "GCYN JEI Integration",
                description = "GCYN JustEnoughItems Integration Module")
public class JustEnoughItemsModule extends IntegrationSubmodule implements IModPlugin {

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new CoolantCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new FissionFuelCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        // Nuclear
        Collection<ItemStack> fissionFuels = FissionFuelRegistry.getAllFissionableRods();
        List<FissionFuelInfo> fissionFuelInfos = new ArrayList<>();
        for (ItemStack fuel : fissionFuels) {
            fissionFuelInfos.add(new FissionFuelInfo(fuel,
                    FissionFuelRegistry.getDepletedFuel(FissionFuelRegistry.getFissionFuel(fuel))));
        }

        String fissionFuelID = GCYNValues.MODID + ":" + "fission_fuel";

        registry.addRecipes(fissionFuelInfos, fissionFuelID);
        registry.addRecipeCatalyst(GCYNMetaTileEntities.FISSION_REACTOR.getStackForm(), fissionFuelID);

        Collection<Fluid> coolants = CoolantRegistry.getAllCoolants();
        List<CoolantInfo> coolantInfos = new ArrayList<>();
        for (Fluid coolant : coolants) {
            coolantInfos.add(new CoolantInfo(coolant, CoolantRegistry.getCoolant(coolant).getHotCoolant()));
        }

        String coolantID = GCYNValues.MODID + ":" + "coolant";
        registry.addRecipes(coolantInfos, coolantID);
        registry.addRecipeCatalyst(GCYNMetaTileEntities.FISSION_REACTOR.getStackForm(), coolantID);
        // Nuclear End
    }
}
