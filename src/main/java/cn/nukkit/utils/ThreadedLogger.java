package cn.nukkit.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class ThreadedLogger extends Thread {
    public Logger log;

    public ThreadedLogger(){
        log = LogManager.getLogger();
    }
}
