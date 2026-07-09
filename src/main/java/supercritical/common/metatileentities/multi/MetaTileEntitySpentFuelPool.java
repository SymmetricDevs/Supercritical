package supercritical.common.metatileentities.multi;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supercritical.api.pattern.SCPredicates;
import supercritical.api.recipes.SCRecipeMaps;
import supercritical.api.registries.SCRegistries;
import supercritical.common.registry.SCBlocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static supercritical.api.util.SCUtility.scId;

/**
 * Spent fuel pool multiblock. Slowly cools spent nuclear fuel by submerging it in water.
 * Pool length is variable and determines maximum recipe parallelism.
 */
@MethodsReturnNonnullByDefault
public class MetaTileEntitySpentFuelPool extends WorkableMultiblockMachine implements IControllable, IDisplayUIMachine {

    public static final int PARALLEL_PER_LENGTH = 32;

    private boolean workingEnabled = true;
    private boolean waterFilled;
    private List<BlockPos> waterPositions;
    @Nullable
    private TickableSubscription waterFillSubscription;
    private int poolLength = 1;

    public MetaTileEntitySpentFuelPool(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object... args) {
        return new SpentFuelPoolRecipeLogic(this);
    }

    @Override
    public @NotNull SpentFuelPoolRecipeLogic getRecipeLogic() {
        return (SpentFuelPoolRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.waterPositions = getMultiblockState().getMatchContext().getOrDefault(SCPredicates.FLUID_BLOCKS_KEY, new ArrayList<>());
        this.waterPositions.sort(Comparator.comparingInt(BlockPos::getY));
        this.waterFilled = waterPositions.isEmpty();
        int[] repetitions = getPattern().getFormedRepetitionCount();
        this.poolLength = repetitions != null && repetitions.length > 2 ? Math.max(1, repetitions[2]) : 1;
        this.waterFillSubscription = subscribeServerTick(this::tryFillWater);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unsubscribe(this.waterFillSubscription);
        this.waterFillSubscription = null;
        this.waterPositions = null;
        this.waterFilled = false;
        this.poolLength = 1;
    }

    private void tryFillWater() {
        if (waterFilled || waterPositions == null || waterPositions.isEmpty()) return;
        if (getOffsetTimer() % 5 != 0) return;

        SCPredicates.fillFluid(this, this.waterPositions, Fluids.WATER);
        if (this.waterPositions.isEmpty()) {
            this.waterFilled = true;
        }
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return super.isRecipeLogicAvailable() && waterFilled;
    }

    public boolean isWaterFilled() {
        return waterFilled;
    }

    public int getPoolLength() {
        return poolLength;
    }

    public int getMaxParallel() {
        return getPoolLength() * PARALLEL_PER_LENGTH;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            if (!waterFilled) {
                textList.add(Component.translatable("supercritical.multiblock.spent_fuel_pool.obstructed"));
            } else if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            } else if (recipeLogic.isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }
            textList.add(Component.translatable("supercritical.multiblock.spent_fuel_pool.parallel", getMaxParallel()));
        }
    }

    @NotNull
    @Override
    public BlockPattern getPattern() {
        return buildPattern(getDefinition());
    }

    private static BlockPattern buildPattern(MultiblockMachineDefinition definition) {
        return FactoryBlockPattern.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT)
                // spotless:off
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "S........T")
                .aisle("CCCCCCCCCC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWRRRRRRWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "T........T")
                .setRepeatable(1, 10)
                .aisle("CCCCCCCCCC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "CWWWWWWWWC", "T........T")
                .aisle("CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "CCCCCCCCCC", "TTTTTTTTTT")
                //spotless:on
                .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                .where('.', Predicates.any())
                .where('C', Predicates.blocks(SCBlocks.GRAY_PANELLING.get()))
                .where('W', SCPredicates.fluid(Fluids.WATER))
                .where('R', Predicates.blocks(SCBlocks.SPENT_FUEL_CASING.get()))
                .where('T', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                        .or(Predicates.autoAbilities(SCRecipeMaps.SPENT_FUEL_POOL_RECIPES))
                        .or(Predicates.autoAbilities(ConfigHolder.INSTANCE.machines.enableMaintenance, false, false))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS)))
                .build();
    }

    public static MultiblockMachineDefinition register() {
        return SCRegistries.REGISTRATE
                .multiblock("spent_fuel_pool", MetaTileEntitySpentFuelPool::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .allowExtendedFacing(false)
                .recipeType(SCRecipeMaps.SPENT_FUEL_POOL_RECIPES)
                .recipeModifiers(MetaTileEntitySpentFuelPool::poolParallel)
                .pattern(MetaTileEntitySpentFuelPool::buildPattern)
                .workableCasingModel(scId("block/gray_panelling"), scId("block/multiblock/spent_fuel_pool"))
                .tooltipBuilder((stack, tooltip) -> {
                    tooltip.add(Component.translatable("supercritical.machine.spent_fuel_pool.tooltip.parallel", PARALLEL_PER_LENGTH));
                    tooltip.add(Component.translatable("supercritical.machine.fluid_auto_fill.tooltip"));
                })
                .register();
    }

    public static class SpentFuelPoolRecipeLogic extends RecipeLogic {

        public SpentFuelPoolRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        public void serverTick() {
            if (!machine.isWorkingEnabled()) {
                return;
            }
            super.serverTick();
        }

        @Override
        public boolean checkMatchedRecipeAvailable(GTRecipe match) {
            if (!(machine instanceof MetaTileEntitySpentFuelPool pool) || !pool.isWaterFilled()) {
                return false;
            }
            var modified = machine.fullModifyRecipe(match);
            if (modified != null) {
                var recipeMatch = checkRecipe(modified);
                if (recipeMatch.isSuccess()) {
                    setupRecipe(modified);
                }
                if (lastRecipe != null && getStatus() == Status.WORKING) {
                    lastOriginRecipe = match;
                    lastFailedMatches = null;
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Recipe modifier that applies pool-length based parallelism.
     */
    public static ModifierFunction poolParallel(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MetaTileEntitySpentFuelPool pool) || !pool.isFormed()) {
            return ModifierFunction.IDENTITY;
        }
        int parallels = ParallelLogic.getParallelAmount(machine, recipe, pool.getMaxParallel());
        if (parallels <= 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(parallels)
                .parallels(parallels)
                .build();
    }
}
