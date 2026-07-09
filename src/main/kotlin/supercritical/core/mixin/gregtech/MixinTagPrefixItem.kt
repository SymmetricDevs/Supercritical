package supercritical.core.mixin.gregtech

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.item.TagPrefixItem
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem
import com.gregtechceu.gtceu.common.data.GTDamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import supercritical.api.items.armor.ArmorLogicExtension
import supercritical.api.unification.material.MaterialExtension
import supercritical.api.unification.ore.SCOrePrefix
import supercritical.api.unification.tag.TagPrefixExtension
import java.util.function.DoubleUnaryOperator
import java.util.function.Function

@Mixin(value = [TagPrefixItem::class], remap = false)
abstract class MixinTagPrefixItem {
    @Shadow
    var material: Material? = null

    @Shadow
    var tagPrefix: TagPrefix? = null

    @Unique
    private fun `sc$handleHeatDamage`(material: Material, entity: LivingEntity) {
        val prefix = tagPrefix
        val level = entity.level()
        var heatDamage: Double
        if (prefix === SCOrePrefix.fuelRodHotDepleted) {
            heatDamage = 2.0
        } else if (material.hasProperty<BlastProperty?>(PropertyKey.BLAST)) {
            heatDamage = SC_DEFAULT_HEAT_DAMAGE.applyAsDouble(material.getBlastTemperature().toDouble())
        } else {
            return
        }
        val armor = entity.getItemBySlot(EquipmentSlot.CHEST)
        if (!armor.isEmpty() && armor.getItem() is ArmorComponentItem) {
            heatDamage *= armorItem.getArmorLogic().getHeatResistance().toDouble()
        }
        if (heatDamage > 0.0f) {
            entity.hurt(GTDamageTypes.HEAT.source(level), heatDamage.toFloat())
        } else if (heatDamage < 0.0f) {
            entity.hurt(level.damageSources().freeze(), -heatDamage.toFloat())
        }
    }

    @Unique
    private fun `sc$handleRadiationDamage`(material: Material, entity: LivingEntity) {
        val prefix = tagPrefix
        val radiationFunction: Function<Double?, Double?>? =
            TagPrefixExtension.Companion.getRadiationDamageFunction(prefix)
        if (radiationFunction == null) return

        var radiationDamage: Double =
            radiationFunction.apply(MaterialExtension.Companion.getDecaysPerSecond(material))!!
        val armor = entity.getItemBySlot(EquipmentSlot.CHEST)
        if (!armor.isEmpty() && armor.getItem() is ArmorComponentItem) {
            radiationDamage *= ArmorLogicExtension.Companion.getRadiationResistance(armorItem.getArmorLogic())
                .toDouble()
        }
        if (radiationDamage > 0.0) {
            entity.hurt(GTDamageTypes.RADIATION.source(entity.level()), radiationDamage.toFloat())
        }
    }

    @Inject(method = ["inventoryTick"], at = [At("TAIL")])
    private fun `sc$inventoryTick`(
        itemStack: ItemStack?, level: Level?, entityIn: Entity?, itemSlot: Int, isSelected: Boolean,
        ci: CallbackInfo?
    ) {
        if (entityIn is LivingEntity && entityIn.tickCount % 20 == 0) {
            val material = this.material
            if (material == null || material.isNull()) return
            `sc$handleHeatDamage`(material, entityIn)
            `sc$handleRadiationDamage`(material, entityIn)
        }
    }

    companion object {
        @Unique
        private val SC_DEFAULT_HEAT_DAMAGE =
            DoubleUnaryOperator { temperature: Double -> ((temperature - 1750) / 1000.0f).toFloat() + 2 }
    }
}
