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

This project is based on maven (mvn) so you need to install maven to be able to compile it.
The project has been tested using java versions 7 and 8.
In the following instruction we are using for project directory : `<PhysCondDB>/CondDBWeb`

## Deployment:

- tomcat deployment: `mvn clean tomcat7:redeploy`
- jetty deployment for testing: `mvn -Dspring.profiles.active=jetty,h2,basic jetty:run`
- wildfly deployment: for the moment just copy the war file created in `./target ` into the wildfly deployment directory
 	
Be careful is you are using authentication that the correct profile is set in `CATALINA_OPTS`

## List of delivered services
The PhysCondDB/CondDBWeb module delivers a set of RESTful services that can be used to interact with the conditions DB. The present DB structure is describe in the module `<PhysCondDB>/CondDBData `. The low level services for DB interactions are defined in `<PhysCondDB>/CondDBServices `. The RESTful services allows data retrieval methods implemented via HTTP protocol. In the following list we describe the methods implemented until now.  

* Automatic discovery of services.
   The application uses spring HATEOS to implement automatic discovery of delivered services.
   Use the following URL to navigate in the conditions DB.
```   
  http://localhost:8080/physconddb/conddb/alps   GET
```
   This set of services can be useful for the development of a client in python that can discover the services and propose to the user a simple way of navigation into the tables content.
   
* General purpose services
   In order to deliver more appropriate queries, the application implements a set of high level
   services which are performing more specific DB queries.
```   
  http://localhost:8080/physconddb/conddbweb/tracetags   GET
```  
   Arguments: ....

## Some command examples:
Once the h2 database has been filled, you can perform query actions.
* Query global tags: 
`curl http://localhost:8090/conddb/globalTags/search/findByNameLike\?name=CONDBR2%25`




