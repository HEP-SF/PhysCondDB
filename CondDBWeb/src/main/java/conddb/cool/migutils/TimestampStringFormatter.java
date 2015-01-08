package conddb.cool.migutils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author formica
 *
 */
public class TimestampStringFormatter {

	/**
	 * 
	 */
	protected static final SimpleDateFormat df = new SimpleDateFormat();

	/**
	 * 
	 */
	protected static final String lfmt = "yyyy/MM/dd hh:mm:ss";

	/**
	 * @param format
	 * @param adate
	 * @return
	 */
	public static String format(final String format, final Date adate) {
		try {
			String lformat = format;
			if (format == null) {
				lformat = lfmt;
			}
			df.applyPattern(lformat);
			return df.format(adate);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param format
	 * @param adate
	 * @return
	 */
	public static String formatTs(final String format, final Timestamp adate) {
		try {
			String lformat = format;
			if (format == null) {
				lformat = lfmt;
			}
			df.applyPattern(lformat);
			return df.format(new Date(adate.getTime()));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
