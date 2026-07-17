package io.github.symmetricdevs.supercritical.common.block

import com.gregtechceu.gtceu.api.fluids.FluidState
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.MOLTEN
import com.gregtechceu.gtceu.api.registry.registrate.forge.GTFluidBuilder
import io.github.symmetricdevs.supercritical.common.data.ScritMaterials.Corium
import io.github.symmetricdevs.supercritical.common.registry.ScritRegistration
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.FluidTags
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluid
import java.util.function.Supplier

class MoltenCorium(
    fluid: Supplier<out FlowingFluid>,
    properties: Properties,
) : LiquidBlock(fluid, properties) {

    @Suppress(
        "DEPRECATION",
        "OVERRIDE_DEPRECATION"
    ) // entityInside is the correct 1.20.1 hook for in-fluid entity effects.
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        super.entityInside(state, level, pos, entity)
        // Mirrors vanilla Entity#lavaHurt (which never fires for corium — Entity#isInLava keys off
        // ForgeMod.LAVA_TYPE, not our FluidType). 15s burn + 4 lava damage + the burn sound.
        if (!entity.fireImmune()) {
            entity.setSecondsOnFire(15)
            if (entity.hurt(level.damageSources().lava(), 4.0f)) {
                // Entity.random is protected; vanilla lavaHurt uses the entity's own RandomSource for
                // the pitch jitter. A level-side RandomSource gives the same variation without the access.
                entity.playSound(SoundEvents.GENERIC_BURN, 0.4f, 2.0f + level.random.nextFloat() * 0.4f)
            }
        }
    }

    override fun isFireSource(
        state: BlockState, level: LevelReader, pos: BlockPos, direction: Direction
    ): Boolean = true

    override fun isRandomlyTicking(state: BlockState): Boolean = true

    // Fire-spreading logic adapted from legacy BlockMoltenCorium#updateTick (originally BlockStaticLiquid).
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        super.randomTick(state, level, pos, random)
        if (level.gameRules.getBoolean(GameRules.RULE_DOFIRETICK)) {
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
        meltAdjacentBlocks(level, pos)
    }

    // Corium runs far hotter than lava, so — beyond the passive light-driven melt that lava relies on
    // (our luminance 15 already makes adjacent ice/snow self-melt) — it actively dissolves meltable
    // blocks on contact. Ice/frosted ice melt into water (mirrors IceBlock#meltsInto); snow and
    // powder snow are destroyed outright.
    private fun meltAdjacentBlocks(level: ServerLevel, pos: BlockPos) {
        for (direction in Direction.entries) {
            val neighbour = pos.relative(direction)
            if (!level.isLoaded(neighbour)) continue
            val neighbourState = level.getBlockState(neighbour)
            val block = neighbourState.block
            if (block == Blocks.ICE || block == Blocks.FROSTED_ICE) {
                level.setBlockAndUpdate(neighbour, Blocks.WATER.defaultBlockState())
            } else if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || block == Blocks.POWDER_SNOW) {
                level.destroyBlock(neighbour, true)
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

    companion object {
        internal fun register(): Supplier<Fluid> {
            val name = MOLTEN.getRegistryNameFor(Corium)
            val langKey = MOLTEN.getTranslationKeyFor(Corium)
            val still = MOLTEN.iconType.getBlockTexturePath(Corium.materialIconSet, true)
            return ScritRegistration.REGISTRATE
                .createFluid(name, langKey, Corium, still, still)
                .temperature(2500)
                .density(8000)
                .luminance(15)
                .viscosity(10000)
                .color(Corium.materialRGB)
                .state(FluidState.LIQUID)
                .hasBucket(true)
                .also { (it  as GTFluidBuilder<*>)
                    .tag(FluidTags.LAVA)
                    .block(::MoltenCorium)
                    .initialProperties { Blocks.LAVA }
                    .register()
                }
                .registerFluid() as Supplier<Fluid>
        }
    }
}
