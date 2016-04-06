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
In the following instruction we are using for project directory : `<PhysCondDB>`

## Pre-compilation instructions
You need to create a property file in your <PhysCondDB> directory and link this file in all modules sub-directories.
The file is named: `conddb-filter-values.properties`
It contains a set of parameters which are used to set user names and passwords. Depending on the testing that you want to perform, you do not need all these parameters. Here is a list of the parameters which can be set for the moment. 
- `condreader=xxxxxx` : password for an Oracle account containing PL/SQL code for accessing COOL tables (used only for ATLAS).
- `tomcat.datasource=java/MYDS` : _datasource_ used in tomcat, and declared in `$CATALINA_HOME/conf/server.xml` file where _datasources_ can be defined in order to _share_ them among different applications.
- `h2.file.name=/tmp/h2physconddb` : name of the file containing the h2 database on local disk (used for *jetty* testing for example).
- `devuser=xxxxxx` : user name which can be used for Oracle DB connection for testing under `devdb11`  (used only for testing environment in ATLAS/CMS).
- `devpassword=xxxxxx` : password which can be used for Oracle DB connection for testing under `devdb11`  (used only for testing environment in ATLAS/CMS).

These passwords (condreder, devuser, devpassword) are used in the Web modules (e.g.: CondDBWeb and CondDBCool). You should change the relative configuration files of CondDBWeb module in order to write your own DB connection. 
For example in case of jetty deployment you can modify the content of `CondDBWeb/src/main/webabb/WEV-INF/jett-env.xml`, if you intend to set a connection to a DB server which is not a local H2 database. The external server connection is something like this for Oracle:

```
<New id="oradevdatasource" class="org.eclipse.jetty.plus.jndi.Resource">
        <Arg>jdbc/OraDB</Arg>
        <Arg>
            <New class="org.apache.commons.dbcp.BasicDataSource">
                <Set name="driverClassName">oracle.jdbc.OracleDriver</Set>
                <Set name="url">jdbc:oracle:thin:@(DESCRIPTION = 
        (ADDRESS= (PROTOCOL=TCP) (HOST=mydbhostname) (PORT=aportnumber) )
        (ENABLE=BROKEN)
        (CONNECT_DATA=
                (SERVICE_NAME=aservicename)
        )
)</Set>
                <Set name="username">${devuser}</Set>
                <Set name="password">${devpassword}</Set>
            </New>
        </Arg>
</New>
```
The important configuration here (a part from the DB connection string) consists in the JNDI
name of the connection: `jdbc/OraDB`.
This name should be the same as the one defined in spring configuration files: `CondDBWeb/src/main/resourcer/spring/sql/oracle.properties`.
Here you can find a variable called: `serverMode.jetty.localDataSource`, which should then be set equal to `jdbc/OraDB`. In the case of other deployments (like tomcat), the system is using
other variables (e.g. `serverMode.tomcat.localDataSource`). In this case the name has to match the definition of the connection that you will configure in your tomcat server. This can be done in 2 ways: either for all applications (inside `$CATALINA_HOME/conf/xxx.xml` configuration files) or via a self-contained `context.xml` file, that you can find in `CondDBWeb/src/main/resources/META-INF/context.xml`. Be careful since the JNDI name will be different if you are in tomcat or JBoss server. The name examples can be find in the provided files (`adatabase.properties`). The name of the selected properties file that spring will pick up is drived via a spring profile: `h2` or `oracle` will load `h2.properties` or `oracle.properties`.

If you use in addition the `dev` spring profile (instead of `prod`) you may define directly via properties, inside the `database.properties` file, those properties that will activate a connection for your application. The definition of the datasources is inside the
`CondDBWeb/src/main/resources/spring/db/dao-datasources-context.xml` file.


Some of the fields in this file are very important in case of deployment under *jetty*, *tomcat7* or *jboss*.

## Oracle driver installation
Since Oracle JDBC driver is not present in maven repositories, you should install it locally using
the following procedure. Download the ojdbc6.jar file in your computer.
Use maven to install it:
`<a_dir_with_the_jar>$ mvn install:install-file -Dfile=./ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3 -Dpackaging=jar`

This will create an appropriate repository for oracle inside your maven local repository (~/.m2/xxxx).

## Build instructions
Execute the following commands from project ROOT directory.
- To compile the code: `<PhysCondDB>/$ mvn clean compile `
- To package the code: `<PhysCondDB>/$ mvn package `
- To install the compiled jars into the user maven repository: `<PhysCondDB>/$ mvn install `. The installation 
step uses the directory `~/.m2/...`. 

The previous steps can also be included in the same line
- `<PhysCondDB>/$ mvn clean install`
During this phase tests are also executed (the code is contained in `<package>/src/test/...`)

The products of the build are store in the `target` directory of every sub-module.

## Modules description
1. conddb-cool => Web application that is used to retrieve data from ATLAS COOL DB (using PL/SQL package)
              Go to the conddb-cool/README.txt for more informations.
2. conddb-web => Web application that is used to access the PhysCondDB conditions DB core services
              Go to the conddb-web/README.txt for more informations.


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
   <PhysCondDB>/$ cd conddb-cool
   <PhysCondDB>/conddb-cool$ mvn tomcat7:redeploy
```   
or
```
   <PhysCondDB>/$ cd conddb-web
   <PhysCondDB>/conddb-web$ mvn tomcat7:redeploy
```   
   
This command suppose that tomcat is running under: `http://localhost:8080`
   
### Jetty
Jetty is a small server embedded within the application. Single modules can be deployed in Jetty by using the following command:

Go to project ROOT directory and choose the web module to install:
```
   <PhysCondDB>/$ cd conddb-cool
   <PhysCondDB>/conddb-cool$ mvn -Dspring.profiles.active=jetty,h2,basic jetty:run
```   
or
```
   <PhysCondDB>/$ cd conddb-web
   <PhysCondDB>/conddb-web$ mvn -Dspring.profiles.active=jetty,h2,basic jetty:run
```   
In this case, the modules cannot be started together, but only one at the time.
   
     
   
## Using spring profiles
This prototype has profiles to create appropriate _datasources_ and JNDI naming for retrieving a connection depending on the server where the module has been deployed.
We can then use an environment variable to set the appropriate profile:
   `spring.profiles.active`
   
The variable can be set at command line level or using `JAVA_OPTS` or `CATALINA_OPTS` variables.

## Skip tests for maven
In order to skip tests when compiling and installing libraries you can use the following option:
```
mvn -Dmaven.test.skip=true     
```   
test are not executed and not compiled
or
```
mvn -DskipTests=true
```
test are not executed

## Some useful commands to test services
* Insert a global tag:
 - ```time curl -H "Content-Type: application/json" -H "Accept: application/json" -X POST -d '{ "name" : "BGTAG_02", "lockstatus" : "unlocked","validity" : 4, "description" : "Second test gtag", "release" : "3.0", "snapshotTime" : "2013-06-01T10:10:10+02:00"}' --proxy socks5h://localhost:3129 http://aiatlas137.cern.ch:8080/physconddb/conddbweb/rest/expert/globaltags```
