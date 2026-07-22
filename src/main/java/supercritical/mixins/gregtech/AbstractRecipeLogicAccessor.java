package supercritical.mixins.gregtech;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public interface AbstractRecipeLogicAccessor {

	@Invoker("getInputTank")
	IMultipleTankHandler inputTank();
}
