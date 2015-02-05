#########################################################
#       CondDBCool                                      #
# Author: A.Formica                                     #
# Date : 2015/01/01                                     #
#########################################################
ROOT module directory : <PhysCondDB>/CondDBCool

This project is based on maven (mvn) so you need 
to install maven to be able to compile it.
The project has been tested using java versions 7 and 8.

1) Deployment:
	tomcat deployment: mvn tomcat7:redeploy
	jetty deployment for testing: mvn -Dspring.profiles.active=jetty,h2,basic jetty:run
	
Be careful is you are using authentication that the correct profile is set in "CATALINA_OPTS"

2) List of delivered services
The COOL access and data retrieval allows to list tags and global tags
in COOL via PL/SQL package, and to migrate tags and iovs from COOL into
the PhysCondDB database.
 - http://localhost:8080/physconddbcool/cool/tracetags   GET
   Retrieves global tags and tags from COOL
   Arguments: 
   	name = schema name (default to ATLAS_COOL%)
   	dbname = database instance of COOL (default to CONDBR2)
   	gtag = global tag name (default to CONDBR2%)
   	
 - http://localhost:8080/physconddbcool/admin/loadtags   POST
   Arguments: ...
   
 - http://localhost:8080/physconddbcool/admin/loadiovs   POST
   Arguments: ...
   
   
CAVEAT: in case of jetty avoid putting the physconddbcool path in the url.
 ===>>>  http://localhost:8080/cool/tracetags

3) Refer to ../README.md for instructions on building and deploying this application.

============================================================
# Use the following commands to test services
# 1: try access to administratif services: should fail without authentication
curl -i -X POST http://localhost:8080/physconddbcool/admin/loadtags
# 2: try access to standard unprotected services : should work
curl -i -X GET http://localhost:8080/physconddbcool/cool/tracetags
# 3: try to authenticate with WRONG parameters : should fail
curl -i -X POST --user user:user http://localhost:8080/physconddbcool/admin/loadtags
# 3: try to authenticate with CORRECT parameters : should work
curl -i -X POST --user user:userPass http://localhost:8080/physconddbcool/admin/loadtags

