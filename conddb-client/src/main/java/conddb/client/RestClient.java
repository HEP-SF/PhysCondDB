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

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author formica
 *
 */
@Component
public class RestClient {

	public final Logger logger = Logger.getLogger(this.getClass().getName());

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RestClientProperties clientProperties;
	
	public RestClient() {
		super();
	}
	public RestClient(RestClientProperties clientProperties) {
		super();
		this.clientProperties = clientProperties;
	}
	
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
    /**
     * Creates URL based on the URI passed in.
     */
    public String createUrl(String uri) {
        StringBuilder sb = new StringBuilder();

        sb.append(clientProperties.getUrl());
        String apipath = clientProperties.getApiPath();
        if (!apipath.equals("none")) {
        	sb.append(clientProperties.getApiPath());
        }
        sb.append(uri);

        logger.info("URL is "+ sb.toString());

        return sb.toString();
    }	
    
    public <T> T getForObject(String url, Class<T> responseType) {
    	String fullurl = createUrl(url);
        logger.info("URL is "+ fullurl);
    	return restTemplate.getForObject(fullurl, responseType);
    }

    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
    	String fullurl = createUrl(url);
        logger.info("URL is "+ fullurl);
    	return restTemplate.getForEntity(fullurl, responseType);
    }

    public <T> void put(String url, T reqObj) {
    	String fullurl = createUrl(url);
        logger.info("URL is "+ fullurl);
    	restTemplate.put(fullurl, reqObj);
    }

    public <T> T postForObject(String url, T reqObj) {
    	String fullurl = createUrl(url);
        logger.info("URL is "+ fullurl);
    	return (T) restTemplate.postForObject(fullurl, reqObj, reqObj.getClass());
    }

}
