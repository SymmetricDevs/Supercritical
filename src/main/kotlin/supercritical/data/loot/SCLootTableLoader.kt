package supercritical.data.loot

import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraftforge.registries.ForgeRegistries
import supercritical.common.registry.SCBlocks
import java.util.function.BiConsumer

/**
 * Generates block loot tables for Supercritical's raw-DeferredRegister blocks.
 *
 * The legacy 1.12.2 casings were GregTech VariantBlocks whose item form was the block
 * itself (they dropped themselves). The modern blocks are plain `Block` instances
 * registered through a bare `DeferredRegister`, so they receive no Registrate loot
 * callback and would otherwise drop nothing. This provider restores the legacy
 * self-drop behaviour by emitting a single-item loot table per block.
 *
 * The raw `addLootAction(LootContextParamSet.BLOCK, ...)` entry point is used because
 * `RegistrateBlockLootTables.dropSelf` resolves to the protected
 * `BlockLootSubProvider.dropSelf` (GCYR widens it via an access transformer, which an
 * addon cannot add). Building the table from the public Loot API and registering it
 * through the BiConsumer sink yields the same self-drop JSON that `dropSelf` produces.
 */
object SCLootTableLoader {
    fun init(provider: RegistrateLootTableProvider?) {
        provider?.addLootAction(LootContextParamSets.BLOCK) { sink ->
            // Fission casing variants (legacy BlockFissionCasing).
            selfDrop(sink, SCBlocks.REACTOR_VESSEL.get())
            selfDrop(sink, SCBlocks.FUEL_CHANNEL.get())
            selfDrop(sink, SCBlocks.CONTROL_ROD_CHANNEL.get())
            selfDrop(sink, SCBlocks.COOLANT_CHANNEL.get())
            // Nuclear / gas-centrifuge casings (legacy BlockNuclearCasing / BlockGasCentrifugeCasing).
            selfDrop(sink, SCBlocks.SPENT_FUEL_CASING.get())
            selfDrop(sink, SCBlocks.GAS_CENTRIFUGE_HEATER.get())
            selfDrop(sink, SCBlocks.GAS_CENTRIFUGE_COLUMN.get())
            // Panelling, all 16 dye colors (legacy BlockPanelling dropped itself).
            for (block in SCBlocks.PANELLING.values) {
                selfDrop(sink, block.get())
            }
        }
    }

    private fun selfDrop(sink: BiConsumer<ResourceLocation, LootTable.Builder>, block: Block) {
        val id = ForgeRegistries.BLOCKS.getKey(block)
            ?: error("Block ${block::class.simpleName} has no registry name during loot datagen")
        // Block loot tables live at <namespace>:blocks/<path>, matching BlockBehaviour.getLootTable().
        val tableId = ResourceLocation(id.namespace, "blocks/" + id.path)
        val table: LootTable.Builder = LootTable.lootTable()
            .withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0f))
                    .add(LootItem.lootTableItem(block))
            )
        sink.accept(tableId, table)
    }
}
