package supercritical.api.nuclear.fission.components

import kotlin.math.pow
import kotlin.math.sqrt

open class ReactorComponent(
    val moderationFactor: Double, var maxTemperature: Double, val thermalConductivity: Double, val mass: Double,
    val isValid: Boolean
) {
    var x: Int = 0
        private set
    var y: Int = 0
        private set
    var index: Int = -1

    fun setPos(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    open fun getAbsorptionFactor(controlsInserted: Boolean, thermal: Boolean): Double {
        return 0.0
    }

    fun samePositionAs(component: ReactorComponent): Boolean {
        return x == component.x && y == component.y
    }

    fun getDistance(component: ReactorComponent): Double {
        return sqrt((x - component.x).toDouble().pow(2.0) + (y - component.y).toDouble().pow(2.0))
    }
}
