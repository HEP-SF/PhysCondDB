#
# Prototype project for Conditions Database
# Authors: A.Formica
# Date : 2014/12/10
#
This project uses maven for build.
It can be deployed in tomcat or JBoss wildfly.


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



