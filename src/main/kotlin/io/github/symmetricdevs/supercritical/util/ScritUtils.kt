package io.github.symmetricdevs.supercritical.util

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.ClassicVeinGenerator
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition as MultiblockDefinition
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.common.data.GTOres
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.tterrag.registrate.util.entry.BlockEntry
import io.github.symmetricdevs.supercritical.BuildConfig
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import java.util.Locale
import java.util.function.Consumer
import kotlin.reflect.KProperty

fun scId(path: String): ResourceLocation = BuildConfig.TEMPLATE_RL.withPath(path)

fun String.replace(index: Int, c: Char): String = substring(0, index) + c + substring(index + 1)

inline fun <T : GenericEvent<out F>, reified F> IEventBus.addGenericListener(
    priority: EventPriority = EventPriority.NORMAL,
    receiveCancelled: Boolean = false,
    listener: Consumer<T>,
) = addGenericListener(F::class.java, priority, receiveCancelled, listener)

inline fun oreVein(name: ResourceLocation, crossinline config: GTOreDefinition.() -> Unit): GTOreDefinition {
    return GTOres.create(name) { config(it) }
}

inline fun GTOreDefinition.classicGenerator(crossinline config: ClassicVeinGenerator.() -> Unit): GTOreDefinition {
    return this.classicVeinGenerator { config(it) }
}

inline fun GTOreDefinition.surfaceIndicator(crossinline config: SurfaceIndicatorGenerator.() -> Unit): GTOreDefinition {
    return this.surfaceIndicatorGenerator { config(it) }
}

fun GTRecipeBuilder.outputFluids(material: Material, amount: Int): GTRecipeBuilder =
    outputFluids(material.getFluid(amount))

fun GTRecipeBuilder.notConsumableFluid(material: Material, amount: Int): GTRecipeBuilder =
    notConsumableFluid(material.getFluid(amount))

fun blocks(block: BlockEntry<Block>): TraceabilityPredicate = Predicates.blocks(block.get())

fun blocks(vararg block: BlockEntry<Block>): TraceabilityPredicate = Predicates.blocks(*Array(block.size) { i -> block[i].get() })

val MultiblockDefinition.self: TraceabilityPredicate
    get() = Predicates.controller(Predicates.blocks(this.block))

inline fun <reified T: IMaterialProperty> PropertyKey(name: String) = PropertyKey(name, T::class.java)