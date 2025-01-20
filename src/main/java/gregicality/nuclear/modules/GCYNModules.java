package gregicality.nuclear.modules;

import gregicality.nuclear.GCYNValues;
import gregtech.api.modules.IModuleContainer;
import gregtech.api.modules.ModuleContainer;

@ModuleContainer
public class GCYNModules implements IModuleContainer {

    public static final String MODULE_CORE = "gcyn_core";
    public static final String MODULE_JEI = "gcyn_jei_integration";
    public static final String MODULE_OC = "gcyn_oc_integration";

    @Override
    public String getID() {
        return GCYNValues.MODID;
    }
}
