/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package conddb.dao.repositories.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.dao.repositories.IovRepository;
import conddb.data.PayloadData;
import conddb.utils.bytes.PayloadBytesHandler;

/**
 * @author formica
 *
 */
public class PayloadDataJCRImpl implements PayloadDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String rootpath = "/tmp";

	@Autowired
	private IovRepository iovRepository;

	/**
	 * 
	 */
	public PayloadDataJCRImpl() {
		super();
	}

	/**
	 * @param rootpath
	 */
	public PayloadDataJCRImpl(String rootpath) {
		super();
		this.rootpath = rootpath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * conddb.dao.baserepository.PayloadDataBaseCustom#find(java.lang.String)
	 */
	@Override
	public PayloadData find(String id) {
		try {
			Resource resource = new FileSystemResource(rootpath + "/" + id);
			log.info("Search resource filename "+resource.getFilename()+ " in path " + resource.getURI().getPath());
			BufferedInputStream is = new BufferedInputStream(
					resource.getInputStream());
			byte[] bytes = PayloadBytesHandler.getBytesFromInputStream(is);
			if (bytes != null) {
				PayloadData pyld = new PayloadData(id, bytes);
				return pyld;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * conddb.dao.baserepository.PayloadDataBaseCustom#save(conddb.data.PayloadData
	 * )
	 */
	@Override
	public PayloadData save(PayloadData entity) {
		String id = entity.getHash();
		log.info("Inserting payload data using hash " + entity.getHash());
		try {
			Resource resource = new FileSystemResource(rootpath + "/" + id);
			log.info("Use resource filename "+resource.getFilename()+ " in path " + resource.getURI().getPath());
			File outputfile = resource.getFile();
			BufferedOutputStream stream = new BufferedOutputStream(
					new FileOutputStream(outputfile));
			stream.write(entity.getData());
			stream.close();
			log.info("Writing of file is ended");
			entity.setUri(outputfile.getAbsolutePath());
			return entity;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void delete(String id) {
		try {
			Resource resource = new FileSystemResource(rootpath + "/" + id);
			log.info("Search resource filename "+resource.getFilename()+ " in path " + resource.getURI().getPath()+" for REMOVAL ");
			resource.getFile().delete();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getRootpath() {
		return rootpath;
	}

	public void setRootpath(String rootpath) {
		this.rootpath = rootpath;
	}

}
