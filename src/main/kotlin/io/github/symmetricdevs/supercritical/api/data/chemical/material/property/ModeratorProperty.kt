package io.github.symmetricdevs.supercritical.api.data.chemical.material.property

import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import io.github.symmetricdevs.supercritical.api.fission.stats.ModeratorStats

class ModeratorProperty(
    override val maxTemperature: Double,
    override val moderationFactor: Double,
    override val absorptionFactor: Double,
) : IMaterialProperty, ModeratorStats {

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet<DustProperty?>(PropertyKey.DUST, true)
    }
}
