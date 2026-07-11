package supercritical.core.mixin.gregtech;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercritical.api.items.armor.ArmorLogicExtension;
import supercritical.api.unification.material.MaterialExtension;
import supercritical.api.unification.ore.ScritOrePrefix;
import supercritical.api.unification.tag.TagPrefixExtension;

import java.util.function.Function;

@Mixin(value = TagPrefixItem.class, remap = false)
public abstract class MixinTagPrefixItem {

    @Final
    @Shadow
    public Material material;

    @Final
    @Shadow
    public TagPrefix tagPrefix;

    @Unique
    private void sc$handleHeatDamage(@NotNull LivingEntity entity) {
        // Legacy applied SC heat damage only to hot depleted fuel rods (the sole prefix with a
        // heatDamageFunction). Hot-ingot heat damage is handled natively by GTCEu Modern's
        // TagPrefixItem.inventoryTick (ingotHot + BLAST), so we must not re-apply it here, or it
        // would damage every BLAST item (steel/aluminium/... ingots, plates, dusts) and
        // double-apply for ingotHot.
        TagPrefix prefix = tagPrefix;
        if (prefix != ScritOrePrefix.INSTANCE.getFuelRodHotDepleted()) {
            return;
        }
        Level level = entity.level();
        double heatDamage = 2.0D;
        ItemStack armor = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!armor.isEmpty() && armor.getItem() instanceof ArmorComponentItem armorItem) {
            heatDamage *= armorItem.getArmorLogic().getHeatResistance();
        }
        if (heatDamage > 0.0f) {
            entity.hurt(GTDamageTypes.HEAT.source(level), (float) heatDamage);
        } else if (heatDamage < 0.0f) {
            entity.hurt(level.damageSources().freeze(), (float) -heatDamage);
        }
    }

    @Unique
    private void sc$handleRadiationDamage(@NotNull Material material, @NotNull LivingEntity entity) {
        TagPrefix prefix = tagPrefix;
        Function<Double, Double> radiationFunction = TagPrefixExtension.Companion.getRadiationDamageFunction(prefix);

        double radiationDamage = radiationFunction.apply(MaterialExtension.Companion.getDecaysPerSecond(material));
        ItemStack armor = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!armor.isEmpty() && armor.getItem() instanceof ArmorComponentItem armorItem) {
            radiationDamage *= ArmorLogicExtension.Companion.getRadiationResistance(armorItem.getArmorLogic());
        }
        if (radiationDamage > 0.0) {
            entity.hurt(GTDamageTypes.RADIATION.source(entity.level()), (float) radiationDamage);
        }
    }

    @Inject(method = "inventoryTick", at = @At("TAIL"))
    private void sc$inventoryTick(ItemStack itemStack, Level level, Entity entityIn, int itemSlot, boolean isSelected,
                                  CallbackInfo ci) {
        if (entityIn instanceof LivingEntity entity && entity.tickCount % 20 == 0) {
            Material material = this.material;
            if (material == null || material.isNull()) return;
            sc$handleHeatDamage(entity);
            sc$handleRadiationDamage(material, entity);
        }
    }
}
