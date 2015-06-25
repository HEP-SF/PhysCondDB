/**
 * 
 */
package conddb.cli.action;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Configurable;

import conddb.cli.ActionCommand;
import conddb.cli.CliBase;
import conddb.cli.CliCommand;
import conddb.cli.CliExecuteException;
import conddb.cli.CliInitException;
import conddb.cli.HelpException;

/**
 * @author formica
 * 
 */
@CliCommand(document = "Test action")
@Configurable(preConstruction = true)
public class TestAction extends CliBase implements ActionCommand {

	public static final Logger logger = Logger.getLogger(TestAction.class.getName());

	private String tag = "none";

	/**
	 * 
	 */
	public TestAction() {
		super();
		setParentName(TestAction.class.getSimpleName());
		logger.info("Object TestAction created, but what about spring configuration ? "+tag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see align.client.cli.base.AlignCLIBase#init(java.lang.String[])
	 */
	@Override
	public void init(final String[] args) throws HelpException,
			CliInitException {
		super.init(args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see align.client.cli.base.AlignCLIBase#initOptions()
	 */
	@SuppressWarnings("static-access")
	@Override
	public void initOptions() {
		Option action = Option.builder("do").longOpt("do").hasArg().argName("Command")
				.desc("Execute command <Command>").build();
		addOption(action);	
	}

	@Override
	public CommandLine parse(final String[] args) throws ParseException {
		CommandLine line = super.parse(args);
		if (line == null) {
			return null;
		}
		if (line.hasOption("do")) {
			logger.log(Level.INFO,"Argument do:");
			method = line.getOptionValue("do");
			logger.log(Level.INFO,method);
		}
		
		return line;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see align.client.cli.base.AlignCLIBase#execute(java.lang.String[])
	 */
	@Override
	public void execute(final String[] args) throws HelpException,
			CliInitException, CliExecuteException {
		init(args);
		logger.log(Level.ALL, "Empty action for test");
		
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		logger.log(Level.INFO, "Spring is calling this method ???");
		this.tag = tag;
	}
	
	
}