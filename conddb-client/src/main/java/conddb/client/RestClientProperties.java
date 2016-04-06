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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author formica
 *
 */
@Component
public class RestClientProperties {


    @Value("#{ restProperties['ws.api'] }")
    private String apiPath;

    @Value("#{ systemProperties['ws.url'] != null ? systemProperties['ws.url'] : restProperties['ws.url'] }")
    private String url;

    /**
     * Gets base URI for the REST APIs.
     */
    public String getApiPath() {
        return apiPath;
    }

    /**
     * Gets URL.
     */
    public String getUrl() {
        return url;
    }

}
