package supercritical.common.registry

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
import supercritical.api.machine.multiblock.SCMultiblockAbility
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.registries.SCRegistries
import supercritical.api.util.scId
import supercritical.common.SCConfigHolder
import supercritical.common.machine.multiblock.MetaTileEntityFissionReactor
import supercritical.common.machine.multiblock.MetaTileEntityHeatExchanger
import supercritical.common.machine.multiblock.MetaTileEntitySpentFuelPool
import supercritical.common.metatileentities.multi.electric.MetaTileEntityGasCentrifuge
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantExportHatch
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityCoolantImportHatch
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodExportBus
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityFuelRodImportBus
import supercritical.common.metatileentities.multi.multiblockpart.MetaTileEntityModeratorPort

object SCMachines {
    val FUEL_ROD_INPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine("fuel_rod_input") { holder: IMachineBlockEntity -> MetaTileEntityFuelRodImportBus(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.IMPORT_FUEL_ROD)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/fuel_rod_input")
            )
        )
        .register()

    val FUEL_ROD_OUTPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine("fuel_rod_output") { holder: IMachineBlockEntity -> MetaTileEntityFuelRodExportBus(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.EXPORT_FUEL_ROD)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/fuel_rod_output")
            )
        )
        .register()

    val COOLANT_INPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine("coolant_input") { holder: IMachineBlockEntity -> MetaTileEntityCoolantImportHatch(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.IMPORT_COOLANT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/coolant_input")
            )
        )
        .register()

    val COOLANT_OUTPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine("coolant_output") { holder: IMachineBlockEntity -> MetaTileEntityCoolantExportHatch(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.EXPORT_COOLANT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/coolant_output")
            )
        )
        .register()

    val CONTROL_ROD: MachineDefinition = SCRegistries.REGISTRATE
        .machine("control_rod") { holder: IMachineBlockEntity -> MetaTileEntityControlRodPort(holder, 4, false) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.CONTROL_ROD_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/control_rod")
            )
        )
        .register()

    val CONTROL_ROD_MODERATED: MachineDefinition = SCRegistries.REGISTRATE
        .machine("control_rod_moderated") { holder: IMachineBlockEntity ->
            MetaTileEntityControlRodPort(
                holder,
                4,
                true
            )
        }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.CONTROL_ROD_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/control_rod_moderated")
            )
        )
        .register()

    val MODERATOR_PORT: MachineDefinition = SCRegistries.REGISTRATE
        .machine("moderator_port") { holder: IMachineBlockEntity -> MetaTileEntityModeratorPort(holder, 4) }
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.MODERATOR_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                scId("block/reactor_vessel"),
                scId("block/overlay/machine/moderator_port")
            )
        )
        .register()

    val FISSION_REACTOR: MultiblockMachineDefinition = SCRegistries.REGISTRATE
        .multiblock("fission_reactor") { holder: IMachineBlockEntity -> MetaTileEntityFissionReactor(holder) }
        .rotationState(RotationState.NON_Y_AXIS)
        .allowExtendedFacing(true)
        .pattern { definition: MultiblockMachineDefinition ->
            MetaTileEntityFissionReactor.buildPattern(
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

    val HEAT_EXCHANGER: MultiblockMachineDefinition? =
        if (SCConfigHolder.INSTANCE.misc.enableHX) MetaTileEntityHeatExchanger.register() else null

    val SPENT_FUEL_POOL: MultiblockMachineDefinition = MetaTileEntitySpentFuelPool.register()

    val GAS_CENTRIFUGE: MultiblockMachineDefinition = SCRegistries.REGISTRATE
        .multiblock("gas_centrifuge") { holder: IMachineBlockEntity -> MetaTileEntityGasCentrifuge(holder) }
        .rotationState(RotationState.NON_Y_AXIS)
        .allowExtendedFacing(false)
        .recipeType(SCRecipeMaps.GAS_CENTRIFUGE_RECIPES)
        .recipeModifiers({ m, _ ->
            MetaTileEntityGasCentrifuge.recipeModifier(
                m
            )
        })
        .pattern { definition: MultiblockMachineDefinition ->
            FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK)
                .aisle("SI", "HH", "CC", "CC", "CC", "CC", "CC")
                .aisle("EE", "HH", "CC", "CC", "CC", "CC", "CC").setRepeatable(1, 14)
                .aisle("OO", "HH", "CC", "CC", "CC", "CC", "CC")
                .where('S', Predicates.controller(Predicates.blocks(definition.block)))
                .where('H', Predicates.blocks(SCBlocks.GAS_CENTRIFUGE_HEATER.get()))
                .where('C', Predicates.blocks(SCBlocks.GAS_CENTRIFUGE_COLUMN.get()))
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
