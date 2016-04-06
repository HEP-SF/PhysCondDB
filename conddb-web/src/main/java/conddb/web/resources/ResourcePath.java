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

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.SystemDescription;
import conddb.data.AfEntity;

public enum ResourcePath {

    globaltags(Link.GLOBALTAGS, GlobalTag.class),
    maps(Link.GLOBALTAGMAPS, GlobalTagMap.class),
    tags(Link.TAGS, Tag.class),
    iovs(Link.IOVS, Iov.class),
    payload(Link.PAYLOAD, Payload.class),
    payloaddata(Link.PAYLOADDATA, PayloadData.class),
    systems(Link.SYSTEMS, SystemDescription.class)
    ;

    final String path;
    final Class<? extends AfEntity> associatedClass;

    private ResourcePath(String elt, Class<? extends AfEntity> clazz) {
        path = elt;
        associatedClass = clazz;
    }

    public static ResourcePath forClass(Class<? extends AfEntity> clazz) {
        for (ResourcePath rp : values()) {
            //Cannot use equals because of hibernate proxied object
            //Cannot use instanceof because type not fixed at compile time
            if (rp.associatedClass.isAssignableFrom(clazz)) {
                return rp;
            }
        }
        throw new IllegalArgumentException("No ResourcePath for class '" + clazz.getName() + "'");
    }

    public String getPath() {
        return path;
    }

    public Class<? extends AfEntity> getAssociatedClass() {
        return associatedClass;
    }
}
