#########################################################
#       CondDBWeb                                      #
# Author: A.Formica                                     #
# Date : 2015/01/01                                     #
#########################################################
ROOT module directory : <PhysCondDB>/CondDBWeb

This project is based on maven (mvn) so you need 
to install maven to be able to compile it.
The project has been tested using java versions 7 and 8.

1) Deployment:
	tomcat deployment: mvn tomcat7:redeploy
	jetty deployment for testing: mvn -Dspring.profiles.active=dev,h2,basic jetty:run
	
Be careful is you are using authentication that the correct profile is set in "CATALINA_OPTS"

2) List of delivered services
The PhysCondDB access and data retrieval allows to list tags and global tags, iovs and 
payload (NOT IMPLEMENTED YET)

 * Automatic discovery of services.
   The application uses spring HATEOS to implement automatic discovery of delivered services.
   Use the following URL to navigate in the conditions DB.
 - http://localhost:8080/physconddb/conddb/alps   GET
 
 * General purpose services
   In order to deliver more appropriate queries, the application implements a set of high level
   services which are performing more specific DB queries.
 - http://localhost:8080/physconddb/conddbweb/tracetags   GET
   Arguments: ....

2) Refer to ../README.md for instructions on building and deploying this application.


OLD INSTRUCTIONS...NOW CHECK ../README.md
Using Spring Profiles you do not need to change anything at this level.

For the moment we keep the instructions below as a history of the development.

===============================================================
Tomcat7
=======
Update the file in:

<project>/src/java/main/resources/application.properties

in order to select the 2 properties flagged as used for tomcat7:
serverMode.localDataSource=java:comp/env/java/H2DB
serverMode.coolDataSource=java:comp/env/java/OraAtlrDB

The connections should exists in the file:
<project>/src/java/main/webapp/META-INF/context.xml

Use the following commands for building and deploy:
mvn clean tomcat7:redeploy

JBoss Wildfly
=============
As before, but this time you should select another JNDI format:
serverMode.localDataSource=java:jboss/datasources/H2DB
serverMode.coolDataSource=java:jboss/datasources/JBCoolRestDS

The connections should exists in datasource definition file, as the one
present in:
<project>/src/java/main/resources/jboss/h2db-ds.xml

Use the following commands for building and deploy:
mvn clean package
cp target/physconddb.war $JBOSS_HOME/standalone/deployments/

General remarks:
================
The mentioned properties are used inside the configuration file for connections:
<project>/src/java/main/resources/spring/connections-config.xml



