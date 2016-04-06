/**
 * 
 */
package conddb.web.utils;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author aformic
 *
 */
public class PropertyConfigurator {
	
	private static PropertyConfigurator jbcoolPropertyConfigurator=null;
	
	@Value(value="${server.port}")
	private String serverPort;
	@Value(value="${server.protocol}")
	private String serverProtocol;
	@Value(value="${server.host}")
	private String serverHost;
	@Value("${conddb.monitor.logging}")
	public String monitorLogging;
	@Value("${conddb.time.format}")
	private String pattern = "yyyyMMddHHmmss:z";

	private DateTimeFormatter locFormatter;
	
	private PropertyConfigurator () {
		
	}
	
	public static PropertyConfigurator getInstance() {
		if (jbcoolPropertyConfigurator==null) {
			jbcoolPropertyConfigurator = new PropertyConfigurator();
		}
		return jbcoolPropertyConfigurator;
	}
	
	public String getServerPort() {
		return serverPort;
	}

	public String getServerProtocol() {
		return serverProtocol;
	}

	public String getServerHost() {
		return serverHost;
	}

	public String getMonitorLogging() {
		return monitorLogging;
	}
	public DateTimeFormatter getLocformatter() throws IllegalArgumentException {
		if (pattern.equals("ISO_OFFSET_DATE_TIME")) {
			locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		} else if (pattern.equals("ISO_LOCAL_DATE_TIME")) {
			locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		} else {
			locFormatter = DateTimeFormatter.ofPattern(pattern);
		}
		return locFormatter;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	
}
