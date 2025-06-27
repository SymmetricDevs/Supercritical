package supercritical.mixins.gregtech;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import lombok.experimental.ExtensionMethod;
import supercritical.api.items.armor.ArmorLogicExtension;
import supercritical.api.unification.material.MaterialExtension;
import supercritical.api.unification.material.properties.SCPropertyKey;
import supercritical.api.unification.ore.OrePrefixExtension;

@Mixin(value = MetaPrefixItem.class, remap = false)
@ExtensionMethod({
        OrePrefixExtension.Handler.class,
        MaterialExtension.Handler.class,
        ArmorLogicExtension.Handler.class
})
public abstract class MixinMetaPrefixItem extends StandardMetaItem {

    @Shadow
    @Final
    private OrePrefix prefix;

    @Shadow
    public abstract Material getMaterial(@NotNull ItemStack stack);

    @Unique
    private void sc$handleHeatDamage(@NotNull Material material, @NotNull EntityLivingBase entity) {
        float heatDamage = 0.f;
        if (material.hasProperty(PropertyKey.BLAST)) {
            heatDamage = prefix.heatDamageFunction.apply(material.getBlastTemperature());
        } else if (material.hasProperty(SCPropertyKey.FISSION_FUEL)) {
            heatDamage = prefix.heatDamageFunction.apply(0);
        }
        ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
            ArmorMetaItem<?>.ArmorMetaValueItem metaValueItem = ((ArmorMetaItem<?>) armor.getItem())
                    .getItem(armor);
            if (metaValueItem != null) heatDamage *= metaValueItem.getArmorLogic().getHeatResistance();
        }
        if (heatDamage > 0.0) {
            entity.attackEntityFrom(DamageSources.getHeatDamage().setDamageBypassesArmor(), heatDamage);
        } else if (heatDamage < 0.0) {
            entity.attackEntityFrom(DamageSources.getFrostDamage().setDamageBypassesArmor(),
                    -heatDamage);
        }
    }

    @Unique
    private void sc$handleRadiationDamage(@NotNull Material material, EntityLivingBase entity) {
        double radiationDamage = prefix.getRadiationDamageFunction()
                .apply(material.getDecaysPerSecond());
        ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
            ArmorMetaItem<?>.ArmorMetaValueItem metaValueItem = ((ArmorMetaItem<?>) armor.getItem())
                    .getItem(armor);
            if (metaValueItem != null) {
                radiationDamage *= metaValueItem.getArmorLogic().getRadiationResistance();
            }
        }
        if (radiationDamage > 0.0) {
            entity.attackEntityFrom(DamageSources.getRadioactiveDamage().setDamageBypassesArmor(),
                    (float) radiationDamage);
        }
    }

    // Can't really figure out a better way than a total overwrite like this...
    @Override
    public void onUpdate(@NotNull ItemStack itemStack, @NotNull World worldIn, @NotNull Entity entityIn, int itemSlot,
                         boolean isSelected) {
        super.onUpdate(itemStack, worldIn, entityIn, itemSlot, isSelected);
        if (metaItems.containsKey((short) itemStack.getItemDamage()) && entityIn instanceof EntityLivingBase entity) {
            if (entityIn.ticksExisted % 20 == 0) {
                Material material = getMaterial(itemStack);
                if (material != null) {
                    // Handle heat damage
                    if (prefix.heatDamageFunction != null) {
                        sc$handleHeatDamage(material, entity);
                    }

                    // Handle radiation damage
                    if (prefix.getRadiationDamageFunction() != null) {
                        sc$handleRadiationDamage(material, entity);
                    }
                }
            }
        }
    }
}
