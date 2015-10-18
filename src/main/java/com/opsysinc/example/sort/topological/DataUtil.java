package com.opsysinc.example.sort.topological;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Basic data utils.
 *
 * @author mkitchin
 */
public final class DataUtil {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DataUtil.class.getName());

    /**
     * Checks for null/empty string.
     *
     * @param input     String to check.
     * @param isToThrow True to throw IAE on null/empty, false otherwise.
     * @return True if null/empty, false otherwise.
     */
    public static boolean checkEmptyString(final Object input,
                                           final boolean isToThrow) {

        final boolean result = (input == null)
                || input.toString().trim().isEmpty();

        try {

            if (result && isToThrow) {

                throw new IllegalArgumentException("missing input (null/empty)");
            }

        } catch (final Throwable ex) {

            DataUtil.LOGGER.log(Level.WARNING, "checkEmptyString()", ex);
            throw ex;
        }

        return result;
    }

    /**
     * Checks for null object.
     *
     * @param input     Object to check.
     * @param isToThrow True to throw IAE on null, false otherwise.
     * @return True if null, false otherwise.
     */
    public static boolean checkNullObject(final Object input,
                                          final boolean isToThrow) {

        final boolean result = input == null;

        try {

            if (result && isToThrow) {

                throw new IllegalArgumentException("missing input (null)");
            }

        } catch (final Throwable ex) {

            DataUtil.LOGGER.log(Level.WARNING, "checkNullObject()", ex);
            throw ex;
        }

        return result;
    }
}
