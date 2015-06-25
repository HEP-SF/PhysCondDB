/**
 * 
 */
package conddb.client.actions;

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
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Tag;

/**
 * @author formica
 * 
 */
@CliCommand(document = "Find data action")
@Configurable(preConstruction = true)
public class FindDataAction extends CliBase implements ActionCommand {

	public static final Logger logger = Logger.getLogger(FindDataAction.class
			.getName());

	private String[] findaction;
	private String fullurl = "";
	/**
	 * 
	 */
	public FindDataAction() {
		super();
		setParentName(FindDataAction.class.getSimpleName());
		logger.info("Object FindDataAction created, but what about spring configuration ? ");
		if (getRestClient()==null){
			logger.info("RESTclient is null ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see conddb.cli.CliBase#init(java.lang.String[])
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
	@Override
	public void initOptions() {
		Option action = Option.builder("f").longOpt("find").hasArgs()
				.argName("type> <id> <method").numberOfArgs(3)
				.valueSeparator(' ')
				.desc("Send url search: e.g. globaltag TEST% list").build();
		addOption(action);

	}

	@Override
	public CommandLine parse(final String[] args) throws ParseException {
		CommandLine line = super.parse(args);
		if (line == null) {
			return null;
		}
		if (line.hasOption("f")) {
			logger.log(Level.INFO, "Argument for find :");
			findaction = line.getOptionValues("f");
			logger.log(Level.INFO, findaction[0] + " " + findaction[1] + " "
					+ findaction[2]);
		}
		return line;
	}

	@Override
	public void execute(final String[] args) throws HelpException,
			CliInitException, CliExecuteException {
		init(args);
		try {
			logger.log(Level.INFO, "Find data action");
			
			String type = findaction[0];
			String name = findaction[1];
			String method = findaction[2];
			
			if (name.contains("%")) {
				name = name.replaceAll("%", "%25");
			}
			Object[] list = null;
			String urlargs = findaction[0] + "/" + findaction[1] + "/"
					+ findaction[2];
			fullurl = fullurl.concat("/" + urlargs);
			logger.log(Level.INFO, "Using url " + fullurl);
			
			if (type.equals("globaltag")) {
				list = getRestClient()
						.getForObject(fullurl, GlobalTag[].class);	
				logger.log(Level.INFO, "Found list of global tags  " + list.length);
			} else if (type.equals("tag")) {
				list = getRestClient()
						.getForObject(fullurl, Tag[].class);	
				logger.log(Level.INFO, "Found list of tags  " + list.length);
			}  else if (type.equals("iovs")) {
				list = getRestClient()
						.getForObject(fullurl, Iov[].class);	
				logger.log(Level.INFO, "Found list of tags  " + list.length);
			} else {
				throw new HelpException("Cannot find type "+type);
			}
			
//			ResponseEntity<Object[]> responseEntity = getRestClient()
//					.getForEntity(fullurl, Object[].class);
//			Object[] list = responseEntity.getBody();
//			logger.log(Level.INFO, "Found list of objects " + list.length);
//			for (int i=0; i<list.length;i++) {
//				logger.log(Level.INFO, "   retrieved " + list[i]);
//			}
			logger.log(Level.INFO, "Dumping retrieved list of objects...");
			for (int i=0; i<list.length;i++) {
				logger.log(Level.INFO, "   retrieved \n" + clijsonhandler.toJsonPretty(list[i]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}