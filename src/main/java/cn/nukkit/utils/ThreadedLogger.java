package cn.nukkit.utils;

import org.apache.logging.log4j.LogManager;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public abstract class ThreadedLogger extends Thread implements Logger {
	public org.apache.logging.log4j.Logger log;
	public ThreadedLogger() {
		log = LogManager.getLogger();
	}
}
