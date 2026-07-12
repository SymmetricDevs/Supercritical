package io.github.symmetricdevs.supercritical.api.data.chemical.material.info

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType

object ScritMaterialIconType {
    // Nuclear MaterialIconTypes
    val fuelRod: MaterialIconType = MaterialIconType("fuelRod")
    val fuelRodDepleted: MaterialIconType = MaterialIconType("fuelRodDepleted")
    val fuelRodHotDepleted: MaterialIconType = MaterialIconType("fuelRodHotDepleted")
    val fuelPelletRaw: MaterialIconType = MaterialIconType("fuelPelletRaw")
    val fuelPellet: MaterialIconType = MaterialIconType("fuelPellet")
    val fuelPelletDepleted: MaterialIconType = MaterialIconType("fuelPelletDepleted")

    val dustSpentFuel: MaterialIconType = MaterialIconType("dustSpentFuel")
    val dustBredFuel: MaterialIconType = MaterialIconType("dustBredFuel")
    val dustFissionByproduct: MaterialIconType = MaterialIconType("dustFissionByproduct")
}
