package gregicality.nuclear.common;

import gregicality.nuclear.GCYNValues;
import gregicality.nuclear.common.materials.GCYNElementMaterials;
import gregicality.nuclear.common.materials.GCYNFirstDegreeMaterials;
import gregicality.nuclear.common.materials.GCYNMaterialModifications;
import gregicality.nuclear.common.materials.GCYNSecondDegreeMaterials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = GCYNValues.MODID)
public final class GCYNEventHandlers {

    private GCYNEventHandlers() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerMaterials(MaterialEvent event) {

        GCYNElementMaterials.register();
        GCYNFirstDegreeMaterials.register();
        GCYNSecondDegreeMaterials.register();
    }

    @SubscribeEvent
    public static void registerMaterialsPost(PostMaterialEvent event) {
        GCYNMaterialModifications.init();
    }
}
