package supercritical.common.metatileentities;

import supercritical.common.SCConfigHolder;
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor;
import supercritical.common.metatileentities.multi.MetaTileEntityHeatExchanger;
import supercritical.common.metatileentities.multi.MetaTileEntitySpentFuelPool;
import supercritical.common.metatileentities.multi.electric.MetaTileEntityGasCentrifuge;
import supercritical.common.metatileentities.multi.multiblockpart.*;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static supercritical.api.util.SCUtility.scId;

/*
 * Ranges 14000-14499
 */
public class SCMetaTileEntities {

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
        /*
         * Singleblocks: 14000-14399
         */
        FUEL_ROD_INPUT = registerMetaTileEntity(id(14000), new MetaTileEntityFuelRodImportBus(scId("fuel_rod_input")));
        FUEL_ROD_OUTPUT = registerMetaTileEntity(id(14001), new MetaTileEntityFuelRodExportBus(scId("fuel_rod_output")));
        COOLANT_INPUT = registerMetaTileEntity(id(14002), new MetaTileEntityCoolantImportHatch(scId("coolant_input")));
        COOLANT_OUTPUT = registerMetaTileEntity(id(14003), new MetaTileEntityCoolantExportHatch(scId("coolant_output")));
        CONTROL_ROD = registerMetaTileEntity(id(14004), new MetaTileEntityControlRodPort(scId("control_rod"), false));
        CONTROL_ROD_MODERATED = registerMetaTileEntity(id(14005),
                new MetaTileEntityControlRodPort(scId("control_rod_moderated"), true));

        /*
         * Multiblocks: 14400-14499
         */
        if (SCConfigHolder.misc.enableHX) {
            HEAT_EXCHANGER = registerMetaTileEntity(id(14400), new MetaTileEntityHeatExchanger(scId("heat_exchanger")));
        }
        FISSION_REACTOR = registerMetaTileEntity(id(14401), new MetaTileEntityFissionReactor(scId("fission_reactor")));
        SPENT_FUEL_POOL = registerMetaTileEntity(id(14402), new MetaTileEntitySpentFuelPool(scId("spent_fuel_pool")));
        GAS_CENTRIFUGE = registerMetaTileEntity(id(14403), new MetaTileEntityGasCentrifuge(scId("gas_centrifuge")));
    }

    private static int id(int id) {
        return SCConfigHolder.misc.startIdShift + id;
    }
}
