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
package conddb.web.config;

import javax.ws.rs.core.Response;

import conddb.data.ErrorMessage;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;

import java.net.URI;

public abstract class BaseController {

    protected Response created(Link resource) {
        String href = (String)resource.get("href");
        URI uri = URI.create(href);
        return Response.created(uri).status(Response.Status.OK).entity(resource).build();
    }

    protected ConddbWebException buildException(String msg, String internalmsg, Response.Status status) {
    	ConddbWebException ex = new ConddbWebException(msg);
		ErrorMessage error = new ErrorMessage(msg);
		error.setCode(status.getStatusCode());
		error.setInternalMessage(internalmsg);
		ex.setStatus(status);
		ex.setErrMessage(error);
		return ex;    	
    }
}
