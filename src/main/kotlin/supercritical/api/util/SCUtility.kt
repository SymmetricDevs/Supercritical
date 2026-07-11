package supercritical.api.util

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import supercritical.BuildConfig
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