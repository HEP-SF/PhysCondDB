#!/bin/bash
# Copy the files from the JS project into the webapp folder

echo "----------------------------------------------------------------"
echo "COPYING FILES..."
echo "----------------------------------------------------------------"
cp -r ../CondJS/src/main/resources/app/bower_components src/main/webapp/condjs
echo "bower_components/ copied."

cp -r ../CondJS/src/main/resources/app/css src/main/webapp/condjs
echo "css/ copied."

cp -r ../CondJS/src/main/resources/app/js src/main/webapp/condjs
echo "js/ copied."

cp -r ../CondJS/src/main/resources/app/partials src/main/webapp/condjs
echo "partials/ copied."

cp ../CondJS/src/main/resources/app/index.html src/main/webapp/condjs
echo "index.html copied."

echo "----------------------------------------------------------------"
echo "REPLACE THE LOCALHOST WITH THE REAL URL..."
echo "----------------------------------------------------------------"

grep "http://localhost:8080/physconddb/conddbweb/" src/main/webapp/condjs/js/services.js | sed 's/http:\/\/localhost:8080\/physconddb\/conddbweb/https:\/\/test-physconddb.web.cern.ch\/test-physconddb/'

echo "----------------------------------------------------------------"
echo "CREATING WAR FILE..."
echo "----------------------------------------------------------------"
mvn clean package
