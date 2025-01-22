package supercritical.api.unification;

import static gregtech.api.unification.Elements.*;

import gregtech.api.unification.Element;

public class SCElements {

    public static final Element U239 = add(92, 147, -1, null, "Uranium-239", "U-239", true);
    public static final Element Np235 = add(93, 142, -1, null, "Neptunium-235", "Np-235", true);
    public static final Element Np236 = add(93, 143, -1, null, "Neptunium-236", "Np-236", true);
    public static final Element Np237 = add(93, 144, -1, null, "Neptunium-237", "Np-237", true);
    public static final Element Np239 = add(93, 146, -1, null, "Neptunium-239", "Np-239", true);
    public static final Element Pu238 = add(94, 144, -1, null, "Plutonium-238", "Pu-238", true);
    public static final Element Pu240 = add(94, 146, -1, null, "Plutonium-240", "Pu-240", true);
    public static final Element Pu242 = add(94, 148, -1, null, "Plutonium-242", "Pu-242", true);
    public static final Element Pu244 = add(94, 150, -1, null, "Plutonium-244", "Pu-244", true);

    static {
        setHalfLiveSeconds(U, 1.4090285e+17);
        setHalfLiveSeconds(U238, 1.4090285e+17);
        setHalfLiveSeconds(U235, 2.2195037e+16);
        setHalfLiveSeconds(U239, 1407);

        setHalfLiveSeconds(Np, -1);
        setHalfLiveSeconds(Np235, -1);
        setHalfLiveSeconds(Np236, -1);
        setHalfLiveSeconds(Np237, -1);
        setHalfLiveSeconds(Np239, -1);

        setHalfLiveSeconds(Pu, -1);
        setHalfLiveSeconds(Pu238, 2765707200d);
        setHalfLiveSeconds(Pu239, 760332960000d);
        setHalfLiveSeconds(Pu240, 206907696000d);
        setHalfLiveSeconds(Pu241, 450649440d);
        setHalfLiveSeconds(Pu242, 1.1826e+13);
        setHalfLiveSeconds(Pu244, 2.52288e+15);
    }

    private static void setHalfLiveSeconds(Element element, double halfLife) {
        ((ElementExtension) element).setHalfLiveSeconds(halfLife);
    }
}
