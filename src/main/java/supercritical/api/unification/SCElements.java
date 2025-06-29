package supercritical.api.unification;

import static gregtech.api.unification.Elements.*;

import gregtech.api.unification.Element;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ElementExtension.Handler.class)
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
        U.setHalfLiveSeconds(1.4090285e+17);
        U238.setHalfLiveSeconds(1.4090285e+17);
        U235.setHalfLiveSeconds(2.2195037e+16);
        U239.setHalfLiveSeconds(1407);

        Np.setHalfLiveSeconds(-1);
        Np235.setHalfLiveSeconds(-1);
        Np236.setHalfLiveSeconds(-1);
        Np237.setHalfLiveSeconds(-1);
        Np239.setHalfLiveSeconds(-1);

        Pu.setHalfLiveSeconds(-1);
        Pu238.setHalfLiveSeconds(2765707200d);
        Pu239.setHalfLiveSeconds(760332960000d);
        Pu240.setHalfLiveSeconds(206907696000d);
        Pu241.setHalfLiveSeconds(450649440d);
        Pu242.setHalfLiveSeconds(1.1826e+13);
        Pu244.setHalfLiveSeconds(2.52288e+15);
    }
}
