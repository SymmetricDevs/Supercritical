package supercritical.common.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import java.util.function.Supplier

/**
 * Molten corium block — a lava-like flowing corium source. Ports the 1.12.2
 * `supercritical.common.blocks.BlockMoltenCorium` (which extended GTCEu's `GTFluidBlock`):
 *
 *  - Spreads fire in `randomTick`, copied from vanilla `BlockStaticLiquid` / legacy `updateTick`.
 *  - `isFireSource` returns true (legacy `isBurning()` == true).
 *  - Entities inside corium are treated as inside lava (burn + lava damage), replicating the legacy
 *    `isEntityInsideMaterial` override.
 *
 * Wiring note: GTCEu Modern's `GTFluid` holds a `final` block supplier fixed when the material fluid is
 * registered, so the corium fluid's own legacy block (the one returned by
 * `Corium.getFluid().defaultFluidState().createLegacyBlock()` and used by `SCPredicates.fillFluid`) is the
 * default `LiquidBlock` that `FluidBuilder.block()` registers — it cannot be replaced by this subclass
 * without a GTCEu API extension (a `FluidBuilder#block(factory)` overload on the material builder) or a
 * mixin. The corium fluid is fully placeable/flowing via that default block; this class is the faithful
 * behavior port kept ready for that wiring (e.g. a mixin that swaps the block, or a future GTCEu API).
 * Register with `BlockBehaviour.Properties.copy(Blocks.LAVA).noLootTable().randomTicks().lightLevel { 15 }`
 * bound to the corium `FlowingFluid`.
 */
class BlockMoltenCorium(
    fluid: Supplier<out FlowingFluid>,
    properties: Properties,
) : LiquidBlock(fluid, properties) {

    @Suppress(
        "DEPRECATION",
        "OVERRIDE_DEPRECATION"
    ) // entityInside is the correct 1.20.1 hook for in-fluid entity effects.
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        super.entityInside(state, level, pos, entity)
        if (!entity.fireImmune()) {
            entity.setSecondsOnFire(15)
            entity.hurt(level.damageSources().lava(), 4.0f)
        }
    }

    override fun isFireSource(
        state: BlockState, level: LevelReader, pos: BlockPos, direction: Direction
    ): Boolean = true

    override fun isRandomlyTicking(state: BlockState): Boolean = true

    // Fire-spreading logic adapted from legacy BlockMoltenCorium#updateTick (originally BlockStaticLiquid).
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        super.randomTick(state, level, pos, random)
        if (!level.gameRules.getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK)) return
        val spread = random.nextInt(3)
        if (spread > 0) {
            var cursor = pos
            for (j in 0 until spread) {
                cursor = cursor.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1)
                if (cursor.y < level.minBuildHeight || cursor.y >= level.maxBuildHeight || !level.isLoaded(cursor)) {
                    return
                }
                val target = level.getBlockState(cursor)
                if (target.isAir) {
                    if (isSurroundingBlockFlammable(level, cursor)) {
                        level.setBlockAndUpdate(cursor, Blocks.FIRE.defaultBlockState())
                        return
                    }
                } else if (!target.getCollisionShape(level, cursor).isEmpty) {
                    // Legacy used BlockState#blocksMotion() (deprecated); the non-deprecated
                    // equivalent is a non-empty collision shape, which is how blocksMotion is defined.
                    return
                }
            }
        } else {
            for (k in 0 until 3) {
                val side = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1)
                if (side.y < level.minBuildHeight || side.y >= level.maxBuildHeight || !level.isLoaded(side)) {
                    return
                }
                if (level.isEmptyBlock(side.above()) && canBlockBurn(level, side)) {
                    level.setBlockAndUpdate(side.above(), Blocks.FIRE.defaultBlockState())
                }
            }
        }
    }

    private fun isSurroundingBlockFlammable(level: LevelAccessor, pos: BlockPos): Boolean {
        for (direction in Direction.entries) {
            if (canBlockBurn(level, pos.relative(direction))) return true
        }
        return false
    }

    private fun canBlockBurn(level: LevelAccessor, pos: BlockPos): Boolean {
        val state: BlockState = level.getBlockState(pos)
        // Legacy used Material#getCanBurn(); the 1.20.1 equivalent is BlockState#ignitedByLava().
        return state.ignitedByLava()
    }
}
