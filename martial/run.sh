mvn clean install -DskipTests=true
cd martial-math
mvn clean -DskipTests=true  assembly:assembly
cd -
cd martial-hadoop
mvn clean -DskipTests=true  assembly:assembly

