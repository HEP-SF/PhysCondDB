package conddb.web.monitoring.influx;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxRepository {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private InfluxDB influxDB = null;
	
	protected InfluxDB getInfluxDB() {
		if (influxDB == null) {
			influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
		}
		return influxDB;
	}	
	
	public void setDB(String dbname) {
		try {
			getInfluxDB().createDatabase(dbname);
			log.info("Database name has been created in influxDB");
		} catch (RuntimeException e) {
			log.info("Database name exists already in influxDB");
		}		
	}
	
	public void writeToDb(String dbname, Serie...serieargs) {
		setDB(dbname);
		getInfluxDB().write(dbname, TimeUnit.MILLISECONDS, serieargs);
	}
	
}
