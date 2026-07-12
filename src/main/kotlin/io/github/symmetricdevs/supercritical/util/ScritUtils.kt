package io.github.symmetricdevs.supercritical.util

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.ClassicVeinGenerator
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.common.data.GTOres
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import io.github.symmetricdevs.supercritical.BuildConfig
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import java.util.function.Consumer

fun scId(path: String): ResourceLocation = BuildConfig.TEMPLATE_RL.withPath(path)

fun String.replace(index: Int, c: Char): String = substring(0, index) + c + substring(index + 1)

inline fun <T : GenericEvent<out F>, reified F> IEventBus.addGenericListener(
    priority: EventPriority = EventPriority.NORMAL,
    receiveCancelled: Boolean = false,
    listener: Consumer<T>,
) = addGenericListener(F::class.java, priority, receiveCancelled, listener)

inline fun <reified T> nullWrongType(actual: MetaMachine): ModifierFunction {
    return RecipeModifier.nullWrongType(T::class.java, actual)
}

inline fun oreVein(name: ResourceLocation, crossinline config: GTOreDefinition.() -> Unit): GTOreDefinition {
    return GTOres.create(name) { config(it) }
}

inline fun GTOreDefinition.classicGenerator(crossinline config: ClassicVeinGenerator.() -> Unit): GTOreDefinition {
    return this.classicVeinGenerator { config(it) }
}

inline fun GTOreDefinition.surfaceIndicator(crossinline config: SurfaceIndicatorGenerator.() -> Unit): GTOreDefinition {
    return this.surfaceIndicatorGenerator { config(it) }
}

fun GTRecipeBuilder.outputFluids(material: Material, amount: Int): GTRecipeBuilder = outputFluids(material.getFluid(amount))

fun GTRecipeBuilder.notConsumableFluid(material: Material, amount: Int): GTRecipeBuilder = notConsumableFluid(material.getFluid(amount))