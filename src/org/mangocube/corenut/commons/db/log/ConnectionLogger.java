/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.db.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO dengtailin: Change to the actual description of this class
 * @version   Revision History
 * <pre>
 * Author     Version       Date        Changes
 * Allen      1.0           2010-12-29     Created
 *
 * </pre>
 * @since 1.0
 */
public class ConnectionLogger {
    private static final Map<String, LogInfo> OPEN_CONN_CACHE = new HashMap<String, LogInfo>();
    private static final Map<String, LogInfo> CLOSE_CONN_CACHE = new HashMap<String, LogInfo>();
    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int DEFAULT_MAX_CACHE_SIZE = 5000;
    
    private static volatile boolean turnOnLog = true;
    private static int clearCount = 0;
    
    public static synchronized void turnOnLog(boolean isOn) {
    	turnOnLog = isOn;
    	OPEN_CONN_CACHE.clear();
    	CLOSE_CONN_CACHE.clear();
    }

    private static class LogInfo {
    	private AtomicInteger count = new AtomicInteger(0);
    	String stackTrace;
    	
    	public void increase() {
    		count.incrementAndGet();
    	}
    	
    	public int getCount() {
    		return count.get();
    	}
    }
    
    public static void openConnectionLog(Object conn, String id) {
    	if (!turnOnLog) return;
    	
    	synchronized(ConnectionLogger.class) {
	        //logging
	        String key = "[" + id + "]" + conn.hashCode();
	       
	        LogInfo log = OPEN_CONN_CACHE.get(key);
	        if (log == null) {
	        	log = new LogInfo();
	        	StringWriter sw = new StringWriter();
	            PrintWriter pw = new PrintWriter(sw, true);
	            new Throwable().printStackTrace(pw);
	            log.stackTrace = sw.toString();
	        }
	        
	        log.increase();  
	        OPEN_CONN_CACHE.put(key, log);
	        
	        clearCount ++;
	        if (clearCount % DEFAULT_CACHE_SIZE == 0) {
	        	clearCount = 0;
	        	clear();
	        }
    	}
    }
    
    private static void clear() {
    	List<String> rmKeys = new ArrayList<String>();
    	for (Map.Entry<String, LogInfo> entry : OPEN_CONN_CACHE.entrySet()) {
             String keyValue = (String) entry.getKey();
             LogInfo openLog = entry.getValue();
             LogInfo closeLog = CLOSE_CONN_CACHE.get(keyValue);
             
             if (closeLog != null && openLog.getCount() == closeLog.getCount()) {
            	 rmKeys.add(keyValue);
             }
        }
    	for (String key : rmKeys) {
    		OPEN_CONN_CACHE.remove(key);
    		CLOSE_CONN_CACHE.remove(key);
    	}
    	
    	if (OPEN_CONN_CACHE.size() >= DEFAULT_MAX_CACHE_SIZE) {
    		OPEN_CONN_CACHE.clear();
    		CLOSE_CONN_CACHE.clear();
    	}
    }

    public static void closeConnectionLog(Object conn, String id) {
    	if (!turnOnLog) return;

    	synchronized(ConnectionLogger.class) {
	    	 //logging
	        String key = "[" + id + "]" + conn.hashCode();
	       
	        LogInfo log = CLOSE_CONN_CACHE.get(key);
	        if (log == null) {
	        	log = new LogInfo();
	        }
	        
	        log.increase();
	        CLOSE_CONN_CACHE.put(key, log);
    	}
    }

    public static List<String> retrieveLoggingInfo() {
        List<String> infos = new ArrayList<String>();
        for (Map.Entry<String, LogInfo> entry : OPEN_CONN_CACHE.entrySet()) {
        	StringBuffer info = new StringBuffer();
            String keyValue = (String) entry.getKey();
            LogInfo openLog = entry.getValue();
            LogInfo closeLog = CLOSE_CONN_CACHE.get(keyValue);
            
            info.append("Connection[").append(keyValue).append("]").append(" Opened:[").append(openLog.getCount()).append("]times");
            info.append(" Closed:[").append(closeLog == null ? 0 : closeLog.getCount()).append("]times<br>");
            if (closeLog == null || closeLog.getCount() != openLog.getCount()) {
            	info.append("First Open StatckTrace:<br>" + openLog.stackTrace); 
            }
            
            infos.add(info.toString());
        }
        return infos;
    }
    
    public static List<String> retrieveAbnormalLoggingInfo() {
    	List<String> infos = new ArrayList<String>();
        for (Map.Entry<String, LogInfo> entry : OPEN_CONN_CACHE.entrySet()) {
            StringBuffer info = new StringBuffer();
            String keyValue = (String) entry.getKey();
            LogInfo openLog = entry.getValue();
            LogInfo closeLog = CLOSE_CONN_CACHE.get(keyValue);
            
            if (closeLog == null || openLog.getCount() != closeLog.getCount()) {                
                info.append("Connection[").append(keyValue).append("]").append(" Opened:[").append(openLog.getCount()).append("]times");
                info.append(" Closed:[").append(closeLog == null ? 0 : closeLog.getCount()).append("]times<br>");
                info.append("First Open StatckTrace:<br>" + openLog.stackTrace); 
                infos.add(info.toString());
           }
        }
        return infos;
    }
}
