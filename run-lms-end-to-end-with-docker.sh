mvn -f service-discovery clean install package -DskipTests
mvn -f api-gateway clean install package -DskipTests
mvn -f signup-service clean install package -DskipTests

mvn -f login-service clean install package -DskipTests
mvn -f book-service clean install package -DskipTests
mvn -f notification-service clean install package -DskipTests

docker-compose -f docker-compose-lms-end-to-end.yaml up --build

echo "sleeping for 10 secs."
sleep 10

