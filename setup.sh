#!/bin/sh
alldirs=`ls -1`
alldirs=`find . -name "pom.xml" -print | egrep -v "target" | awk -F'/' '{print $2}' | egrep -v "pom"`
wdir=$PWD

# Change this variables to point to your local installation
for adir in $(echo $alldirs | tr ";" "\n"); do
##for adir in $alldirs; do
echo "present directory is $wdir: examine $adir"
echo "linking properties..." 
cd $adir
if [ ! -e conddb-filter-values.properties ]; then
 echo "linking properties file"
 ln -s $wdir/conddb-filter-values.properties .
fi
cd $wdir
done
