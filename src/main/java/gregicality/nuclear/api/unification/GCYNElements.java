package gregicality.nuclear.api.unification;

import gregtech.api.unification.Element;

import static gregtech.api.unification.Elements.add;

public class GCYNElements {

    // TODO: mixin into Element.class, making half-life seconds a double instead of long
    public static final Element U239 = add(92, 147, -1, null, "Uranium-239", "U-239", true);
    public static final Element Np235 = add(93, 142, -1, null, "Neptunium-235", "Np-235", true);
    public static final Element Np236 = add(93, 143, -1, null, "Neptunium-236", "Np-236", true);
    public static final Element Np237 = add(93, 144, -1, null, "Neptunium-237", "Np-237", true);
    public static final Element Np239 = add(93, 146, -1, null, "Neptunium-239", "Np-239", true);
    public static final Element Pu238 = add(94, 144, -1, null, "Plutonium-238", "Pu-238", true);
    public static final Element Pu240 = add(94, 146, -1, null, "Plutonium-240", "Pu-240", true);
    public static final Element Pu242 = add(94, 148, -1, null, "Plutonium-242", "Pu-242", true);
    public static final Element Pu244 = add(94, 150, -1, null, "Plutonium-244", "Pu-244", true);

}
