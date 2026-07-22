package supercritical.integration.jei;

import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;
import supercritical.Tags;
import supercritical.api.nuclear.fission.CoolantRegistry;
import supercritical.api.nuclear.fission.FissionFuelRegistry;
import supercritical.api.nuclear.fission.ModeratorRegistry;
import supercritical.common.metatileentities.SCMetaTileEntities;
import supercritical.integration.jei.basic.*;
import supercritical.modules.SCModules;

@JEIPlugin
@GregTechModule(moduleID = SCModules.MODULE_JEI, containerID = Tags.MOD_ID, modDependencies = Mods.Names.JUST_ENOUGH_ITEMS, name = "Supercritical JEI Integration", description = "Supercritical JustEnoughItems Integration Module")
public class JustEnoughItemsModule extends IntegrationSubmodule implements IModPlugin {

	@Override
	public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new CoolantCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new FissionFuelCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new ModeratorCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void register(@NotNull IModRegistry registry) {
		// Nuclear
		Collection<ItemStack> fissionFuels = FissionFuelRegistry.getAllFissionableRods();
		List<FissionFuelInfo> fissionFuelInfos = new ArrayList<>();
		for (ItemStack fuel : fissionFuels) {
			fissionFuelInfos.add(new FissionFuelInfo(fuel));
		}

		String fissionFuelID = Tags.MOD_ID + ":" + "fission_fuel";

		registry.addRecipes(fissionFuelInfos, fissionFuelID);
		registry.addRecipeCatalyst(SCMetaTileEntities.FISSION_REACTOR.getStackForm(), fissionFuelID);

		Collection<Fluid> coolants = CoolantRegistry.getAllCoolants();
		List<CoolantInfo> coolantInfos = new ArrayList<>();
		for (Fluid coolant : coolants) {
			coolantInfos.add(new CoolantInfo(coolant, CoolantRegistry.getCoolant(coolant).getHotCoolant()));
		}

		String coolantID = Tags.MOD_ID + ":" + "coolant";
		registry.addRecipes(coolantInfos, coolantID);
		registry.addRecipeCatalyst(SCMetaTileEntities.FISSION_REACTOR.getStackForm(), coolantID);

		Collection<ModeratorRegistry.ModeratorInfo> moderators = ModeratorRegistry.getAllModerators();
		List<ModeratorInfo> moderatorInfos = new ArrayList<>();
		for (ModeratorRegistry.ModeratorInfo moderator : moderators) {
			moderatorInfos.add(new ModeratorInfo(moderator.getRegistryName(), moderator.getMeta()));
		}

		String moderatorID = Tags.MOD_ID + ":" + "moderator";
		registry.addRecipes(moderatorInfos, moderatorID);
		registry.addRecipeCatalyst(SCMetaTileEntities.FISSION_REACTOR.getStackForm(), moderatorID);
		registry.addRecipeCatalyst(SCMetaTileEntities.MODERATOR_PORT.getStackForm(), moderatorID);
		// Nuclear End
	}
}
