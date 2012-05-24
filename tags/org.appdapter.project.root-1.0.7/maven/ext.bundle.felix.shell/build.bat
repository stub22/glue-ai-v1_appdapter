
call mvn clean
call mvn package
call mvn -Prun-on-felix antrun:run