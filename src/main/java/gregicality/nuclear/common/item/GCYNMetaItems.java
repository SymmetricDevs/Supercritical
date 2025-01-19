package gregicality.nuclear.common.item;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.StandardMetaItem;

public class GCYNMetaItems {

    // Nuclear
    public static MetaValueItem ANODE_BASKET;
    public static MetaValueItem FUEL_CLADDING;

    private static StandardMetaItem metaItem;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("gcyn_meta_item");
    }

    public static void initSubitems() {
        GCYNMetaItems.ANODE_BASKET = metaItem.addItem(0, "basket.anode");
        GCYNMetaItems.FUEL_CLADDING = metaItem.addItem(1, "cladding.fuel");
    }
}
