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
import javax.ws.rs.core.UriInfo;

import conddb.data.AfEntity;
import conddb.data.ErrorMessage;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.generic.GenericPojoResource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public abstract class BaseController {

	protected Response created(Link resource) {
		String href = (String) resource.get("href");
		URI uri = URI.create(href);
		return Response.created(uri).status(Response.Status.CREATED).entity(resource).build();
	}

	protected Response updated(Link resource) {
		String href = (String) resource.get("href");
		URI uri = URI.create(href);
		return Response.created(uri).status(Response.Status.ACCEPTED).entity(resource).build();
	}

	protected Response ok(Link resource) {
		String href = (String) resource.get("href");
		URI uri = URI.create(href);
		return Response.created(uri).status(Response.Status.OK).entity(resource).build();
	}
	
	protected Response response(Link resource, Response.StatusType status) {
		String href = (String) resource.get("href");
		URI uri = URI.create(href);
		return Response.created(uri).status(status).entity(resource).build();
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

	protected <T extends AfEntity> CollectionResource listToCollection(Collection<T> coll, boolean expand,
			UriInfo info, String subPath) {
		Collection<Link> items = new ArrayList<Link>(coll.size());
		for (T entity : coll) {
			if (expand) {
				GenericPojoResource<AfEntity> resource = new GenericPojoResource<AfEntity>(info, entity);
				items.add(resource);
			} else {
				Link link = new Link(info, entity);
				items.add(link);
			}
		}
		return new CollectionResource(info,subPath,items);
	}
	
	protected <T extends AfEntity> CollectionResource listToCollection(Collection<T> coll, boolean expand,
			UriInfo info, String subPath, int offset, int limit) {
		Collection<Link> items = new ArrayList<Link>(coll.size());
		for (T entity : coll) {
			if (expand) {
				GenericPojoResource<AfEntity> resource = new GenericPojoResource<AfEntity>(info, entity);
				items.add(resource);
			} else {
				Link link = new Link(info, entity);
				items.add(link);
			}
		}
		return new CollectionResource(info,subPath,items,offset,limit);
	}
}
