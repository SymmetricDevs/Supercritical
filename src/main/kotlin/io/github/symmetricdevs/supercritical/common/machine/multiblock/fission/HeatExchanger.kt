package io.github.symmetricdevs.supercritical.common.machine.multiblock.fission

import com.gregtechceu.gtceu.GTCEu.id as gtId
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates.abilities
import com.gregtechceu.gtceu.api.pattern.Predicates.frames
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.util.blocks
import io.github.symmetricdevs.supercritical.util.scId
import io.github.symmetricdevs.supercritical.util.self

/**
 * Heat exchanger multiblock. Converts a hot coolant into a cooled coolant while producing power.
 * Does not require external energy input.
 */
class HeatExchanger(holder: IMachineBlockEntity, vararg args: Any?) :
    WorkableMultiblockMachine(holder, *args) {

    companion object {
        fun register(): MultiblockMachineDefinition = ScritRegistration.REGISTRATE
            .multiblock("heat_exchanger") { HeatExchanger(it) }
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(ScritRecipeTypes.HEAT_EXCHANGER)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH)
            .pattern {
                FactoryBlockPattern.start()
                    .aisle("CCC", "BCB", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "CDC", "ACA")
                    .aisle("CCC", "BSB", "AEA")
                    .where('S', it.self)
                    .where('A', frames(GTMaterials.Steel))
                    .where('B', abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(2)
                        .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(2)))
                    .where('C', blocks(GTBlocks.CASING_STEEL_SOLID))
                    .where('D', blocks(GTBlocks.CASING_STEEL_PIPE))
                    .where('E', blocks(GTBlocks.CASING_STEEL_SOLID))
                    .build()
            }
            .workableCasingModel(gtId("block/casings/solid/machine_casing_solid_steel"),
                scId("block/multiblock/heat_exchanger"))
            .register()
    }
}
