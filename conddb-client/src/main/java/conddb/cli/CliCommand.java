/**
 * 
 */
package conddb.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author formica
 *
 */
@Component
@Scope("prototype")
@Retention(RetentionPolicy.RUNTIME)
public @interface CliCommand {
	String document() default "";
}
