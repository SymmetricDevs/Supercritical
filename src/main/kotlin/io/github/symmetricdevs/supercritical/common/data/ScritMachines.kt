package io.github.symmetricdevs.supercritical.common.data

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import io.github.symmetricdevs.supercritical.api.machine.multiblock.ScritMultiblockAbility
import io.github.symmetricdevs.supercritical.common.machine.multiblock.electric.GasCentrifuge
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.FissionReactor
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.HeatExchanger
import io.github.symmetricdevs.supercritical.common.machine.multiblock.fission.SpentFuelPool
import io.github.symmetricdevs.supercritical.common.machine.multiblock.part.*
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.util.scId

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

    val FISSION_REACTOR: MultiblockMachineDefinition = FissionReactor.register()

    val HEAT_EXCHANGER: MultiblockMachineDefinition = HeatExchanger.register()

    val SPENT_FUEL_POOL: MultiblockMachineDefinition = SpentFuelPool.register()

    val GAS_CENTRIFUGE: MultiblockMachineDefinition = GasCentrifuge.register()

    fun init() {
        // no-op; referencing this class triggers static machine registration.
    }
}