/**
 * 
 */
package conddb.client.actions;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.databind.ObjectMapper;

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
@CliCommand(document = "Insert data action")
@Configurable(preConstruction = true)
public class InsertDataAction extends CliBase implements ActionCommand {

	public static final Logger logger = Logger.getLogger(InsertDataAction.class
			.getName());

	private String inserttype;
	private String action;
	private String[] mode;
	private String[] data;
	private String fullurl = "";

	/**
	 * 
	 */
	public InsertDataAction() {
		super();
		setParentName(InsertDataAction.class.getSimpleName());
		logger.info("Object "+this.getClass().getName()+" created, but what about spring configuration ? ");
		if (getRestClient() == null) {
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
		Option action = Option.builder("i").longOpt("insert").hasArgs()
				.argName("type").numberOfArgs(1).valueSeparator(' ')
				.desc("Insertion type: e.g. globaltag [tag,iov]").build();
		addOption(action);
		Option fromfile = Option.builder("d").longOpt("data").hasArgs()
				.argName("type><content").numberOfArgs(2).valueSeparator(' ')
				.desc("Define data content: e.g. file jsonfileurl, data jsonstring").build();
		addOption(fromfile);
		Option mode = Option.builder("m").longOpt("mode").hasArgs()
				.argName("mode").numberOfArgs(2).valueSeparator(' ')
				.desc("Insertion type: e.g. add [update], default is add").build();
		addOption(mode);
	}

	@Override
	public CommandLine parse(final String[] args) throws ParseException {
		CommandLine line = super.parse(args);
		if (line == null) {
			return null;
		}
		action = "add";
		if (line.hasOption("i")) {
			logger.log(Level.INFO, "Argument for insert :");
			inserttype = line.getOptionValue("i");
			logger.log(Level.INFO, inserttype);
		}
		if (line.hasOption("d")) {
			logger.log(Level.INFO, "Argument for data :");
			data = line.getOptionValues("d");
			logger.log(Level.INFO, data[0]+" "+data[1]);
		}
		if (line.hasOption("m")) {
			logger.log(Level.INFO, "Argument for mode :");
			mode = line.getOptionValues("m");
			action = mode[0];
			logger.log(Level.INFO, mode[0]+" "+mode[1]);
		}
		return line;
	}

	@Override
	public void execute(final String[] args) throws HelpException,
			CliInitException, CliExecuteException {
		init(args);
		try {
			logger.log(Level.INFO, "Insert objects action");

			String type = inserttype;
			String jsonContent = "";
			
			
			jsonContent = getJsonContent(clijsonhandler.getPojoClass(type));
			
			logger.log(Level.INFO,"Loaded content "+jsonContent);
			
			Object[] list = new Object[1];
			String urlargs = type + "/"+action;
			fullurl = fullurl.concat("/" + urlargs);
			logger.log(Level.INFO, "Using url " + fullurl);
			Object entity = getPojoFromJson(clijsonhandler.getPojoClass(type));
			if (action.equals("add")){
				Object storedentity = restClient.postForObject(fullurl, entity);
				list[0] = storedentity;
			} else if (action.equals("update")) {
				fullurl = fullurl.concat("/"+mode[1]);
				restClient.put(fullurl, entity);
				list[0] = entity;
			}
			logger.log(Level.INFO, "Dumping retrieved list of objects...");
			for (int i = 0; i < list.length; i++) {
				logger.log(Level.INFO,
						"   retrieved " + clijsonhandler.toJson(list[i]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private <T> String getJsonContent(Class<T> clazz) {
		String type = data[0];
		String content = data[1];
		if (type.equals("data")) {
			return content;
		}
		T obj = (T) clijsonhandler.getJsonContent(clazz, new File(content));
		return clijsonhandler.toJson(obj);
	}
	
	private <T> T getPojoFromJson(Class<T> clazz) {
		String type = data[0];
		String content = data[1];
		if (type.equals("data")) {
			T obj = (T) clijsonhandler.getJsonContent(clazz, content);
			return obj;
		} else {
			T obj = (T) clijsonhandler.getJsonContent(clazz, new File(content));
			return obj;
		}
	}


}