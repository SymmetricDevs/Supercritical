package gregicality.nuclear.modules;

import gregicality.nuclear.GCYNValues;
import gregicality.nuclear.api.util.GCYNLog;
import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@GregTechModule(
        moduleID = GCYNModules.MODULE_CORE,
        containerID = GCYNValues.MODID,
        name = "GCYN Core Module",
        description = "Core module of GCYN",
        coreModule = true)
public class GCYNCoreModule implements IGregTechModule {

    @NotNull
    @Override
    public Logger getLogger() {
        return GCYNLog.logger;
    }
}
