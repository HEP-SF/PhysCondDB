#########################################################
#       PhysCondDB                        #
# Author: A.Formica                       #
# Date : 2015/01/01                       #
#########################################################
ROOT project directory : <PhysCondDB>

This project is based on maven (mvn) so you need 
to install maven to be able to compile it.
The project has been tested using java versions 7 and 8.

0) Pre-compilation instructions
You need to create a property file in your <PhysCondDB> directory
and link this file in all modules sub-directories
The file is named: conddb-filter-values.properties
It contains a password for Oracle ATLAS_COND_TOOLS READER account:
condreader=xxxxxx 
This password is used in Web modules.
If you set it to a fake password, all services related to ATLAS COOL data
retrieval will not be active.

1) Build
Execute the following commands from project ROOT directory.
To compile the code:
<PhysCondDB>/$ mvn clean compile 
To package the code:
<PhysCondDB>/$ mvn package 
To install the compiled jars into maven repository
<PhysCondDB>/$ mvn install 

The previous steps can also be included in the same line
<PhysCondDB>/$ mvn clean install 
During this phase tests are also executed.


MODULES:
CondDBCool => Web application that is used to retrieve data from ATLAS COOL DB (using PL/SQL package)
              Go to the CondDBCool/README.txt for more informations.
CondDBWeb => Web application that is used to access the PhysCondDB conditions DB core services
              Go to the CondDBWeb/README.txt for more informations.


2) Deployment
   We can deploy the application in several environments. Below we list
   instructions for tomcat and Jboss wildfly.
   
   Tomcat7:
   ========
   Go to the tomcat installation directory to start tomcat server. In general
   this is defined as CATALINA_HOME.
   Set the profiles that you want to use:
   <$CATALINA_HOME>/$ export CATALINA_OPTS="-Dspring.profiles.active=prod,h2,basic"
  
   These profiles refer to 
   - prod : the database connection in tomcat (spring definition)
   - h2 : the database connections parameters for h2 database
   - basic : the authentication profile to be used
  
   If you are not at cern, you may need socks proxy to allow connections to Oracle DB
   that we use for testing; in this case the options should be:
   <$CATALINA_HOME>/$ export CATALINA_OPTS="-Dspring.profiles.active=prod,h2,basic -DsocksProxyHost=localhost -DsocksProxyPort=3129"
   
   Start tomcat server:
   <$CATALINA_HOME>/$ ./bin/catalina.sh start
   
   Go to project ROOT directory and choose the web module to install:
   <PhysCondDB>/$ cd CondDBCool
   <PhysCondDB>/CondDBCool$ mvn tomcat7:redeploy
 or
   <PhysCondDB>/$ cd CondDBWeb
   <PhysCondDB>/CondDBWeb$ mvn tomcat7:redeploy
   
   
   This command suppose that tomcat is running under: http://localhost:8080
   
   Jetty
   ========
   Jetty is a small server embedded within the application. Single modules can be deployed in Jetty
   by using the following command:

   Go to project ROOT directory and choose the web module to install:
   <PhysCondDB>/$ cd CondDBCool
   <PhysCondDB>/CondDBCool$ mvn -Dspring.profiles.active=dev,h2,basic jetty:run
 or
   <PhysCondDB>/$ cd CondDBWeb
   <PhysCondDB>/CondDBWeb$ mvn -Dspring.profiles.active=dev,h2,basic jetty:run
   
   In this case, the modules cannot be started together, but only one at the time.
   
     
   
3) spring profiles available:
   This prototype has profiles to create appropriate datasources and JNDI naming
   for retrieving a connection depending on the server where the module has been deployed.
   We can then use an environment variable to set the appropriate profile:
   spring.profiles.active 
   
   The variable can be set at command line level or using JAVA_OPTS or CATALINA_OPTS variables.
   
   
   
   

#########################################################
