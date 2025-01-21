package gregicality.nuclear.mixins.gregtech;

import gregicality.nuclear.api.items.armor.IRadiationResistanceArmorLogic;
import gregicality.nuclear.api.unification.material.MaterialExtension;
import gregicality.nuclear.api.unification.material.properties.GCYNPropertyKey;
import gregicality.nuclear.api.unification.ore.OrePrefixExtension;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
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

@Mixin(value = MetaPrefixItem.class, remap = false)
public abstract class MixinMetaPrefixItem extends StandardMetaItem {

    @Shadow
    @Final
    private OrePrefix prefix;

    @Shadow
    public abstract Material getMaterial(@NotNull ItemStack stack);

    @Unique
    private void gcyn$handleHeatDamage(@NotNull Material material, @NotNull EntityLivingBase entity) {
        float heatDamage = 0.f;
        if (material.hasProperty(PropertyKey.BLAST)) {
            heatDamage = prefix.heatDamageFunction.apply(material.getBlastTemperature());
        } else if (material.hasProperty(GCYNPropertyKey.FISSION_FUEL)) {
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
    private void gcyn$handleRadiationDamage(@NotNull Material material, EntityLivingBase entity) {
        double radiationDamage = ((OrePrefixExtension) prefix).getDamageFunction()
                .apply(((MaterialExtension) material).getDecaysPerSecond());
        ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
            ArmorMetaItem<?>.ArmorMetaValueItem metaValueItem = ((ArmorMetaItem<?>) armor.getItem())
                    .getItem(armor);
            if (metaValueItem != null) {
                radiationDamage *= ((IRadiationResistanceArmorLogic) (metaValueItem.getArmorLogic())).getRadiationResistance();
            }
        }
        if (radiationDamage > 0.0) {
            entity.attackEntityFrom(DamageSources.getRadioactiveDamage().setDamageBypassesArmor(),
                    (float) radiationDamage);
        }
    }

    /**
     * @author MCTian-mi
     * @reason Can't really figure out a better way than this...
     */
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
                        gcyn$handleHeatDamage(material, entity);
                    }

                    // Handle radiation damage
                    if (((OrePrefixExtension) prefix).getDamageFunction() != null) {
                        gcyn$handleRadiationDamage(material, entity);
                    }
                }
            }
        }
    }
}
