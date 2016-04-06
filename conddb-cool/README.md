#       PhysCondDB

#### Author: A.Formica
##### Date of last development period: 2015/01/01
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
## Description
Test project for the implementation of a generic purpose conditions database for physics experiment.

This project is based on maven (mvn) so you need to install maven to be able to compile it.
The project has been tested using java versions 7 and 8.
In the following instruction we are using for project directory : `<PhysCondDB>/CondDBCool`

## Pre-compilation instructions
You need to follow the general project README.md for the installation of needed dependency jars.

## Deployment
- tomcat deployment: `mvn clean tomcat7:redeploy`
- jetty deployment for testing: `mvn -Dspring.profiles.active=jetty,h2,basic jetty:run`
- wildfly deployment: for the moment just copy the war file created in `./target ` into the wildfly deployment directory

Be careful is you are using authentication that the correct profile is set in `CATALINA_OPTS`

## List of delivered services
Some basic services are used to provide COOL access and data retrieval in order to list tags and global tags
stored in a COOL instance via PL/SQL package which has been developed inside ATLAS_COND_TOOLS.
Other services are used to copy COOL related data into the PhysCondDB database.
- trace cool global tags and tags : `http GET localhost:8080/physconddbcool/cool/tracetags` 
  Arguments: 
   	name = schema name (default to ATLAS_COOL%)
   	dbname = database instance of COOL (default to CONDBR2)
   	gtag = global tag name (default to CONDBR2%)
   	
- load global tags and tags from COOL : `http POST localhost:8080/physconddbcool/admin/loadtags`
   Arguments: ...
   
- load iovs from COOL: `http POST localhost:8080/physconddbcool/admin/loadiovs`
   Arguments: ...
  
## Some command line examples
============================================================
- Use the following commands to test services
* try access to administratif services: should fail without authentication
  ```curl -i -X POST http://localhost:8080/physconddbcool/admin/loadtags```
* try access to standard unprotected services : should work
  ```curl -i -X GET http://localhost:8080/physconddbcool/cool/tracetags```
* try to authenticate with WRONG parameters : should fail
  ```curl -i -X POST --user user:user http://localhost:8080/physconddbcool/admin/loadtags```
* try to authenticate with CORRECT parameters : should work
  ```curl -i -X POST --user user:userPass http://localhost:8080/physconddbcool/admin/loadtags```

