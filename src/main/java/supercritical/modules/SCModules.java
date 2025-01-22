package supercritical.modules;

import gregtech.api.modules.IModuleContainer;
import gregtech.api.modules.ModuleContainer;
import supercritical.SCValues;

@ModuleContainer
public class SCModules implements IModuleContainer {

    public static final String MODULE_CORE = "supercritical_core";
    public static final String MODULE_JEI = "supercritical_jei_integration";
    public static final String MODULE_OC = "supercritical_oc_integration";

    @Override
    public String getID() {
        return SCValues.MODID;
    }
}
