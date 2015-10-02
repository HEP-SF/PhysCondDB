package conddb.utils.converters;

import java.math.BigDecimal;

import conddb.data.exceptions.ConversionException;

public interface IIovConverter {

	BigDecimal convert(String inputstr, String deststr, String value) throws ConversionException;

}