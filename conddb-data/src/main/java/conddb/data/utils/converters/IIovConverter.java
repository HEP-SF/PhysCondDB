package conddb.data.utils.converters;

import java.math.BigDecimal;

import conddb.data.exceptions.ConversionException;

public interface IIovConverter {

	BigDecimal convert(CondTimeTypes input, CondTimeTypes dest, String value) throws ConversionException;

}