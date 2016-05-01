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
package conddb.data;

import javax.persistence.Transient;


/**
 * Base class for all persistent entities in the application.
 */
public abstract class AfEntity {

    private String resid = "none";

    public AfEntity(){}

    @Transient
    @Deprecated
    public String getResId() {
        return resid;
    }

    public void setResId(String id) {
        this.resid = id;
    }
    
    @Transient
    public String getHref() {
        return resid;
    }

    public void setHref(String id) {
        this.resid = id;
    }
    
	public abstract boolean equals(Object obj);
	
	public abstract int hashCode();

}
