export MYSQL_SERVICE=true
export BUILD_ENVIRONMENT=Travis
mvn -Prelease release:perform -Djdbdt.site.path=$JDBDT_SITE_PATH
