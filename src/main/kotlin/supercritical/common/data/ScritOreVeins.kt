package supercritical.common.data

import com.gregtechceu.gtceu.api.data.worldgen.WorldGenLayers
import com.gregtechceu.gtceu.api.data.worldgen.generator.indicators.SurfaceIndicatorGenerator
import com.gregtechceu.gtceu.common.data.GTMaterials
import net.minecraft.tags.BiomeTags
import net.minecraft.util.valueproviders.UniformInt
import supercritical.util.classicGenerator
import supercritical.util.oreVein
import supercritical.util.scId
import supercritical.util.surfaceIndicator

object ScritOreVeins {

    val ZIRCON = oreVein(scId("zircon_vein")) {
        clusterSize(UniformInt.of(24, 32))
        density(0.4f)
        weight(40)
        layer(WorldGenLayers.STONE)
        heightRangeUniform(10, 50)
        biomes(BiomeTags.IS_OVERWORLD)
        classicGenerator {
            primary { it.mat(GTMaterials.VanadiumMagnetite).size(3) }
            secondary { it.mat(GTMaterials.Apatite).size(2) }
            between { it.mat(GTMaterials.VanadiumMagnetite).size(2) }
            sporadic { it.mat(ScritMaterials.Zircon) }
        }
        surfaceIndicator {
            surfaceRock(ScritMaterials.Zircon)
            placement(SurfaceIndicatorGenerator.IndicatorPlacement.ABOVE)
        }
    }

    fun init() {}
}
