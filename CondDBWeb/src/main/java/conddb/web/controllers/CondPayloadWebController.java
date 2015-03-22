/**
 * 
 */
package conddb.web.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import conddb.dao.repositories.PayloadRepository;
import conddb.data.Payload;
import conddb.data.handler.PayloadHandler;

/**
 * @author formica
 *
 */
@RestController
public class CondPayloadWebController {

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
                apayload.setData(bytes);
                apayload.setVersion(version);
                apayload.setObjectType(objtype);
                apayload.setStreamerInfo(strinfo);
                apayload.setDatasize(bytes.length);

                PayloadHandler phandler = new PayloadHandler(apayload);
                Payload storable = phandler.getPayloadWithHash();
                if (payloadrepo.findOne(storable.getHash()) == null) {
                	payloadrepo.save(storable);
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
}
