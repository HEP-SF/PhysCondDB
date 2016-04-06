/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package conddb.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import conddb.cli.ActionCommand;
import conddb.cli.CliSpringConfig;
import conddb.cli.action.AtestClass;
import conddb.cli.action.MainAction;
import conddb.cli.action.TestAction;
import conddb.client.actions.FindDataAction;
import conddb.client.actions.InsertDataAction;

/**
 * @author formica
 *
 */
public class CondDBClientCLI {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		// Use xml only configuration
//		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(CliSpringConfig.class);
		// add a shutdown hook for the above context...
		((AbstractApplicationContext) applicationContext).registerShutdownHook();

		AtestClass atc = new AtestClass();
//		System.out.println("Debugging configurable on AtestClass instance :"+atc.getFormat());
		
		MainAction main = new MainAction();
		main.setDefaultSystemProperty("logging.configuration", "logging.properties");

		ActionCommand testaction = main.getAction("TestAction");
		System.out.println("Debugging configurable on TestAction instance :"+((TestAction)testaction).getTag());
		System.out.println("Debugging configurable on TestAction instance :"+((TestAction)testaction).getRestClient());
		ActionCommand action = main.getAction("FindDataAction");
		System.out.println("Debugging configurable on FindDataAction instance :"+((FindDataAction)action).getRestClient());
		ActionCommand insertaction = main.getAction("InsertDataAction");
		System.out.println("Debugging configurable on InsertDataAction instance :"+((InsertDataAction)insertaction).getRestClient());
		
		// Check spring created beans
		MappingJackson2HttpMessageConverter messconv = (MappingJackson2HttpMessageConverter) applicationContext.getBean("jsonConverter");
		if (messconv != null) {
			System.out.println("Debugging Jackson2 MessageConverter :"+messconv);
		}
		
		RestTemplate rest = (RestTemplate) applicationContext.getBean("restTemplate");
		if (rest != null) {
			System.out.println("Debugging RestTemplate :"+rest);
			List<HttpMessageConverter<?>> messconverters = rest.getMessageConverters();
			for (HttpMessageConverter<?> httpMessageConverter : messconverters) {
				System.out.println("Found converter "+httpMessageConverter+" class "+httpMessageConverter.getClass().getName());
			}
		}
		
		
		
		ArrayList<String> parsedArgs = new ArrayList<String>();
		ArrayList<String> mainArgs = new ArrayList<String>();
		int nargs = args.length;
		if (nargs > 0) {
			boolean getfollowing = false;
			for (String arg : args) {
				if (getfollowing) {
					if (parsedArgs.size() == 0) {
						mainArgs.add(arg);
					}
					parsedArgs.add(arg);
				} else {
					mainArgs.add(arg);
				}
				if (arg.equals("-A") || arg.equals("--action")) {
					getfollowing = true;
				}
			}
		}
		String[] actionArgs = new String[parsedArgs.size()];
		parsedArgs.toArray(actionArgs);
		String[] margs = new String[mainArgs.size()];
		mainArgs.toArray(margs);
		main.executeAction(margs, actionArgs);
	}
}
