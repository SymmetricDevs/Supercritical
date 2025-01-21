package gregicality.nuclear.mixins.gregtech;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Using a String instead of a Class reference here because
 * {@link Material.MaterialInfo} is private.
 * This allows us to indirectly reference the private class in the mixin.
 */
@Mixin(targets = "gregtech.api.unification.material.Material$MaterialInfo", remap = false)
public interface MaterialInfoAccessor {

    @Accessor("componentList")
    ImmutableList<MaterialStack> getComponentList();

    @Accessor("element")
    Element getElement();
}
