package io.github.symmetricdevs.supercritical.api.nuclear.fission

interface IModeratorStats {
    val maxTemperature: Int

    val moderationFactor: Double

    val absorptionFactor: Double
}
