/**
 * 
 */
package conddb.calibration.tools;

import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;

/**
 * @author aformic
 *
 */
public interface ICalibService {

	/**
	 * This method insert a new Iov and the corresponding payload into an existing tag
	 * 
	 * @param atag
	 * @param iov
	 * @param payload
	 * @throws ConddbServiceException
	 */
	void commit(Tag atag, Iov iov, Payload payload) throws ConddbServiceException;
	
	/**
	 * This method create a new association between all tags having a given 
	 * pattern and an existing global tag
	 * @param atag
	 * @param globaltag
	 * @throws ConddbServiceException
	 */
	void tagPackage(String tagpattern, GlobalTag globaltag) throws ConddbServiceException;
	
}
