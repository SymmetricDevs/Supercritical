package supercritical.common.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import net.minecraft.network.chat.Component
import supercritical.api.machine.multiblock.ScritMultiblockAbility
import supercritical.common.machine.multiblock.fission.FissionReactor
import supercritical.common.machine.multiblock.electric.GasCentrifuge
import supercritical.common.machine.multiblock.fission.HeatExchanger
import supercritical.common.machine.multiblock.fission.SpentFuelPool
import supercritical.common.machine.multiblock.multiblockpart.*
import supercritical.common.machine.multiblock.part.CoolantExportHatch
import supercritical.common.machine.multiblock.part.CoolantImportHatch
import supercritical.common.registry.ScritBlocks
import supercritical.common.registry.ScritRegistration
import supercritical.util.scId

object ScritMachines {
    val FUEL_ROD_INPUT: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("fuel_rod_input") { holder: IMachineBlockEntity -> FuelRodImportBus(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.IMPORT_FUEL_ROD)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/fuel_rod_input")
            )
        )
        .register()

    val FUEL_ROD_OUTPUT: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("fuel_rod_output") { holder: IMachineBlockEntity -> FuelRodExportBus(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.EXPORT_FUEL_ROD)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/fuel_rod_output")
            )
        )
        .register()

    val COOLANT_INPUT: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("coolant_input") { holder: IMachineBlockEntity -> CoolantImportHatch(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.IMPORT_COOLANT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/coolant_input")
            )
        )
        .register()

    val COOLANT_OUTPUT: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("coolant_output") { holder: IMachineBlockEntity -> CoolantExportHatch(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.EXPORT_COOLANT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/coolant_output")
            )
        )
        .register()

    val CONTROL_ROD: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("control_rod") { holder: IMachineBlockEntity -> ControlRodPort(holder, 4, false) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.CONTROL_ROD_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/control_rod")
            )
        )
        .register()

    val CONTROL_ROD_MODERATED: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("control_rod_moderated") { holder: IMachineBlockEntity ->
            ControlRodPort(
                holder,
                4,
                true
            )
        }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.CONTROL_ROD_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/control_rod_moderated")
            )
        )
        .register()

    val MODERATOR_PORT: MachineDefinition = ScritRegistration.REGISTRATE
        .machine("moderator_port") { holder: IMachineBlockEntity -> ModeratorPort(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(ScritMultiblockAbility.MODERATOR_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/moderator_port")
            )
        )
        .register()

    val FISSION_REACTOR: MultiblockMachineDefinition = ScritRegistration.REGISTRATE
        .multiblock("fission_reactor") { holder: IMachineBlockEntity -> FissionReactor(holder) }
        .rotationState(RotationState.NON_Y_AXIS)
        .allowExtendedFacing(true)
        .pattern { definition: MultiblockMachineDefinition ->
            FissionReactor.buildPattern(
                definition,
                5,
                1,
                1
            )
        }
        .workableCasingModel(scId("block/reactor_vessel"), scId("block/multiblock/fission_reactor"))
        .tooltipBuilder { _, tooltip ->
            tooltip.add(Component.translatable("supercritical.machine.fission_reactor.tooltip.1"))
            tooltip.add(Component.translatable("supercritical.machine.fission_reactor.tooltip.2"))
            tooltip.add(Component.translatable("supercritical.machine.fission_reactor.tooltip.3"))
        }
        .register()

    val HEAT_EXCHANGER: MultiblockMachineDefinition = HeatExchanger.register()

    val SPENT_FUEL_POOL: MultiblockMachineDefinition = SpentFuelPool.register()

    val GAS_CENTRIFUGE: MultiblockMachineDefinition = ScritRegistration.REGISTRATE
        .multiblock("gas_centrifuge") { holder: IMachineBlockEntity -> GasCentrifuge(holder) }
        .rotationState(RotationState.NON_Y_AXIS)
        .allowExtendedFacing(false)
        .recipeType(ScritRecipeTypes.GAS_CENTRIFUGE_RECIPES)
        .recipeModifiers(GasCentrifuge.RECIPE_MODIFIER)
        .pattern { definition: MultiblockMachineDefinition ->
            FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK)
                .aisle("SI", "HH", "CC", "CC", "CC", "CC", "CC")
                .aisle("EE", "HH", "CC", "CC", "CC", "CC", "CC").setRepeatable(1, 14)
                .aisle("OO", "HH", "CC", "CC", "CC", "CC", "CC")
                .where('S', Predicates.controller(Predicates.blocks(definition.block)))
                .where('H', Predicates.blocks(ScritBlocks.GAS_CENTRIFUGE_HEATER.get()))
                .where('C', Predicates.blocks(ScritBlocks.GAS_CENTRIFUGE_COLUMN.get()))
                .where(
                    'I', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get())
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                )
                .where(
                    'E', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get())
                        .or(
                            Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)
                        )
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS, PartAbility.EXPORT_ITEMS))
                )
                .where(
                    'O', Predicates.abilities(PartAbility.EXPORT_FLUIDS)
                        .or(Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                )
                .build()
        }
        .workableCasingModel(
            scId("block/gas_centrifuge_heater"),
            scId("block/multiblock/gas_centrifuge")
        )
        .tooltipBuilder { _, tooltip ->
            tooltip.add(Component.translatable("supercritical.machine.gas_centrifuge.tooltip.parallel"))
        }
        .register()

    fun ensureInitialized() {
        // no-op; referencing this class triggers static machine registration.
    }
}