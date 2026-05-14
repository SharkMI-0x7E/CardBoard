package org.cardboardpowered.util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.cardboardpowered.BukkitLogger;

import static com.google.common.base.Preconditions.checkNotNull;

public class RecordMessagePrefixer extends Handler {

    private final Logger parentLogger;
    private final String prefix;

    public RecordMessagePrefixer(Logger parentLogger, String prefix) {
        checkNotNull(parentLogger);
        checkNotNull(prefix);

        this.parentLogger = parentLogger;
        this.prefix = prefix;
    }

    @Override
    public void publish(LogRecord record) {
        // Ideally we would make a copy of the record
        record.setMessage(prefix + record.getMessage());
        parentLogger.log(record);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    /**
     * Register a prefix handler on the given logger.
     *
     * @param logger the logger
     * @param prefix the prefix
     */
    public static void register(Logger logger, String prefix) {
        checkNotNull(logger);

        // Fix issues with multiple classloaders loading the same class
        String className = RecordMessagePrefixer.class.getCanonicalName();

        logger.setUseParentHandlers(false);
        for (Handler handler : logger.getHandlers()) {
            if (handler.getClass().getCanonicalName().equals(className)) {
                logger.removeHandler(handler);
            }
        }
        logger.addHandler(new RecordMessagePrefixer( BukkitLogger.getLogger() , prefix));
    }

}
