package supercritical.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.experimental.UtilityClass;
import supercritical.SCValues;

@UtilityClass
public final class SCLog {

    public static Logger logger = LogManager.getLogger(SCValues.MODID);
}
