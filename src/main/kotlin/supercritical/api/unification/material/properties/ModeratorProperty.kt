package supercritical.api.unification.material.properties

import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import supercritical.api.nuclear.fission.IModeratorStats

class ModeratorProperty private constructor(builder: Builder) : IMaterialProperty, IModeratorStats {
    private val maxTemperature: Int
    private val moderationFactor: Double
    private val absorptionFactor: Double

    init {
        this.maxTemperature = builder.maxTemperature
        this.moderationFactor = builder.moderationFactor
        this.absorptionFactor = builder.absorptionFactor
    }

    override fun verifyProperty(properties: MaterialProperties) {
        properties.ensureSet<DustProperty?>(PropertyKey.DUST, true)
    }

    override fun getMaxTemperature(): Int {
        return maxTemperature
    }

    override fun getModerationFactor(): Double {
        return moderationFactor
    }

    override fun getAbsorptionFactor(): Double {
        return absorptionFactor
    }

    class Builder {
        private var maxTemperature = 0
        private var moderationFactor = 0.0
        private var absorptionFactor = 0.0

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
