package supercritical.common.item;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.StandardMetaItem;

public class SCMetaItems {

    // Nuclear
    public static MetaValueItem ANODE_BASKET;
    public static MetaValueItem FUEL_CLADDING;

    private static StandardMetaItem metaItem;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("supercritical_meta_item");
    }

    public static void initSubitems() {
        SCMetaItems.ANODE_BASKET = metaItem.addItem(0, "basket.anode");
        SCMetaItems.FUEL_CLADDING = metaItem.addItem(1, "cladding.fuel");
    }
}
