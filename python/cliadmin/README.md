#       Python Client for PhysCondDB      
#### Author: A.Formica      
##### Date : 2015/10/01 

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
1. [Installation instructions](#installation-instructions)
2. [General options](#general-options)
3. [Calibration client](#calibration-client)
4. [Administration client](#administration-client)

## Installation instructions
Install the sagger client API. Go to `<prj_home>/python/swagger-physcond` and run the installation script:
``` 
python setup.py install
``` 

## General options
List of options available for command line clients.
1. `--url="http:<hostname>:<port>/physconddb/api/rest"` : allow to select a specific host for the server.
2. `--by=<field><op><value> ` : allow to filter results by selecting a specific value for the given field.
The `<op>` available options are `:`, which represents `like` , `< , >` which represent `less than` or `greater than`.
3. `--trace=[on|off]` : to enable tracing, used when querying global tags or tags.
4. `--debug=[true|false]` : to enable debugging.
5. `--expand=[true|false]` : to expand the results, if false it shows only urls to resources.

## Calibration client:
In the following we assume that the path is : `<prj_home>/python/cliadmin`.
The python client `calibcli.py` is conceived for the calibration files use case. It is a prototype created after discussions with Will Buttinger. To overview the options available you can use the option -h.

Short list of commands as examples:

* COMMIT a file: insert a new file in a given package to the corresponding destination path
```   
calibcli.py  COMMIT  <packagename> <localfilenameandpath> <destpath>
```   
Example:
```   
calibcli.py COMMIT JavaDocPkg primefaces_users_guide_3_4.pdf   /JavaDocPkg/Computing/Generics
```
```
calibcli.py COMMIT JavaDocPkg jquery-getting-started.pdf   /JavaDocPkg/Computing/Generics
```

* TAG a package : associate the last versions of the files to a (new) global tag
``` 
calibcli.py TAG <package-name> <globaltagname>
```
Example:
```
calibcli.py TAG JavaDocPkg JavaDocPkg-00-03
```
* LS list the files under a global tag
``` 
calibcli.py LS <globaltagname>
```
Example:
```
calibcli.py LS JavaDocPkg-00-03
```
* LOCK a global tag
``` 
calibcli.py LOCK <globaltagname> <lockstatus> (Default: LOCKED)
```
Using calibcli command one can add the package name as last argument. In this case it will dump
the correct directory structure. Otherwise the directory structure will start from the global tag name.
Example:
```
calibcli.py LOCK JavaDocPkg-00-03
```

* COLLECT a global tag (dump in the server /tmp/ area the full global tag directory structure)
``` 
calibcli.py COLLECT <globaltagname> <asg global tag> 
```
Example:
```
calibcli.py COLLECT JavaDocPkg-00-03 ASG-00-01
```

* TAR a global tag (download a previously dumped global tag from the server into a local TAR file named globaltag-temp.tar)
``` 
calibcli.py TAR <globaltagname> 
```
Example:
```
calibcli.py TAR JavaDocPkg-00-03
```

## Administration client:
In the following we assume that the path is : `<prj_home>/python/cliadmin`.
The file phcli.py can be used for managing the conditions data. It uses all services delivered by the server, allowing to insert, update and retrieve metadata informations. Type -h for a detailed list of options.

The commands mentioned below use a prototype of conditions DB which is for the moment in Oracle at CERN.
If you run them over your private instance they will not work if you have not filled the DB.

Short list of commands as examples:

* Search global tags, tags or systems
```   
phcli.py  FIND globaltags [tags,systems]
```   
Example:
```   
phcli.py --by=name:BLKPA FIND globaltags 
```
The command below will retrieve the list of associated tags. It will fail if no associated tags are present.
```
phcli.py FIND globaltags CONDBR2-BLKPA-2015-07
```
The command below will retrieve the list of associated global tags.
```
phcli.py FIND tags TRTCalibDX-RUN2-BLK-UPD4-03
```

* Insert global tags, tags or systems
```   
phcli.py  ADD tags [globaltags,systems]
```   
Example:
```   
phcli.py add globaltags "name=AF-GTAG-01;release=1.0;description=Test global tag;validity=1000"
phcli.py add tags "name=AF-TEST-01;timeType=time;objectType=testblob;description=Test tag;lastValidatedTime=0;synchronization=none;endOfValidity=9"
```
In order to see what format the tag creation string has to appear you can type
```
phcli.py DESCRIBE tag [globaltag, map, system]
```
* Associate a tag to a global tag, or remove association
```
phcli.py LINK AF-GTAG-01 AF-TEST-01 "record=test;label=my package"
phcli.py UNLINK AF-GTAG-01 AF-TEST-01 
``` 
