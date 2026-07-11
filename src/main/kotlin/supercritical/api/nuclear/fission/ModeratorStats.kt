package supercritical.api.nuclear.fission

class ModeratorStats(
    override val maxTemperature: Int,
    override val moderationFactor: Double,
    override val absorptionFactor: Double
) : IModeratorStats
