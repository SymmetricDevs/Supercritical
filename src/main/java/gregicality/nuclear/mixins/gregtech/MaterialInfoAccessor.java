package gregicality.nuclear.mixins.gregtech;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.stack.MaterialStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "gregtech.api.unification.material.Material$MaterialInfo", remap = false)
public interface MaterialInfoAccessor {

    @Accessor("componentList")
    ImmutableList<MaterialStack> getComponentList();

    @Accessor("element")
    Element getElement();
}
