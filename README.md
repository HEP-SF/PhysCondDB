#       PhysCondDB      

#### Author: A.Formica      
##### Date : 2015/01/01 

This project is based on maven (mvn) so you need to install maven to be able to compile it.
The project has been tested using java versions 7 and 8.
In the following instruction we are using for project directory : <PhysCondDB>

## Pre-compilation instructions
You need to create a property file in your <PhysCondDB> directory and link this file in all modules sub-directories.
The file is named: `conddb-filter-values.properties`
It contains a set of parameters which are used to set user names and passwords. Depending on the testing that you want to perform, you do not need all these parameters. Here is a list of the parameters which can be set for the moment. 
- `condreader=xxxxxx` : password for an Oracle account containing PL/SQL code for accessing COOL tables.
- `tomcat.datasource=java/MYDS` : _datasource_ used in tomcat, and declared in `$CATALINA_HOME/conf/server.xml` file where _datasources_ can be defined in order to _share_ them among different applications.
- `h2.file.name=/tmp/h2physconddb` : name of the file containing the h2 database on local disk (used for *jetty* testing for example).
- `devuser=xxxxxx` : user name which can be used for Oracle DB connection for testing under `devdb11`.
- `devpassword=xxxxxx` : password which can be used for Oracle DB connection for testing under `devdb11`.

These passwords are used in the Web modules (e.g.: CondDBWeb and CondDBCool).
Some of the fields in this file are very important in case of deployment under *jetty*, *tomcat7* or *jboss*.

## Build instructions
Execute the following commands from project ROOT directory.
- To compile the code: `<PhysCondDB>/$ mvn clean compile `
- To package the code: `<PhysCondDB>/$ mvn package `
- To install the compiled jars into the user maven repository: `<PhysCondDB>/$ mvn install `

The previous steps can also be included in the same line
- `<PhysCondDB>/$ mvn clean install`
During this phase tests are also executed (the code is contained in `<package>/src/test/...`)

## Modules description
1. CondDBCool => Web application that is used to retrieve data from ATLAS COOL DB (using PL/SQL package)
              Go to the CondDBCool/README.txt for more informations.
2. CondDBWeb => Web application that is used to access the PhysCondDB conditions DB core services
              Go to the CondDBWeb/README.txt for more informations.


## Deployment instructions
We can deploy the application in several environments. Below we list instructions for tomcat and Jboss wildfly.
   
###   Tomcat7
Go to the tomcat installation directory to start tomcat server. In general
this is defined as `CATALINA_HOME`.
Set the profiles that you want to use before starting tomcat.

`<$CATALINA_HOME>/$ export CATALINA_OPTS="-Dspring.profiles.active=prod,h2,basic"`
  
These profiles refer to 
- *prod* : the database connection in tomcat (spring definition)
- *h2* : the database connections parameters for h2 database
- *basic* : the authentication profile to be used
  
If you are not at cern, you may need socks proxy to allow connections to Oracle DB
that we use for testing; in this case the options should be:

`<$CATALINA_HOME>/$ export CATALINA_OPTS="-Dspring.profiles.active=prod,h2,basic -DsocksProxyHost=localhost -DsocksProxyPort=3129"`
   
Start tomcat server:
`<$CATALINA_HOME>/$ ./bin/catalina.sh start `
   
Go to project ROOT directory and choose the web module to install:
```
   <PhysCondDB>/$ cd CondDBCool
   <PhysCondDB>/CondDBCool$ mvn tomcat7:redeploy
```   
or
```
   <PhysCondDB>/$ cd CondDBWeb
   <PhysCondDB>/CondDBWeb$ mvn tomcat7:redeploy
```   
   
This command suppose that tomcat is running under: `http://localhost:8080`
   
### Jetty
Jetty is a small server embedded within the application. Single modules can be deployed in Jetty by using the following command:

Go to project ROOT directory and choose the web module to install:
```
   <PhysCondDB>/$ cd CondDBCool
   <PhysCondDB>/CondDBCool$ mvn -Dspring.profiles.active=jetty,h2,basic jetty:run
```   
or
```
   <PhysCondDB>/$ cd CondDBWeb
   <PhysCondDB>/CondDBWeb$ mvn -Dspring.profiles.active=jetty,h2,basic jetty:run
```   
In this case, the modules cannot be started together, but only one at the time.
   
     
   
## Using spring profiles
This prototype has profiles to create appropriate _datasources_ and JNDI naming for retrieving a connection depending on the server where the module has been deployed.
We can then use an environment variable to set the appropriate profile:
   `spring.profiles.active`
   
The variable can be set at command line level or using `JAVA_OPTS` or `CATALINA_OPTS` variables.
   
