package gregicality.nuclear.common.metatileentities;

import static gregicality.nuclear.api.util.GCYNUtility.gcynId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

import gregicality.nuclear.common.GCYNConfigHolder;
import gregicality.nuclear.common.metatileentities.multi.MetaTileEntityFissionReactor;
import gregicality.nuclear.common.metatileentities.multi.MetaTileEntityHeatExchanger;
import gregicality.nuclear.common.metatileentities.multi.MetaTileEntitySpentFuelPool;
import gregicality.nuclear.common.metatileentities.multi.electric.MetaTileEntityGasCentrifuge;
import gregicality.nuclear.common.metatileentities.multi.multiblockpart.*;

public class GCYNMetaTileEntities {

    // Nuclear MTEs
    public static MetaTileEntityHeatExchanger HEAT_EXCHANGER;
    public static MetaTileEntityFissionReactor FISSION_REACTOR;
    public static MetaTileEntityFuelRodImportBus FUEL_ROD_INPUT;
    public static MetaTileEntityFuelRodExportBus FUEL_ROD_OUTPUT;
    public static MetaTileEntityCoolantImportHatch COOLANT_INPUT;
    public static MetaTileEntityCoolantExportHatch COOLANT_OUTPUT;
    public static MetaTileEntityControlRodPort CONTROL_ROD;
    public static MetaTileEntityControlRodPort CONTROL_ROD_MODERATED;
    public static MetaTileEntitySpentFuelPool SPENT_FUEL_POOL;
    public static MetaTileEntityGasCentrifuge GAS_CENTRIFUGE;

    public static void init() {
        if (GCYNConfigHolder.misc.enableHX) {
            HEAT_EXCHANGER = registerMetaTileEntity(15044, new MetaTileEntityHeatExchanger(gcynId("heat_exchanger")));
        }

        FISSION_REACTOR = registerMetaTileEntity(1043, new MetaTileEntityFissionReactor(gcynId("fission_reactor")));
        SPENT_FUEL_POOL = registerMetaTileEntity(1044, new MetaTileEntitySpentFuelPool(gcynId("spent_fuel_pool")));
        GAS_CENTRIFUGE = registerMetaTileEntity(1046, new MetaTileEntityGasCentrifuge(gcynId("gas_centrifuge")));

        FUEL_ROD_INPUT = registerMetaTileEntity(1800, new MetaTileEntityFuelRodImportBus(gcynId("fuel_rod_input")));
        FUEL_ROD_OUTPUT = registerMetaTileEntity(1801, new MetaTileEntityFuelRodExportBus(gcynId("fuel_rod_output")));
        COOLANT_INPUT = registerMetaTileEntity(1802, new MetaTileEntityCoolantImportHatch(gcynId("coolant_input")));
        COOLANT_OUTPUT = registerMetaTileEntity(1803, new MetaTileEntityCoolantExportHatch(gcynId("coolant_output")));
        CONTROL_ROD = registerMetaTileEntity(1804, new MetaTileEntityControlRodPort(gcynId("control_rod"), false));
        CONTROL_ROD_MODERATED = registerMetaTileEntity(1805,
                new MetaTileEntityControlRodPort(gcynId("control_rod_moderated"), true));
    }
}
