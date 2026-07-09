package supercritical.api.nuclear.fission

class ModeratorStats(
    private val maxTemperature: Int,
    private val moderationFactor: Double,
    private val absorptionFactor: Double
) : IModeratorStats {
    override fun getMaxTemperature(): Int {
        return maxTemperature
    }

    override fun getModerationFactor(): Double {
        return moderationFactor
    }

    override fun getAbsorptionFactor(): Double {
        return absorptionFactor
    }
}
