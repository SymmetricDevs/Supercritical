package supercritical.api.unification.material.properties

import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import supercritical.api.nuclear.fission.IModeratorStats

class ModeratorProperty private constructor(builder: Builder) : IMaterialProperty, IModeratorStats {
    override val maxTemperature: Int
    override val moderationFactor: Double
    override val absorptionFactor: Double

    init {
        this.maxTemperature = builder.maxTemperature
        this.moderationFactor = builder.moderationFactor
        this.absorptionFactor = builder.absorptionFactor
    }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet<DustProperty?>(PropertyKey.DUST, true)
    }

    class Builder {
        internal var maxTemperature = 0
        internal var moderationFactor = 0.0
        internal var absorptionFactor = 0.0

        fun maxTemperature(maxTemperature: Int): Builder {
            this.maxTemperature = maxTemperature
            return this
        }

        fun moderationFactor(moderationFactor: Double): Builder {
            this.moderationFactor = moderationFactor
            return this
        }

        fun absorptionFactor(absorptionFactor: Double): Builder {
            this.absorptionFactor = absorptionFactor
            return this
        }

        fun build(): ModeratorProperty {
            return ModeratorProperty(this)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
