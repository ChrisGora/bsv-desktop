PROJECT SETUP:

1. Install mysql 5.7 (e.g. using Ubuntu APT)

2. Execute:

        sudo mysql -u root < init.sql

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


        EXAMPLE:

        1) UPLOAD

        java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -evr=23 -u=/home/chris/Desktop/repos/db-client/trips/trip3 bristol-streetview-photos


        2) RUN A MYSQL QUERY

        mysql -N -u java-db-client -p  < script.sql > out.txt
        v1M4^qVAU!3084NF


        3) DOWNLOAD RESULTS SELECTED BY THE QUERY

        java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -evs bristol-streetview-photos @out.txt
