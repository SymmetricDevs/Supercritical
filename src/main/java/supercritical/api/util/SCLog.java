package supercritical.api.util;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import supercritical.Tags;

@UtilityClass
public final class SCLog {

	public static Logger logger = LogManager.getLogger(Tags.MOD_ID);
}
