/**
 * 
 */
package conddb.cli.action;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationContext;

import conddb.cli.ActionCommand;
import conddb.cli.CliBase;
import conddb.cli.CliCommand;
import conddb.cli.CliExecuteException;
import conddb.cli.CliInitException;
import conddb.cli.HelpException;
import conddb.client.actions.FindDataAction;

/**
 * @author formica
 * 
 */

public class MainAction {

	public final Logger logger = Logger.getLogger(MainAction.class.getName());

	private final Map<String, String> helpMap = new HashMap<String, String>();
	private final Map<String, ActionCommand> actionMap = new HashMap<String, ActionCommand>();
//	private final Map<String, CliBase> actionbaseMap = new HashMap<String, CliBase>();

	protected Options options = new Options();
	private String actionname = "main";

//	private ApplicationContext appctx;
	
	public MainAction() {
//		this.appctx = ctx;
		init();
	}
	
	private void init() {
		try {
			findAnnotatedClasses("conddb.client.actions");
			findAnnotatedClasses("conddb.cli.action");
		} catch (CliInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ActionCommand getAction(String actionname) {
		return actionMap.get(actionname);
	}
	
	private Options getGeneralOptions() {

		Option action = Option.builder("A").longOpt("action").hasArg().argName("Action")
				.desc("Execute command <Action>").build();
		Option list = Option.builder("ls").longOpt("list")
				.desc("List available actions").build();
		options.addOption(action);
		options.addOption(list);
		options.addOption("h", "help", false, "this help message");

		return options;
	}

	private String scanOptions(final String[] args) throws ParseException {
		Options options = getGeneralOptions();
		CommandLineParser parser = new DefaultParser();
		// parse the command line arguments
		CommandLine line = parser.parse(options, args);
		if (line.hasOption("help")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("MainAction", options);
			return null;
		}
		if (line.hasOption("action")) {
			actionname = line.getOptionValue("action");
			logger.log(Level.INFO,"Action selected " + actionname);
		}
		if (line.hasOption("list")) {
			Set<String> actions = actionMap.keySet();
			System.out.println("List available actions: ");
			for (String name : actions) {
				System.out.println("   " + name + " : " + helpMap.get(name));
			}
			return null;
		}
		return actionname;
	}

	/**
	 * 
	 * @param args
	 * @param parsedArgs
	 */
	public void executeAction(final String args[], final String[] parsedArgs) {
		try {
			String rettype = scanOptions(args);
			if (rettype == null) {
				return;
			}
			ActionCommand action = actionMap.get(actionname);
//			ActionCommand action = actionbaseMap.get(actionname);
			if (action == null) {
				System.err.println("Cannot execute command " + actionname
						+ ": type --list for help");
				return;
			}
			action.execute(parsedArgs);

		} catch (HelpException e) {
			//
			logger.log(Level.WARNING,"helpException catched...");
		} catch (CliInitException e) {
			logger.log(Level.WARNING,e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			logger.log(Level.SEVERE,e.getMessage());
			e.printStackTrace();
		} catch (CliExecuteException e) {
			logger.log(Level.SEVERE,e.getMessage());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void findAnnotatedClasses(final String pckgname) throws CliInitException {
		try {

			List<Class> classlist = getClassesForPackage(pckgname);
			for (Class aclass : classlist) {
				if (aclass.isAnnotationPresent(CliCommand.class)) {
					CliCommand ann = (CliCommand) aclass.getAnnotation(CliCommand.class);
					String helpMsg = ann.document();
					helpMap.put(aclass.getSimpleName(), helpMsg);
					Class[] interfaces = aclass.getInterfaces();
					for (Class<?> intfc : interfaces) {
						if (intfc.equals(ActionCommand.class)) {
							ActionCommand actcmd = (ActionCommand) aclass.getDeclaredConstructor(
									(Class[]) null).newInstance((Object[]) null);
							actionMap.put(aclass.getSimpleName(), actcmd);
						}
					}
//					Class<?> parent = aclass.getSuperclass();
//					if (parent.equals(CliBase.class)) {
//						Object basecmd = aclass.getDeclaredConstructor(
//							(Class[]) null).newInstance((Object[]) null);							
////						actionbaseMap.put(aclass.getSimpleName(),(CliBase) basecmd);
//					}
//					// TEST
//					CliBase fa = new FindDataAction();
//					actionbaseMap.put(aclass.getSimpleName(),fa);
				}
			}
		} catch (NoSuchMethodException e) {
			throw new CliInitException(e);
		} catch (ClassNotFoundException e) {
			throw new CliInitException(e);
		} catch (IllegalArgumentException e) {
			throw new CliInitException(e);
		} catch (SecurityException e) {
			throw new CliInitException(e);
		} catch (InstantiationException e) {
			throw new CliInitException(e);
		} catch (IllegalAccessException e) {
			throw new CliInitException(e);
		} catch (InvocationTargetException e) {
			throw new CliInitException(e);
		}
	}

	public void setDefaultSystemProperty(final String key, final String value) {
		if (System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Class> getClassesForPackage(final String pckgname)
			throws ClassNotFoundException {
		// This will hold a list of directories matching the pckgname.
		// There may be more than one if a package is split over multiple
		// jars/paths
		List<Class> classes = new ArrayList<Class>();
		ArrayList<File> directories = new ArrayList<File>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					JarURLConnection conn = (JarURLConnection) res.openConnection();
					JarFile jar = conn.getJarFile();
					for (JarEntry e : Collections.list(jar.entries())) {

						if (e.getName().startsWith(pckgname.replace('.', '/'))
								&& e.getName().endsWith(".class") && !e.getName().contains("$")) {
							String className = e.getName().replace("/", ".")
									.substring(0, e.getName().length() - 6);
							// System.out.println(className);
							classes.add(Class.forName(className));
						}
					}
				} else {
					directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
				}
			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname + " does not appear to be "
					+ "a valid package (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(pckgname + " does not appear to be "
					+ "a valid package (Unsupported encoding)");
		} catch (IOException ioex) {
			throw new ClassNotFoundException("IOException was thrown when trying "
					+ "to get all resources for " + pckgname);
		}

		// For every directory identified capture all the .class files
		for (File directory : directories) {
			if (directory.exists()) {
				// Get the list of the files contained in the package
				String[] files = directory.list();
				for (String file : files) {
					// we are only interested in .class files
					if (file.endsWith(".class")) {
						// removes the .class extension
						classes.add(Class.forName(pckgname + '.'
								+ file.substring(0, file.length() - 6)));
					}
				}
			} else {
				throw new ClassNotFoundException(pckgname + " (" + directory.getPath()
						+ ") does not appear to be a valid package");
			}
		}
		return classes;
	}

	@SuppressWarnings("unchecked")
	public static List<Class> getClassessOfInterface(final String thePackage,
			final Class theInterface) {
		List<Class> classList = new ArrayList<Class>();
		try {
			for (Class discovered : getClassesForPackage(thePackage)) {
				if (Arrays.asList(discovered.getInterfaces()).contains(theInterface)) {
					classList.add(discovered);
				}
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return classList;
	}
}
