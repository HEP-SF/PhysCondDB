/**
 * 
 */
package conddb.web.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author formica
 *
 */
public class RequestProcessingFrontierInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory
            .getLogger(RequestProcessingFrontierInterceptor.class);
 
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        logger.info("Request URL::" + request.getRequestURL().toString()
                + ":: Start Time=" + System.currentTimeMillis() + " Frontier "+request.getRemoteHost());
        return true;
    }
 
 
    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        logger.info("Request URL::" + request.getRequestURL().toString()
                + ":: End Time=" + System.currentTimeMillis() + " Frontier "+request.getRemoteHost());
    }
}
