package supercritical.modules;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import supercritical.Tags;
import supercritical.api.util.SCLog;

@GregTechModule(moduleID = SCModules.MODULE_CORE,
                containerID = Tags.MOD_ID,
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
