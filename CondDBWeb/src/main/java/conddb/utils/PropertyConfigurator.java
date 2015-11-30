/**
 * 
 */
package conddb.utils;

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
	
}
