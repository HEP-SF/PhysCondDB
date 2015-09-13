/**
 * 
 */
package conddb.cli;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

/**
 * Interface for the action files.
 * @author formica
 *
 */
public interface ActionCommand {
	
	/**
	 * @param args
	 * @throws HelpException
	 * @throws CliInitException
	 */
	void init(String[] args) throws HelpException, CliInitException;
	/**
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	void initLogger() throws MalformedURLException, FileNotFoundException;
	/**
	 * Initialize the options
	 */
	void initOptions();
	/**
	 * @param args
	 * @return CommandLine 
	 * @throws ParseException
	 */
	CommandLine parse(String[] args) throws ParseException;
	/**
	 * @param args
	 * @throws HelpException
	 * @throws CliInitException
	 * @throws CliExecuteException
	 */
	void execute(String[] args) throws HelpException, CliInitException,
			CliExecuteException;
}
