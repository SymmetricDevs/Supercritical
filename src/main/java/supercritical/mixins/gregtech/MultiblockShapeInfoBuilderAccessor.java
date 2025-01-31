package supercritical.mixins.gregtech;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.util.BlockInfo;

@Mixin(value = MultiblockShapeInfo.Builder.class, remap = false)
public interface MultiblockShapeInfoBuilderAccessor {

    @Accessor("shape")
    List<String[]> getShape();

    @Accessor("shape")
    void setShape(List<String[]> shape);

    @Accessor("symbolMap")
    void setSymbolMap(Map<Character, BlockInfo> symbolMap);

    @Accessor("symbolMap")
    Map<Character, BlockInfo> getSymbolMap();
}
