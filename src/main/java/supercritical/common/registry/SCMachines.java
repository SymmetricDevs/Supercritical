package supercritical.common.registry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.Direction;
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor;
import supercritical.common.metatileentities.multi.MetaTileEntityHeatExchanger;
import supercritical.common.metatileentities.multi.MetaTileEntitySpentFuelPool;
import supercritical.common.metatileentities.multi.electric.MetaTileEntityGasCentrifuge;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantExportHatch;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantImportHatch;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodExportBus;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodImportBus;
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityModeratorPort;

import static supercritical.api.util.SCUtility.scId;

public final class SCMachines {

    public static final MachineDefinition FUEL_ROD_INPUT = SCRegistrate.REGISTRATE
            .machine("fuel_rod_input", holder -> new MetaTileEntityFuelRodImportBus(holder, 4))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.IMPORT_FUEL_ROD)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/fuel_rod_input")))
            .register();

    public static final MachineDefinition FUEL_ROD_OUTPUT = SCRegistrate.REGISTRATE
            .machine("fuel_rod_output", holder -> new MetaTileEntityFuelRodExportBus(holder, 4))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.EXPORT_FUEL_ROD)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/fuel_rod_output")))
            .register();

    public static final MachineDefinition COOLANT_INPUT = SCRegistrate.REGISTRATE
            .machine("coolant_input", holder -> new MetaTileEntityCoolantImportHatch(holder, 4))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.IMPORT_COOLANT)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/coolant_input")))
            .register();

    public static final MachineDefinition COOLANT_OUTPUT = SCRegistrate.REGISTRATE
            .machine("coolant_output", holder -> new MetaTileEntityCoolantExportHatch(holder, 4))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.EXPORT_COOLANT)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/coolant_output")))
            .register();

    public static final MachineDefinition CONTROL_ROD = SCRegistrate.REGISTRATE
            .machine("control_rod", holder -> new MetaTileEntityControlRodPort(holder, 4, false))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.CONTROL_ROD_PORT)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/control_rod")))
            .register();

    public static final MachineDefinition CONTROL_ROD_MODERATED = SCRegistrate.REGISTRATE
            .machine("control_rod_moderated", holder -> new MetaTileEntityControlRodPort(holder, 4, true))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.CONTROL_ROD_PORT)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/control_rod_moderated")))
            .register();

    public static final MachineDefinition MODERATOR_PORT = SCRegistrate.REGISTRATE
            .machine("moderator_port", holder -> new MetaTileEntityModeratorPort(holder, 4))
            .rotationState(RotationState.ALL)
            .tier(GTValues.LV)
            .abilities(SCMultiblockAbility.MODERATOR_PORT)
            .model(GTMachineModels.createOverlayCasingMachineModel(scId("block/reactor_vessel"),
                    scId("block/overlay/machine/moderator_port")))
            .register();

    public static final MultiblockMachineDefinition FISSION_REACTOR = SCRegistrate.REGISTRATE
            .multiblock("fission_reactor", MetaTileEntityFissionReactor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(true)
            .pattern(definition -> MetaTileEntityFissionReactor.buildPattern(definition, 5, 1, 1))
            .workableCasingModel(scId("block/reactor_vessel"), scId("block/multiblock/fission_reactor"))
            .register();

    public static final MultiblockMachineDefinition HEAT_EXCHANGER = MetaTileEntityHeatExchanger.register();

    public static final MultiblockMachineDefinition SPENT_FUEL_POOL = MetaTileEntitySpentFuelPool.register();

    public static final MultiblockMachineDefinition GAS_CENTRIFUGE = SCRegistrate.REGISTRATE
            .multiblock("gas_centrifuge", MetaTileEntityGasCentrifuge::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(SCRecipeMaps.GAS_CENTRIFUGE_RECIPES)
            .recipeModifiers(MetaTileEntityGasCentrifuge::recipeModifier)
            .pattern(definition -> FactoryBlockPattern.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT)
                    .aisle("SI", "HH", "CC", "CC", "CC", "CC", "CC")
                    .aisle("EE", "HH", "CC", "CC", "CC", "CC", "CC").setRepeatable(1, 14)
                    .aisle("OO", "HH", "CC", "CC", "CC", "CC", "CC")
                    .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                    .where('H', Predicates.blocks(SCBlocks.GAS_CENTRIFUGE_HEATER.get()))
                    .where('C', Predicates.blocks(SCBlocks.GAS_CENTRIFUGE_COLUMN.get()))
                    .where('I', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)))
                    .where('E', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS)))
                    .where('O', Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                            .or(Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get())))
                    .build())
            .workableCasingModel(scId("block/gas_centrifuge_heater"), scId("block/multiblock/gas_centrifuge"))
            .register();

    private SCMachines() {}

    public static void ensureInitialized() {
        // no-op; referencing this class triggers static machine registration.
    }
}
