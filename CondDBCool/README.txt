# Use the following commands to test services
# 1: try access to administratif services: should fail without authentication
curl -i -X POST http://localhost:8080/physconddbcool/admin/loadtags
# 2: try access to standard unprotected services : should work
curl -i -X GET http://localhost:8080/physconddbcool/cool/tracetags
# 3: try to authenticate with WRONG parameters : should fail
curl -i -X POST --user user:user http://localhost:8080/physconddbcool/admin/loadtags
# 3: try to authenticate with CORRECT parameters : should work
curl -i -X POST --user user:userPass http://localhost:8080/physconddbcool/admin/loadtags

