/**
 * 
 */
package conddb.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;

import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
public class TestLoadDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestLoadDB tdbload = new TestLoadDB();
		try {
			tdbload.loadGlobalTags();
			tdbload.loadTags();
			System.out.println("Create mappings");
    		tdbload.doMappings();
			System.out.println("Create iovs");
			tdbload.loadIovs();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void loadGlobalTags() throws ClientProtocolException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		List<GlobalTag> gtaglist = generateGlobalTag(10);
		for (GlobalTag gtag : gtaglist) {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(
					"http://localhost:8080/physconddb/conddbweb/gtagAdd");
			String json = mapper.writer().withDefaultPrettyPrinter()
					.writeValueAsString(gtag);
			StringEntity entity = new StringEntity(json);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			CloseableHttpResponse response = client.execute(httpPost);
			assertThat(response.getStatusLine().getStatusCode(), is(200));
			client.close();
		}
	}

	public void loadTags() throws ClientProtocolException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		List<Tag> taglist = generateTag(20);
		for (Tag tag : taglist) {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(
					"http://localhost:8080/physconddb/conddbweb/tagAdd");
			String json = mapper.writer().withDefaultPrettyPrinter()
					.writeValueAsString(tag);
			StringEntity entity = new StringEntity(json);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			CloseableHttpResponse response = client.execute(httpPost);
			assertThat(response.getStatusLine().getStatusCode(), is(200));
			client.close();
		}
	}

	public void doMappings() throws ClientProtocolException, IOException {

		Map<String, ArrayList<String>> maps = generateMap("TEST_5", 8);
		for (String key : maps.keySet()) {
			System.out.println("Create mappings for global tag "+key);
			for (String val : maps.get(key)) {
				System.out.println("Create mappings for tag "+val);
				CloseableHttpClient client = HttpClients.createDefault();
				HttpPost httpPost = new HttpPost(
						"http://localhost:8080/physconddb/conddbweb/mapTagToGtag");
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("globaltagname", key));
				nvps.add(new BasicNameValuePair("tagname", val));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
				CloseableHttpResponse response = client.execute(httpPost);
				assertThat(response.getStatusLine().getStatusCode(), is(200));
				client.close();
			}
		}
	}

	public void loadIovs() throws ClientProtocolException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		List<Iov> iovlist = generateIov(10);
		for (Iov iov : iovlist) {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(
					"http://localhost:8080/physconddb/conddbweb/iovAdd");
			String json = mapper.writer().withDefaultPrettyPrinter()
					.writeValueAsString(iov);
			StringEntity entity = new StringEntity(json);
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			CloseableHttpResponse response = client.execute(httpPost);
			assertThat(response.getStatusLine().getStatusCode(), is(200));
			client.close();
		}
	}

	public List<GlobalTag> generateGlobalTag(int size) {
		List<GlobalTag> gtaglist = new ArrayList<GlobalTag>();
		for (int i = 1; i < size; i++) {
			Timestamp now = new Timestamp(new Date().getTime());
			String name = "TEST_" + i;
			GlobalTag gtag = new GlobalTag(name, new BigDecimal(100),
					"Test global tag", "0.0.1", now, now);
			gtaglist.add(gtag);
		}
		return gtaglist;
	}

	public Map<String, ArrayList<String>> generateMap(String gtag, int size) {
		Map<String, ArrayList<String>> maps = new HashMap<String, ArrayList<String>>();
		String gtagname = gtag;
		ArrayList<String> namelist = new ArrayList<String>();
		for (int i = 1; i < size; i++) {
			String name = "tagtest_" + i;
			System.out.println("generate mapping for "+name);
			namelist.add(name);
		}
		maps.put(gtagname, namelist);
		return maps;
	}

	public List<Tag> generateTag(int size) {
		List<Tag> taglist = new ArrayList<Tag>();
		for (int i = 1; i < size; i++) {
			String name = "tagtest_" + i;
			Tag tag = new Tag(name, "time", "none", "none", "test leaf tag",
					new BigDecimal(200), new BigDecimal(0));
			taglist.add(tag);
		}
		return taglist;
	}
	
	public List<Iov> generateIov(int size) {
		List<Iov> iovlist = new ArrayList<Iov>();
		Payload pyld = new Payload();
		Tag atag = new Tag("tagtest_7");
		for (int i = 0; i < size; i++) {
			Timestamp now = new Timestamp(new Date().getTime());
			Iov iov = new Iov(new BigDecimal(i*1000L),"this should be a time",now,pyld,atag);
			iovlist.add(iov);
		}
		return iovlist;	
	}

}
