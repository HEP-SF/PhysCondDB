/**
 * 
 */
package conddb.web.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import conddb.dao.repositories.PayloadRepository;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.handler.PayloadHandler;

/**
 * @author formica
 *
 */
@RestController
public class CondPayloadWebController {

	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayloadRepository payloadrepo;

	
	@RequestMapping(value="/uploadPayload", method=RequestMethod.POST)
    public @ResponseBody String handleFileUpload( 
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String objtype,
            @RequestParam("streamer") String strinfo,
            @RequestParam("version") String version){
		
        String name = file.getOriginalFilename();
            
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String outfname = file.getOriginalFilename()+"-uploaded";
                BufferedOutputStream stream = 
                        new BufferedOutputStream(new FileOutputStream(new File("/tmp/"+outfname)));
                stream.write(bytes);
                stream.close();
                Payload apayload = new Payload();
                apayload.setVersion(version);
                apayload.setObjectType(objtype);
                apayload.setStreamerInfo(strinfo);
                apayload.setDatasize(bytes.length);

                PayloadData pylddata = new PayloadData();
                pylddata.setData(bytes);

                PayloadHandler phandler = new PayloadHandler(pylddata);
                PayloadData storable = phandler.getPayloadWithHash();
                apayload.setData(storable);
                apayload.setHash(storable.getHash());
                log.info("Uploaded object has hash "+storable.getHash());
                log.info("Uploaded object has data size "+apayload.getDatasize());

                if (payloadrepo.findOne(apayload.getHash()) == null) {
                	payloadrepo.save(apayload);
                } else {
                	return "Payload with hash " + storable.getHash() + " already exists...skip update ";
                }
                return "You successfully uploaded " + name + " into " + outfname + ", with hash "+storable.getHash();
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }
	
	@RequestMapping(value="/existshash", method=RequestMethod.POST)
    public @ResponseBody String findHash( 
            @RequestParam("hash") String hash){
		
		Payload storedhash = payloadrepo.findOne(hash);
		if (storedhash == null) {
			return "NOT_EXISTS";
		}
		return storedhash.toString();
    }

	@RequestMapping(value="/payloadhash", method=RequestMethod.POST)
    public @ResponseBody String getBlobHash( 
            @RequestParam("file") MultipartFile file){
		
        String name = file.getOriginalFilename();
            
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String outfname = file.getOriginalFilename()+"-uploaded";
                BufferedOutputStream stream = 
                        new BufferedOutputStream(new FileOutputStream(new File("/tmp/"+outfname)));
                stream.write(bytes);
                stream.close();
                PayloadData pylddata = new PayloadData();
                pylddata.setData(bytes);
                PayloadHandler phandler = new PayloadHandler(pylddata);
                PayloadData storable = phandler.getPayloadWithHash();

                log.info("Uploaded object has hash "+storable.getHash());

                return storable.getHash();
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }

}
