package gregicality.nuclear.api.metatileentity.multiblock;

import gregicality.nuclear.api.capability.ICoolantHandler;
import gregicality.nuclear.api.capability.IFuelRodHandler;
import gregicality.nuclear.common.metatileentities.multi.multiblockpart.MetaTileEntityControlRodPort;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import net.minecraftforge.items.IItemHandlerModifiable;

@SuppressWarnings("InstantiationOfUtilityClass")
public class GCYNMultiblockAbility {

    public static final MultiblockAbility<IFuelRodHandler> IMPORT_FUEL_ROD = new MultiblockAbility<>("import_fuel_rod");
    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_FUEL_ROD = new MultiblockAbility<>("export_fuel_rod");
    public static final MultiblockAbility<ICoolantHandler> IMPORT_COOLANT = new MultiblockAbility<>("import_coolant");
    public static final MultiblockAbility<ICoolantHandler> EXPORT_COOLANT = new MultiblockAbility<>("export_coolant");
    public static final MultiblockAbility<MetaTileEntityControlRodPort> CONTROL_ROD_PORT = new MultiblockAbility<>("control_rod_port");
}
