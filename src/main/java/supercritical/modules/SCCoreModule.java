package supercritical.modules;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import supercritical.SCValues;
import supercritical.api.util.SCLog;

@GregTechModule(moduleID = SCModules.MODULE_CORE,
                containerID = SCValues.MODID,
                name = "Supercritical Core Module",
                description = "Core module of Supercritical",
                coreModule = true)
public class SCCoreModule implements IGregTechModule {

    @NotNull
    @Override
    public Logger getLogger() {
        return SCLog.logger;
    }
}
