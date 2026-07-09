package supercritical.common.registry

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import supercritical.api.metatileentity.multiblock.SCMultiblockAbility
import supercritical.api.recipes.SCRecipeMaps
import supercritical.api.registries.SCRegistries
import supercritical.api.util.SCUtility
import supercritical.common.metatileentities.multi.MetaTileEntityFissionReactor
import supercritical.common.metatileentities.multi.MetaTileEntityHeatExchanger
import supercritical.common.metatileentities.multi.MetaTileEntitySpentFuelPool
import supercritical.common.metatileentities.multi.electric.MetaTileEntityGasCentrifuge
import supercritical.common.metatileentities.multi.multiblockpart.*
import java.util.function.Function

object SCMachines {
    val FUEL_ROD_INPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine(
            "fuel_rod_input",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityFuelRodImportBus(holder, 4) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.IMPORT_FUEL_ROD)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/fuel_rod_input")
            )
        )
        .register()

    val FUEL_ROD_OUTPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine(
            "fuel_rod_output",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityFuelRodExportBus(holder, 4) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.EXPORT_FUEL_ROD)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/fuel_rod_output")
            )
        )
        .register()

    val COOLANT_INPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine(
            "coolant_input",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityCoolantImportHatch(holder, 4) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.IMPORT_COOLANT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/coolant_input")
            )
        )
        .register()

    val COOLANT_OUTPUT: MachineDefinition = SCRegistries.REGISTRATE
        .machine(
            "coolant_output",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityCoolantExportHatch(holder, 4) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.EXPORT_COOLANT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/coolant_output")
            )
        )
        .register()

    val CONTROL_ROD: MachineDefinition = SCRegistries.REGISTRATE
        .machine(
            "control_rod",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityControlRodPort(holder, 4, false) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.CONTROL_ROD_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/control_rod")
            )
        )
        .register()

    val CONTROL_ROD_MODERATED: MachineDefinition = SCRegistries.REGISTRATE
        .machine(
            "control_rod_moderated",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityControlRodPort(holder, 4, true) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.CONTROL_ROD_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/control_rod_moderated")
            )
        )
        .register()

    val MODERATOR_PORT: MachineDefinition = SCRegistries.REGISTRATE
        .machine("moderator_port", Function { holder: IMachineBlockEntity? -> MetaTileEntityModeratorPort(holder, 4) })
        .rotationState(RotationState.ALL)
        .tier(GTValues.LV)
        .abilities(SCMultiblockAbility.MODERATOR_PORT)
        .model(
            GTMachineModels.createOverlayCasingMachineModel(
                SCUtility.scId("block/reactor_vessel"),
                SCUtility.scId("block/overlay/machine/moderator_port")
            )
        )
        .register()

    val FISSION_REACTOR: MultiblockMachineDefinition = SCRegistries.REGISTRATE
        .multiblock(
            "fission_reactor",
            Function { holder: IMachineBlockEntity? -> MetaTileEntityFissionReactor(holder) })
        .rotationState(RotationState.NON_Y_AXIS)
        .allowExtendedFacing(true)
        .pattern(Function { definition: MultiblockMachineDefinition? ->
            MetaTileEntityFissionReactor.Companion.buildPattern(
                definition,
                5,
                1,
                1
            )
        })
        .workableCasingModel(SCUtility.scId("block/reactor_vessel"), SCUtility.scId("block/multiblock/fission_reactor"))
        .register()

    val HEAT_EXCHANGER: MultiblockMachineDefinition = MetaTileEntityHeatExchanger.Companion.register()

    val SPENT_FUEL_POOL: MultiblockMachineDefinition = MetaTileEntitySpentFuelPool.Companion.register()

    val GAS_CENTRIFUGE: MultiblockMachineDefinition = SCRegistries.REGISTRATE
        .multiblock("gas_centrifuge", Function { holder: IMachineBlockEntity? -> MetaTileEntityGasCentrifuge(holder) })
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType(SCRecipeMaps.GAS_CENTRIFUGE_RECIPES)
        .recipeModifiers(RecipeModifier { machine: MetaMachine?, recipe: GTRecipe? ->
            MetaTileEntityGasCentrifuge.Companion.recipeModifier(
                machine,
                recipe
            )
        })
        .pattern(Function { definition: MultiblockMachineDefinition? ->
            FactoryBlockPattern.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT)
                .aisle("SI", "HH", "CC", "CC", "CC", "CC", "CC")
                .aisle("EE", "HH", "CC", "CC", "CC", "CC", "CC").setRepeatable(1, 14)
                .aisle("OO", "HH", "CC", "CC", "CC", "CC", "CC")
                .where('S', Predicates.controller(Predicates.blocks(definition!!.getBlock())))
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
        })
        .workableCasingModel(
            SCUtility.scId("block/gas_centrifuge_heater"),
            SCUtility.scId("block/multiblock/gas_centrifuge")
        )
        .register()

    fun ensureInitialized() {
        // no-op; referencing this class triggers static machine registration.
    }
}
