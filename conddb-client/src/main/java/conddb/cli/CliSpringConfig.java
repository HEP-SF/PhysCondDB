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
package conddb.cli;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
////import org.springframework.context.annotation.PropertySource;

/**
 * @author formica
 *
 */
@Configuration
@ImportResource("classpath:application-context.xml")
////@PropertySource("classpath:/spring/cli.properties")
public class CliSpringConfig {

}
