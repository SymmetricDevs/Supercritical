package io.github.symmetricdevs.supercritical.common.machine.multiblock.fission

import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers
import net.minecraft.resources.ResourceLocation
import io.github.symmetricdevs.supercritical.common.data.ScritRecipeTypes
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import io.github.symmetricdevs.supercritical.util.scId

/**
 * Heat exchanger multiblock. Converts a hot coolant into a cooled coolant while producing power.
 * Does not require external energy input.
 */
class HeatExchanger(holder: IMachineBlockEntity, vararg args: Any?) :
    WorkableMultiblockMachine(holder, *args) {

    companion object {
        fun register(): MultiblockMachineDefinition {
            return ScritRegistration.REGISTRATE
                .multiblock("heat_exchanger") { holder: IMachineBlockEntity -> HeatExchanger(holder) }
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(ScritRecipeTypes.HEAT_EXCHANGER_RECIPES)
                .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH)
                .pattern { definition: MultiblockMachineDefinition ->
                    FactoryBlockPattern.start()
                        .aisle("CCC", "BCB", "ACA")
                        .aisle("CCC", "CDC", "ACA").setRepeatable(1, 7)
                        .aisle("CCC", "BSB", "AEA")
                        .where('S', Predicates.controller(Predicates.blocks(definition.block)))
                        .where('A', Predicates.frames(GTMaterials.Steel))
                        .where(
                            'B', Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(2)
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(2))
                        )
                        .where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                        .where('D', Predicates.blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                        .where('E', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                        .build()
                }
                .workableCasingModel(
                    ResourceLocation("gtceu", "block/casings/solid/machine_casing_solid_steel"),
                    scId("block/multiblock/heat_exchanger")
                )
                .register()
        }
    }
}
