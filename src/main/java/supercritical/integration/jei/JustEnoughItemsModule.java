package supercritical.integration.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import supercritical.SCValues;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.common.metatileentities.SCMetaTileEntities;
import supercritical.integration.jei.basic.CoolantCategory;
import supercritical.integration.jei.basic.CoolantInfo;
import supercritical.integration.jei.basic.FissionFuelCategory;
import supercritical.integration.jei.basic.FissionFuelInfo;
import supercritical.modules.SCModules;

@JEIPlugin
@GregTechModule(moduleID = SCModules.MODULE_JEI,
                containerID = SCValues.MODID,
                modDependencies = Mods.Names.JUST_ENOUGH_ITEMS,
                name = "Supercritical JEI Integration",
                description = "Supercritical JustEnoughItems Integration Module")
public class JustEnoughItemsModule extends IntegrationSubmodule implements IModPlugin {

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new CoolantCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new FissionFuelCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(@NotNull IModRegistry registry) {
        // Nuclear
        Collection<ItemStack> fissionFuels = FissionFuelRegistry.getAllFissionableRods();
        List<FissionFuelInfo> fissionFuelInfos = new ArrayList<>();
        for (ItemStack fuel : fissionFuels) {
            fissionFuelInfos.add(new FissionFuelInfo(fuel,
                    FissionFuelRegistry.getDepletedFuel(FissionFuelRegistry.getFissionFuel(fuel))));
        }

        String fissionFuelID = SCValues.MODID + ":" + "fission_fuel";

        registry.addRecipes(fissionFuelInfos, fissionFuelID);
        registry.addRecipeCatalyst(SCMetaTileEntities.FISSION_REACTOR.getStackForm(), fissionFuelID);

        Collection<Fluid> coolants = CoolantRegistry.getAllCoolants();
        List<CoolantInfo> coolantInfos = new ArrayList<>();
        for (Fluid coolant : coolants) {
            coolantInfos.add(new CoolantInfo(coolant, CoolantRegistry.getCoolant(coolant).getHotCoolant()));
        }

        String coolantID = SCValues.MODID + ":" + "coolant";
        registry.addRecipes(coolantInfos, coolantID);
        registry.addRecipeCatalyst(SCMetaTileEntities.FISSION_REACTOR.getStackForm(), coolantID);
        // Nuclear End
    }
}
