
echo "starting mysql and Kafka before we build and start micro services."

docker-compose -f docker-compose-mysql-kafka-only.yaml up -d

echo "Now packaging each microservice"

echo "Now packaging each service discovery"
mvn -f service-discovery clean install package -DskipTests

echo "Now packaging each api gateway"
mvn -f api-gateway clean install package -DskipTests

echo "Now packaging each signup service"
mvn -f signup-service clean install package -DskipTests

echo "Now packaging each login service"
mvn -f login-service clean install package -DskipTests

echo "Now packaging each book service"
mvn -f book-service clean install package -DskipTests

echo "Now packaging each notification service"
mvn -f notification-service clean install package -DskipTests


echo "Now running all jars 1 by 1"

java -server -jar service-discovery/target/service-discovery-*.jar &

echo "sleeping for 10 secs."
sleep 10

java -server -jar api-gateway/target/api-gateway-*.jar &


echo "sleeping for 10 secs."
sleep 10 

java -server -jar signup-service/target/signup-service-*.jar &

echo "sleeping for 10 secs."
sleep 10

java -server -jar login-service/target/login-service-*.jar & 

echo "sleeping for 10 secs."
sleep 10

java -server -jar book-service/target/book-service-*.jar &


echo "sleeping for 10 secs."
sleep 10

java -server -jar notification-service/target/notification-service-*.jar &



echo "sleeping for 10 more secs to ensure all services are registered with eureka."
sleep 20
echo "Please go ahed and hit end points!"
