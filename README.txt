BSV DB CLIENT 1.0

// ---------------------------------------------------------------------------------------------------------------------
PROJECT SETUP:
// ---------------------------------------------------------------------------------------------------------------------

1. Install mysql 5.7 (e.g. using Ubuntu APT)

2. Execute:

        sudo mysql -u root < init.sql

3. Verify the installation by running all client tests:

        mvn -Dtest=ClientTests test

NOTES:

- NEVER have two clients / two instances of the Bucket Handler running concurrently on the same bucket.
    The images and the metadata database will survive - however the internal spatial database (RTree) will get destroyed.

// ---------------------------------------------------------------------------------------------------------------------
GUI USAGE:
// ---------------------------------------------------------------------------------------------------------------------

1. Run the GUI client using. GUI offers limited upload and no download functionality.

        mvn exec:java -D"exec.mainClass"="client.MainGUI"

// ---------------------------------------------------------------------------------------------------------------------
CLI USAGE:  (RECOMMENDED)
// ---------------------------------------------------------------------------------------------------------------------

1. Create an executable JAR:

        mvn clean compile package

2. Run the java code directly:

        java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar  [+OPTIONS]


// ---------------------------------------------------------------------------------------------------------------------
EXAMPLE: Running queries on the data:
// ---------------------------------------------------------------------------------------------------------------------

        1) UPLOAD YOUR DATA

            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve -r=2 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip2 bsv
            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve -r=3 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip3 bsv


        OPTION A:

        A1) RUN A MYSQL QUERY

            mysql -N -u java-db-client -p  < script.sql > out.txt
            Password:   v1M4^qVAU!3084NF


        A2) DOWNLOAD RESULTS SELECTED BY THE QUERY

            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve -s bsv @out.txt


        OPTION B:

        B1) RUN A GEOGRAPHIC QUERY DIRECTLY ON THE CLIENT

            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve --geo=20 --latitude=51.45723 --longitude=-2.60092 --maxGeoResults=40 bsv









