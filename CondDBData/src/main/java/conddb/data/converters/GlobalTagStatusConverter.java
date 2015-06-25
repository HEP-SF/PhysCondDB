package conddb.data.converters;

import javax.persistence.AttributeConverter;

import conddb.data.GlobalTagStatus;

public class GlobalTagStatusConverter implements
		AttributeConverter<GlobalTagStatus, String> {

	@Override
	public String convertToDatabaseColumn(GlobalTagStatus gtagstatus) {
		switch (gtagstatus) {
		case LOCKED:
			return "locked";
		case UNLOCKED:
			return "unlocked";
		default:
			throw new IllegalArgumentException("Unknown" + gtagstatus);
		}
	}

	@Override
	public GlobalTagStatus convertToEntityAttribute(String dbData) {
		switch (dbData) {
		case "locked":
			return GlobalTagStatus.LOCKED;
		case "unlocked":
			return GlobalTagStatus.UNLOCKED;
		default:
			throw new IllegalArgumentException("Unknown" + dbData);
		}
	}

}
