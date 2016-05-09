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

This project contains utility libraries for the interaction with the server of conditions data. In the following we assume that the path is : `<PhysCondDB>/python/cliadmin`

## Calibration client:

The python client `calibcli.py` is conceived for the calibration files use case. It is a prototype created after discussions with Will Buttinger. To overview the options available you can use the option -h.

Short list of commands as examples:

* COMMIT a file: insert a new file in a given package to the corresponding destination path
```   
calibcli.py  COMMIT  <packagename> <localfilenameandpath> <destpath>
```   
Example:
```   
python calibcli.py --url=aiatlas137.cern.ch:8080/physconddb COMMIT JavaDocPkg primefaces_users_guide_3_4.pdf   /JavaDocPkg/Computing/Generics
```
```
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb COMMIT JavaDocPkg jquery-getting-started.pdf   /JavaDocPkg/Computing/Generics
```

* TAG a package : associate the last versions of the files to a (new) global tag
``` 
calibcli.py TAG <package-name> <globaltagname>
```
Example:
```
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb TAG JavaDocPkg JavaDocPkg-00-03
```
* LS list the files under a global tag
``` 
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb LS <globaltagname>
```
Example:
```
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb LS JavaDocPkg-00-03
```
* LOCK a global tag
``` 
calibcli.py LOCK <globaltagname> <lockstatus> (Default: LOCKED)
```
Using calibcli command one can add the package name as last argument. In this case it will dump
the correct directory structure. Otherwise the directory structure will start from the global tag name.
Example:
```
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb LOCK JavaDocPkg-00-03
```

* COLLECT a global tag (dump in the server /tmp/ area the full global tag directory structure)
``` 
calibcli.py COLLECT <globaltagname> <asg global tag> 
```
Example:
```
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb  COLLECT JavaDocPkg-00-03 ASG-00-01
```

* TAR a global tag (download a previously dumped global tag from the server into a local TAR file named globaltag-temp.tar)
``` 
calibcli.py TAR <globaltagname> 
```
Example:
```
calibcli.py --url=aiatlas137.cern.ch:8080/physconddb  TAR JavaDocPkg-00-03
```
## Administration client:
The file phcli.py can be used for managing the conditions data. It uses all services delivered by the server, allowing to insert, update and retrieve metadata informations.

