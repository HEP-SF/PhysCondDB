/**
 * 
 */
package temporary;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author formica
 *
 */
public class TestDate {

	private static DateTimeFormatter formatter = 
			//DateTimeFormatter.ISO_DATE_TIME;
//			DateTimeFormatter.ofPattern("yyyyMMddHHmmss:Z");
			DateTimeFormatter.ofPattern("yyyyMMddHHmmss:z");
	/**
	 * @param args
	 * 	No arguments.
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

// "yyyyMMddHHmmss:Z"       String since = "20150202000054:-0800";
        String since = "20150202000054:CEST";
//        String since = "20150202000054:-0800";
		ZonedDateTime sincedate = ZonedDateTime.parse(since, TestDate.formatter);
		System.out.println("Parsed date is "+sincedate);

	}

}
