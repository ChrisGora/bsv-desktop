PROJECT SETUP:

1. Install mysql 5.7 (e.g. using Ubuntu APT)

2. Execute:

        sudo mysql -u root < script.sql

3. Verify the installation by running all client tests:

        mvn -Dtest=ClientTests test

4. Run the GUI client using:

        mvn exec:java -D"exec.mainClass"="client.MainGUI"


NOTES:

- NEVER have two clients / two instances of the Bucket Handler running concurrently on the same bucket.
    The images and the metadata database will survive - however the internal spatial database (RTree) will get destroyed.



RUNNING:

1. Create an executable JAR:

        mvn clean compile package

2a. Run the java code directly:

        java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar  [+OPTIONS]

2b. Run a mysql query and feed it into the java code:

        mysql -N -u java-db-client -p  < test.sql > out.txt
        v1M4^qVAU!3084NF

        java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar [+OPTIONS] @out.txt

        EG: java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ves @out.txt
