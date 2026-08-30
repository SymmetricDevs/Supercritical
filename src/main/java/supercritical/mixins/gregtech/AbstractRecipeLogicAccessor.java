package supercritical.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public interface AbstractRecipeLogicAccessor {

    @Invoker("getInputTank")
    IMultipleTankHandler inputTank();
}
