package gregicality.nuclear.integration.opencomputers;

import static gregtech.integration.opencomputers.OpenComputersModule.registerDriver;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import gregicality.nuclear.GCYNValues;
import gregicality.nuclear.integration.opencomputers.drivers.specific.DriverFissionReactor;
import gregicality.nuclear.modules.GCYNModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;

@GregTechModule(
                moduleID = GCYNModules.MODULE_OC,
                containerID = GCYNValues.MODID,
                modDependencies = Mods.Names.OPEN_COMPUTERS,
                name = "GCYN OpenComputers Integration",
                description = "GCYN OpenComputers Integration Module")
public class OpenComputersModule extends IntegrationSubmodule {

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("Registering OpenComputers Drivers...");
        registerDriver(new DriverFissionReactor());
    }
}
