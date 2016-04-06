/**
 * 
 */
package conddb.utils.data;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author aformic
 *
 */
public class TimeRanges {

	public static LocalDate MAX_DATE = LocalDate.of(2050, Month.JANUARY, 1);
	
	public static Date toDate() {
		Date convertToDate = Date.from(MAX_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return convertToDate;
	}
	public static Timestamp toTimestamp() {
		Date convertToDate = toDate();
		return new Timestamp(convertToDate.getTime());
	}
}
