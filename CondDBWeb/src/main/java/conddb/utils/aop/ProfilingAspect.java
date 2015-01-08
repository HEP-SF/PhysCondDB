package conddb.utils.aop;

import org.aspectj.lang.ProceedingJoinPoint;

import conddb.annotations.LogAction;
 
//@Aspect
public class ProfilingAspect {
 
//	@Pointcut(value="execution(public * *(..))")
	public void anyPublicMethod() {
		System.out.println("Calling a public method");
	}
 
//	@Around("anyPublicMethod() && @annotation(logAction)")
	public Object logAction(ProceedingJoinPoint pjp, LogAction logAction) throws Throwable {
 
		// Do what you want with the actionperformed
		String actionPerformed = logAction.actionPerformed();
 
		// Do what you want with the join point arguments
		for ( Object object : pjp.getArgs()) {
			System.out.println(object);
		}
 
		return pjp.proceed();
	}
}
