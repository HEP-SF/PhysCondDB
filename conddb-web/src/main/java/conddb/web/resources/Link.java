/*
 * Copyright (C) 2012 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package conddb.web.resources;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import javax.persistence.Id;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.annotations.Href;
import conddb.data.Entity;
import conddb.utils.PropertyConfigurator;

@SuppressWarnings("unchecked")
public class Link extends LinkedHashMap {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7923124693141346797L;

	public static final String PATH_SEPARATOR = "/";

    public static final String GLOBALTAGS = PATH_SEPARATOR + "globaltags";
    public static final String GLOBALTAGMAPS = PATH_SEPARATOR + "maps";
    public static final String TAGS = PATH_SEPARATOR + "tags";
    public static final String IOVS = PATH_SEPARATOR + "iovs";
    public static final String PAYLOAD = PATH_SEPARATOR + "payload";
    public static final String EXPERT = PATH_SEPARATOR + "expert";
    public static final String ADMIN = PATH_SEPARATOR + "admin";
    public static final String CALIB = PATH_SEPARATOR + "calibration";
    public static final String SYSTEMS = PATH_SEPARATOR + "systems";
    public static final String PAYLOADDATA = PAYLOAD + "/data";
    public static final String MONITOR = PATH_SEPARATOR + "monitor";

	private Logger log = LoggerFactory.getLogger(this.getClass());
	protected Entity parent = null;

    public Link(UriInfo info, Entity entity) {
        this(getFullyQualifiedContextPath(info), entity);
        this.serializeTimestamps();
    }
    
    public Link(UriInfo info, Entity entity, Entity parent) {
        this(getFullyQualifiedContextPath(info), entity);
        this.parent = parent;
        this.serializeTimestamps();
    }

    public Link(String fqBasePath, Entity entity) {
        String href = createHref(fqBasePath, entity);
        put("href", href);
    }

    public Link(UriInfo info, String subPath) {
        this(getFullyQualifiedContextPath(info), subPath);
    }

    public Link(String fqBasePath, String subPath) {
        String href = fqBasePath + subPath;
        put("href", href);
    }

    protected static String getFullyQualifiedContextPath(UriInfo info) {
        String fq = info.getBaseUri().toString();
        if (fq.endsWith("/")) {
            return fq.substring(0, fq.length()-1);
        }
        return fq;
    }

//    protected String createHref(String fqBasePath, Entity entity) {
//    	log.debug("Create href link from "+fqBasePath+" for entity "+entity);
//        StringBuilder sb = new StringBuilder(fqBasePath);
//        ResourcePath path = ResourcePath.forClass(entity.getClass());
//        sb.append(path.getPath()).append(PATH_SEPARATOR).append(entity.getResId());
//        return sb.toString();
//    }
    
    protected String createHref(String fqBasePath, Entity entity) {
    	log.debug("Create href link from "+fqBasePath+" for entity "+entity);
        StringBuilder sb = new StringBuilder(fqBasePath);
        ResourcePath path = ResourcePath.forClass(entity.getClass());
        String href = entity.getHref();
        if (href.equals("none")) {
        try {
			Method getid = this.getHref(entity);
			href = getid.invoke(entity, null).toString();
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        // Now URLEncode the href obtained, before creating the full href string with the path
        String encodedhref = href;
        try {
			encodedhref = URLEncoder.encode(href,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        sb.append(path.getPath()).append(PATH_SEPARATOR).append(encodedhref);
        return sb.toString();
    }

    
    /**
     * This method looks for annotation HREF in order to create the links. If no @Href is found
     * then it uses the annotation @Id.
     * @param entity
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected Method getHref(Entity entity) throws NoSuchMethodException, SecurityException {
    	Field[] fields = entity.getClass().getDeclaredFields();
    	Method getiddefault = null;
    	for (int i=0 ; i<fields.length ; i++) {
    		Field afield = fields[i];
    		Href hrefid = afield.getAnnotation(Href.class);
    		if (hrefid != null) {
    			String methname = "get"+afield.getName().substring(0, 1).toUpperCase()+afield.getName().substring(1);
    			Method getid = entity.getClass().getMethod(methname, null);
    			return getid;
    		}
    		javax.persistence.Id annid = afield.getAnnotation(javax.persistence.Id.class);
    		if (annid != null) {
    			String methname = "get"+afield.getName().substring(0, 1).toUpperCase()+afield.getName().substring(1);
    			getiddefault = entity.getClass().getMethod(methname, null);
    		}

    	}
		Method methods[] = entity.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			String akey = methods[i].getName();
			if (methods[i].getAnnotation(Id.class) != null && akey.startsWith("get")) {
				getiddefault = methods[i];
			}
			if (methods[i].getAnnotation(Href.class) != null && akey.startsWith("get")) {
				return methods[i];
			}
		}
    	return getiddefault;
    }


    protected String format(Timestamp ts) {
		try {
			Instant fromEpochMilli = Instant.ofEpochMilli(ts.getTime());
			ZonedDateTime zdt = fromEpochMilli.atZone(ZoneId.of("Europe/Paris"));
			return zdt.format(PropertyConfigurator.getInstance().getLocformatter());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
    //TimestampFormat tsformat
    public void serializeTimestamps() {
		log.debug("time format "+PropertyConfigurator.getInstance().getPattern());
		Timestamp ts = (Timestamp) get("insertionTime");
		if (ts != null) {
			String tsstr = format(ts);
			put("insertionTime", tsstr);
		}
		ts = (Timestamp) get("modificationTime");
		if (ts != null) {
			String tsstr = format(ts);
			put("modificationTime", tsstr);
		}
		ts = (Timestamp) get("snapshotTime");
		if (ts != null) {
			String tsstr = format(ts);
			put("snapshotTime", tsstr);
		}
	}
    
    public String getHref() {
        return (String)get("href");
    }

}
