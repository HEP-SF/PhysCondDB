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
            @RequestParam("file") MultipartFile file){
            String name = "/tmp/test11";
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = 
                        new BufferedOutputStream(new FileOutputStream(new File(name + "-uploaded")));
                stream.write(bytes);
                stream.close();
                Payload apayload = new Payload();
                apayload.setData(bytes);
                apayload.setVersion("0.1-SNAPSHOT");
                apayload.setObjectType("image");
                apayload.setStreamerInfo("none");
                apayload.setDatasize(bytes.length);
                PayloadHandler phandler = new PayloadHandler(apayload);
                Payload storable = phandler.getPayloadWithHash();
                payloadrepo.save(storable);
                return "You successfully uploaded " + name + " into " + name + "-uploaded !";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }
}
