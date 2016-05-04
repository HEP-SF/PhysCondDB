#       CondDBWeb      
#### Author: A.Formica      
##### Date : 2015/01/01 

```
   Copyright (C) 2015  A.Formica

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

# Table of Contents
1. [Deployment](#deployment)
2. [List of delivered services](#list-of-delivered-services)
3. [Command examples](#command-examples)


## Deployment

- tomcat deployment: `mvn clean tomcat7:redeploy`
- jetty deployment for testing: `mvn -Dspring.profiles.active=jetty,h2 jetty:run`
- wildfly deployment: for the moment just copy the war file created in `./target ` into the wildfly deployment directory
 	
Be careful is you are using authentication that the correct profile is set in `CATALINA_OPTS`

## List of delivered services
The PhysCondDB/CondDBWeb module delivers a set of RESTful services that can be used to interact with the conditions DB. The present DB structure is described in the module `conddb-data `. The low level services for DB interactions are defined in `conddb-svc `. The RESTful services allows data management methods implemented via HTTP protocol. In the following list we describe how to get the documentation on the methods available.  

* Swagger documentation of services.
   The application uses swagger to document the REST services, by putting some specific annotations on the @Controller 
methods which implement a given task. To get the swagger description of the application you can download the json file that is generated at the url:
```   
  http://localhost:8080/physconddb/api/rest/swagger.json   GET
```
* Visualize the services
  Use *swagger-ui* to visualize the services and test them. You can also generate dynamic html documentation using *swagger-codegen*.

## Command examples
TO BE DONE.



