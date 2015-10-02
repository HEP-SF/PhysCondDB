package conddb.utils.converters;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import conddb.data.Iov;
import conddb.data.exceptions.ConversionException;
import conddb.utils.json.serializers.TimestampFormat;

@Service
public class IovConversionHandler implements IIovConverter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TimestampFormat timestampFormat;

	/* (non-Javadoc)
	 * @see conddb.utils.converters.IIovConverter#convert(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public BigDecimal convert(String inputstr, String deststr, String value) throws ConversionException {
		if (inputstr.equals("Date") & deststr.equals("Time")) {
			return this.convertDateStringToTime(value);
		}
		return null;
	}

	protected BigDecimal convertDateStringToTime(String datestring) throws ConversionException {
		BigDecimal sincetime = null;
		try {
			log.debug("Use private version of deserializer...." + timestampFormat.getLocformatter().toString());
			ZonedDateTime zdt = ZonedDateTime.parse(datestring, timestampFormat.getLocformatter());
			Timestamp tstamp = new Timestamp(zdt.toInstant().toEpochMilli());
			sincetime = new BigDecimal(tstamp.getTime()*Iov.TO_NANOSECONDS);
		} catch (Exception e) {
			throw new ConversionException(e.getMessage());
		}
		return sincetime;
		
	}
	
	protected BigDecimal convertMillisecStringToTime(String millisec) throws ConversionException {
		BigDecimal sincetime = null;
		try {
			BigDecimal inptime = new BigDecimal(millisec);
			BigDecimal nanosec = new BigDecimal(Iov.TO_NANOSECONDS);
			sincetime = inptime.multiply(nanosec);
		} catch (Exception e) {
			throw new ConversionException(e.getMessage());
		}
		return sincetime;
		
	}

	protected BigDecimal convertRunStringToTime(String run) throws ConversionException {
		BigDecimal sincetime = null;
		try {
			BigDecimal runnum = new BigDecimal(run);
			sincetime = runnum;
		} catch (Exception e) {
			throw new ConversionException(e.getMessage());
		}
		return sincetime;
	}

}
