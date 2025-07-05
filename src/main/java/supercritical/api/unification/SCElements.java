package supercritical.api.unification;

import static gregtech.api.unification.Elements.*;

import gregtech.api.unification.Element;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(ElementExtension.Handler.class)
public class SCElements {

    public static final Element U234 = add(92, 142, -1, null, "Uranium-234", "U-234", true);
    public static final Element U236 = add(92, 144, -1, null, "Uranium-236", "U-236", true);
    public static final Element U239 = add(92, 147, -1, null, "Uranium-239", "U-239", true);

    public static final Element Np235 = add(93, 142, -1, null, "Neptunium-235", "Np-235", true);
    public static final Element Np236 = add(93, 143, -1, null, "Neptunium-236", "Np-236", true);
    public static final Element Np237 = add(93, 144, -1, null, "Neptunium-237", "Np-237", true);
    public static final Element Np239 = add(93, 146, -1, null, "Neptunium-239", "Np-239", true);

    public static final Element Pu238 = add(94, 144, -1, null, "Plutonium-238", "Pu-238", true);
    public static final Element Pu240 = add(94, 146, -1, null, "Plutonium-240", "Pu-240", true);
    public static final Element Pu242 = add(94, 148, -1, null, "Plutonium-242", "Pu-242", true);
    public static final Element Pu244 = add(94, 150, -1, null, "Plutonium-244", "Pu-244", true);

    public static final Element Am241 = add(95, 146, -1, null, "Americium-241", "Am-241", true);
    public static final Element Am243 = add(95, 148, -1, null, "Americium-243", "Am-243", true);

    public static final Element Cm244 = add(96, 148, -1, null, "Curium-244", "Cm-244", true);
    public static final Element Cm245 = add(96, 149, -1, null, "Curium-245", "Cm-245", true);
    public static final Element Cm246 = add(96, 150, -1, null, "Curium-246", "Cm-246", true);

    static {
        U.setHalfLiveSeconds(1.4090285e+17);
        U234.setHalfLiveSeconds(7.7472253e+12);
        U236.setHalfLiveSeconds(7.4046528e+14);
        U238.setHalfLiveSeconds(1.4090285e+17);
        U235.setHalfLiveSeconds(2.2195037e+16);
        U239.setHalfLiveSeconds(1407);

        Np235.setHalfLiveSeconds(34223040);
        Np236.setHalfLiveSeconds(1.33056e+10);
        Np237.setHalfLiveSeconds(6.76801391e+13);
        Np239.setHalfLiveSeconds(66200371);

        Pu238.setHalfLiveSeconds(2765707200d);
        Pu239.setHalfLiveSeconds(760332960000d);
        Pu240.setHalfLiveSeconds(206907696000d);
        Pu241.setHalfLiveSeconds(450649440d);
        Pu242.setHalfLiveSeconds(1.1826e+13);
        Pu244.setHalfLiveSeconds(2.52288e+15);

        Am241.setHalfLiveSeconds(1.36515262e+10);
        Am243.setHalfLiveSeconds(2.31943406e+11);

        Cm244.setHalfLiveSeconds(571590600);
        Cm245.setHalfLiveSeconds(2.60366729e+13);
        Cm246.setHalfLiveSeconds(1.48519516e+13);
    }
}
