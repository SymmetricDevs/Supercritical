package supercritical.integration.opencomputers.drivers.specific

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import li.cil.oc.api.Network
import li.cil.oc.api.driver.NamedBlock
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.Visibility
import li.cil.oc.api.prefab.AbstractManagedEnvironment
import li.cil.oc.api.prefab.DriverSidedTileEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import supercritical.common.machine.multiblock.fission.FissionReactor
import kotlin.jvm.java

/**
 * OpenComputers Community Edition (OC:CE) driver exposing the fission reactor controller to OC
 * adapters. Faithful port of the 1.12.2
 * `supercritical.integration.opencomputers.drivers.specific.DriverFissionReactor`.
 *
 * The 1.12.2 driver extended GTCEu's `EnvironmentMetaTileEntity` helper, which GTCEu Modern no
 * longer ships. That helper was itself a thin wrapper around OC's [AbstractManagedEnvironment]
 * (it called `setNode(Network.newNode(this, Visibility.Network).withComponent(name).create())` and
 * implemented [NamedBlock]); [EnvironmentFissionReactor] reproduces that directly so no GTCEu OC
 * glue is needed.
 *
 * The component name `gt_fissionReactor` is preserved so existing OC programs that address the
 * component by name keep working.
 *
 * Callback names mirror the 1.12.2 OC driver exactly (`getSmiley`/`setSmiley` included), and the
 * descriptive `areControlRodsRegulated`/`setControlRodRegulation` names from the CC:Tweaked
 * peripheral [supercritical.integration.computercraft.FissionReactorPeripheral] are also exposed
 * so OC programs can use either convention.
 *
 * Per the `@Callback` contract, callbacks default to `direct = false`, i.e. they are queued and
 * executed on the server thread — so the two mutating setters are main-thread-safe without any
 * special handling, matching the CC:T peripheral's `mainThread = true` semantics.
 */
class DriverFissionReactor : DriverSidedTileEntity() {

    // The actual BlockEntity at a controller position is an IMachineBlockEntity; the FissionReactor
    // is its MetaMachine. worksWith is overridden (mirroring the 1.12.2 driver) to check the
    // meta-machine type, since the default worksWith only checks the BlockEntity class.
    override fun getTileEntityClass(): Class<*> = IMachineBlockEntity::class.java

    override fun worksWith(world: Level, pos: BlockPos, side: Direction): Boolean {
        val holder = world.getBlockEntity(pos) as? IMachineBlockEntity ?: return false
        return holder.metaMachine is FissionReactor
    }

    override fun createEnvironment(world: Level, pos: BlockPos, side: Direction): EnvironmentFissionReactor? {
        val holder = world.getBlockEntity(pos) as? IMachineBlockEntity ?: return null
        val machine = holder.metaMachine
        return if (machine is FissionReactor) EnvironmentFissionReactor(machine) else null
    }

    /**
     * OC environment backing the fission reactor callbacks. Creates a network-visible component
     * named `gt_fissionReactor` (the 1.12.2 name) so an OC Adapter block facing the controller
     * exposes the reactor methods to connected computers. OC re-invokes `createEnvironment` each
     * time an adapter (re)connects, so holding the controller reference directly is safe — it is
     * refreshed on every adapter load, exactly like the CC:T peripheral provider.
     */
    class EnvironmentFissionReactor(private val reactor: FissionReactor) : AbstractManagedEnvironment(),
        NamedBlock {

        init {
            setNode(Network.newNode(this, Visibility.Network).withComponent(COMPONENT_NAME).create())
        }

        @Callback(doc = "function():number --  Returns the max power of the reactor, in MW.")
        fun getMaxPower(context: Context, args: Arguments): Array<Any?> = arrayOfAny(reactor.getMaxPower())

        @Callback(doc = "function():number --  Returns the power of the reactor, in MW.")
        fun getPower(context: Context, args: Arguments): Array<Any?> = arrayOfAny(reactor.getPower())

        @Callback(doc = "function():number --  Returns the max temperature of the reactor.")
        fun getMaxTemperature(context: Context, args: Arguments): Array<Any?> =
            arrayOfAny(reactor.getMaxTemperature())

        @Callback(doc = "function():number --  Returns the temperature of the reactor.")
        fun getTemperature(context: Context, args: Arguments): Array<Any?> = arrayOfAny(reactor.getTemperature())

        @Callback(doc = "function():number --  Returns the max pressure of the reactor, in pascals.")
        fun getMaxPressure(context: Context, args: Arguments): Array<Any?> = arrayOfAny(reactor.getMaxPressure())

        @Callback(doc = "function():number --  Returns the pressure of the reactor, in pascals.")
        fun getPressure(context: Context, args: Arguments): Array<Any?> = arrayOfAny(reactor.getPressure())

        @Callback(doc = "function():number --  Returns how much control rods are inserted, in [0, 1]")
        fun getControlRodInsertion(context: Context, args: Arguments): Array<Any?> =
            arrayOfAny(reactor.getControlRodInsertion())

        @Callback(doc = "function(insertion:number) --  Sets how much control rods are inserted, in [0, 1]")
        fun setControlRodInsertion(context: Context, args: Arguments): Array<Any?> {
            reactor.setControlRodInsertion(args.checkDouble(0))
            return emptyArray()
        }

        @Callback(doc = "function():boolean --  Returns whether control rods are automatically regulated.")
        fun areControlRodsRegulated(context: Context, args: Arguments): Array<Any?> =
            arrayOfAny(reactor.areControlRodsRegulated())

        @Callback(doc = "function(on:boolean) --  Pass in true to enable control rod regulation, false to disable.")
        fun setControlRodRegulation(context: Context, args: Arguments): Array<Any?> {
            reactor.setControlRodRegulation(args.checkBoolean(0))
            return emptyArray()
        }

        // Legacy 1.12.2 OC aliases for the regulation getter/setter. Kept so existing OC programs
        // that call getSmiley/setSmiley keep working alongside the descriptive names above.
        @Callback(doc = "function():boolean --  Returns whether smiley is regulating control rods.")
        fun getSmiley(context: Context, args: Arguments): Array<Any?> = arrayOfAny(reactor.areControlRodsRegulated())

        @Callback(doc = "function(smiley:boolean) --  Pass in true to revive smiley, pass in false to kill smiley.")
        fun setSmiley(context: Context, args: Arguments): Array<Any?> {
            reactor.setControlRodRegulation(args.checkBoolean(0))
            return emptyArray()
        }

        override fun preferredName(): String = COMPONENT_NAME

        override fun priority(): Int = 0

        companion object {
            // Matches the 1.12.2 component name (EnvironmentMetaTileEntity ctor arg).
            private const val COMPONENT_NAME = "fission_reactor"

            // OC's @Callback contract requires Object[] (not e.g. Double[]), so box single values
            // into a generic Array<Any?> rather than arrayOf(value) (which would produce a typed
            // array such as Double[] and break OC's reflective return-type check).
            private fun arrayOfAny(value: Any?): Array<Any?> = arrayOf(value)
        }
    }
}
