/**
 * 
 */
package conddb.monitoring.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.influxdb.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import conddb.monitoring.influx.InfluxRepository;

/**
 * @author formica
 *
 */
public class RequestProcessingProfilerInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory
            .getLogger(RequestProcessingProfilerInterceptor.class);
 
	@Autowired
	private InfluxRepository influxrep;
	
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        logger.info("Request URL::" + request.getRequestURL().toString()
                + ":: Start Time=" + System.currentTimeMillis() + " from  "+request.getRemoteHost());

        Long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        return true;
    }
 
 
    @Override
    //TODO : define serie name as the URL
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    	
    	Long endtime = System.currentTimeMillis();
    	Long starttime = (Long) request.getAttribute("startTime");
    	Integer code = (Integer) response.getStatus();
    	Long xmill = endtime - starttime;
    	String seriename = request.getRequestURL().toString();
    	Serie profserie = new Serie.Builder("request_handler")
        .columns("start","timeUsed","code","url")
        .values(starttime,xmill,code,seriename)
        .build();
    	
    	influxrep.writeToDb("physconddb_site", profserie);
    }
}
