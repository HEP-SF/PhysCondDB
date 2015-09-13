/**
 * 
 */
package conddb.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import conddb.client.RestClient;

/**
 * @author formica
 * 
 */
@Configurable(preConstruction = true)
public abstract class CliBase implements ActionCommand {

	public static final Logger logger = Logger.getLogger(CliBase.class.getName());
	
	protected String host = "localhost";
	protected Integer port = new Integer(8080);
	protected Integer secureport = new Integer(8443);

	protected String method = "http";
	protected String logconfig = System.getProperty("user.dir") + "/logging.properties";
	
	protected String parentName = "CliBase";
	protected String proxyHost = "none";

	protected Properties jndiprops = new Properties();
	
	@Autowired
	protected RestClient restClient;
	@Autowired
	protected CliJsonHandler clijsonhandler;

	Options options = new Options();

	
	/**
	 * 
	 */
	public CliBase() {
		super();
		logger.info("Creating abstract instance CliBase");
	}

	/**
	 * add options.
	 */
	private void addGeneralOptions() {

		Option host = Option.builder("H").longOpt("host").hasArg().argName("hostname")
				.desc("Server name (default: localhost)").build();
		Option port = Option.builder("P").longOpt("port").hasArg().argName("port")
				.desc("Server Port number (default: 8080)").build();
		Option socks = Option.builder("s").longOpt("socks").hasArg().argName("socks proxy")
				.desc("Socks Proxy host:port (default: localhost:3129)").build();
		Option logging = Option.builder("l").longOpt("log").hasArg().argName("logging configuration")
				.desc("Path to logging configuration file (default: ./logging.properties )").build();

		options.addOption("h", "help", false, "this help message");
		options.addOption(host);
		options.addOption(port);
		options.addOption(socks); // proxy parameters
		options.addOption(logging);
	}

	/**
	 * 
	 * @param opt
	 */
	public void addOption(final Option opt) {
		options.addOption(opt);
	}

	/**
	 * 
	 */
	public CommandLine parse(final String[] args) throws ParseException {
		addGeneralOptions();
		CommandLineParser parser = new DefaultParser();
		// parse the command line arguments
		CommandLine line = parser.parse(options, args);

		if (System.getProperty("java.util.logging.config.file") != null) {
			logconfig = System.getProperty("java.util.logging.config.file");
		}

		if (line.hasOption("help")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(parentName, options);
			return null;
		}

		if (line.hasOption("host")) {
			logger.fine(line.getOptionValue("host"));
			host = line.getOptionValue("host");
		}
		if (line.hasOption("port")) {
			logger.fine(line.getOptionValue("port"));
			port = Integer.valueOf(line.getOptionValue("port"));
		}
		if (line.hasOption("socks")) {
			logger.fine("Socks Proxy host " + line.getOptionValue("socks"));
			proxyHost = line.getOptionValue("socks");
		}
		
		if (line.hasOption("log")) {
			logger.fine(line.getOptionValue("log"));
			logconfig = line.getOptionValue("log");
		}
		
		return line;
	}

	public void initLogger() throws MalformedURLException, FileNotFoundException {
		LogManager manager = LogManager.getLogManager();
		InputStream is = new FileInputStream(new File(logconfig));
		try {
			manager.readConfiguration(is);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @return the parentName
	 */
	public String getParentName() {
		return parentName;
	}

	/**
	 * @param parentName
	 *            the parentName to set
	 */
	public void setParentName(final String parentName) {
		this.parentName = parentName;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see align.client.cli.base.ActionCommand#initOptions()
	 */
	public abstract void initOptions();

	/*
	 * (non-Javadoc)
	 * 
	 * @see align.client.cli.base.ActionCommand#init()
	 */
	public void init(final String[] args) throws HelpException, CliInitException {
		try {
			initOptions();
			if (parse(args) == null) {
				throw new HelpException("");
			}
			initLogger();
		} catch (ParseException e) {
			throw new CliInitException(e);
		} catch (UnsupportedOperationException e) {
			throw new CliInitException(e);
		} catch (FileNotFoundException e) {
			throw new CliInitException(e);
		} catch (MalformedURLException e) {
			throw new CliInitException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see align.client.cli.base.ActionCommand#execute(java.lang.String[])
	 */
	public abstract void execute(String[] args) throws HelpException, CliInitException,
			CliExecuteException;

	public RestClient getRestClient() {
		return restClient;
	}

	public void setRestClient(RestClient restClient) {
		logger.info("Calling setRestclient...is it spring ????");
		this.restClient = restClient;
	}

	public CliJsonHandler getClijsonprinter() {
		return clijsonhandler;
	}

	public void setClijsonprinter(CliJsonHandler clijsonprinter) {
		logger.info("Calling setRestclient...is it spring ????");
		this.clijsonhandler = clijsonprinter;
	}	
}
