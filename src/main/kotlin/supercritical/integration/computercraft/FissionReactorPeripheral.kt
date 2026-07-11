package supercritical.integration.computercraft

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dan200.computercraft.api.peripheral.IPeripheral
import supercritical.common.machine.multiblock.MetaTileEntityFissionReactor

/**
 * CC:Tweaked peripheral wrapping the fission reactor controller. Mirrors the 1.12.2 OpenComputers
 * `DriverFissionReactor` callbacks (see legacy
 * `supercritical.integration.opencomputers.drivers.specific.DriverFissionReactor`), backed by the
 * controller's existing getter/setter API.
 *
 * Reads run on the ComputerCraft Lua thread (they are simple field reads, matching GTCEu's own
 * peripheral sources); the two mutating setters run on the main server thread via
 * [LuaFunction.mainThread] to avoid racing the per-second reactor tick.
 *
 * The legacy OC names `getSmiley` / `setSmiley` are kept as aliases so existing OC programs keep
 * working, with the descriptive `areControlRodsRegulated` / `setControlRodRegulation` names as the
 * primary forms.
 */
class FissionReactorPeripheral(private val reactor: MetaTileEntityFissionReactor) : IPeripheral {

    override fun getType(): String = "fission_reactor"

    override fun getTarget(): Any = reactor

    override fun equals(other: IPeripheral?): Boolean =
        other is FissionReactorPeripheral && other.reactor === reactor

    override fun hashCode(): Int = reactor.hashCode()

    @LuaFunction
    fun getMaxPower(): MethodResult = MethodResult.of(reactor.getMaxPower())

    @LuaFunction
    fun getPower(): MethodResult = MethodResult.of(reactor.getPower())

    @LuaFunction
    fun getMaxTemperature(): MethodResult = MethodResult.of(reactor.getMaxTemperature())

    @LuaFunction
    fun getTemperature(): MethodResult = MethodResult.of(reactor.getTemperature())

    @LuaFunction
    fun getMaxPressure(): MethodResult = MethodResult.of(reactor.getMaxPressure())

    @LuaFunction
    fun getPressure(): MethodResult = MethodResult.of(reactor.getPressure())

    @LuaFunction
    fun getControlRodInsertion(): MethodResult = MethodResult.of(reactor.getControlRodInsertion())

    @LuaFunction(mainThread = true)
    fun setControlRodInsertion(insertion: Double): MethodResult {
        reactor.setControlRodInsertion(insertion)
        return MethodResult.of()
    }

    @LuaFunction(value = ["areControlRodsRegulated", "getSmiley"])
    fun areControlRodsRegulated(): MethodResult = MethodResult.of(reactor.areControlRodsRegulated())

    @LuaFunction(value = ["setControlRodRegulation", "setSmiley"], mainThread = true)
    fun setControlRodRegulation(on: Boolean): MethodResult {
        reactor.setControlRodRegulation(on)
        return MethodResult.of()
    }
}
