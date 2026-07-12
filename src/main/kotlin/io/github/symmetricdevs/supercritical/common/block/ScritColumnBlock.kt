package io.github.symmetricdevs.supercritical.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Block class for the gas centrifuge column — a centered 10×16×10 pillar whose visual model spans
 * [3,0,3]→[13,16,13]. Overrides both the render/selection shape ([getShape]) and the collision shape
 * ([getCollisionShape]) so they match the pillar instead of defaulting to a full 16³ cube.
 *
 * Everything else (BlockItem, blockstate, loot, lang) is co-registered by the GTRegistrate helper
 * exactly as it would be for a plain [Block]; only the bounding shape differs.
 */
class ScritColumnBlock(properties: Properties) : Block(properties) {

    @Suppress("OVERRIDE_DEPRECATION") // Canonical custom-shape override (cf. GTCEu MinerPipeBlock); BlockState delegates here.
    override fun getShape(
        state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext
    ): VoxelShape = SHAPE

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getCollisionShape(
        state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext
    ): VoxelShape = SHAPE

    companion object {
        /** Centered 10×16×10 pillar bounds, matching the column's block-model element. */
        val SHAPE: VoxelShape = box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0)
    }
}
