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

- NEVER have two clients running concurrently.
    The images and the metadata database will survive - however the internal spatial database (RTree) will get destroyed.

// ---------------------------------------------------------------------------------------------------------------------
CLI USAGE:
// ---------------------------------------------------------------------------------------------------------------------

1. Create an executable JAR:

        mvn clean compile package

2. Run the java code directly:

        java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar  [+OPTIONS]


// ---------------------------------------------------------------------------------------------------------------------
EXAMPLE WORKFLOW: Running queries on the data:
// ---------------------------------------------------------------------------------------------------------------------

        1) UPLOAD YOUR DATA

            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -r=2 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip2 bsv
            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -r=3 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip3 bsv
            java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -r=4 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip4 bsv

            - Uploads (i.e. copies) the data from the specified directory into the bucket
            - Make sure to keep track of and specify route numbers. If none are specified 0 will be used.

        OPTION 1:

            2.1A) RUN A MYSQL QUERY

                mysql -N -u java-db-client -p  < script.sql > out.txt

                Password:
                v1M4^qVAU!3084NF

                - Runs script.sql (See contents for an example query)
                - It's a nested script
                - First selects * from the table
                - Then selects just the list of IDs for the client
                - This list of IDs is saved in out.txt

            2.1B) DOWNLOAD RESULTS SELECTED BY THE QUERY

                java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve -s bsv @out.txt

                - out.txt must contain a list of image IDs and nothing else
                - This command uses the '@' sign - it means that contents of the out.txt file are attached to the end of the command


            ALTERNATIVE: RUN BOTH IN ONE COMMAND

                ./src/main/scripts/sql.sh

                Password:
                v1M4^qVAU!3084NF


                (might need to run first: chmod 755 src/main/scripts/sql.sh)

        OPTION 2:

            2.2) RUN A GEOGRAPHIC QUERY DIRECTLY ON THE CLIENT

                java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve --geo=20 --latitude=51.45722 --longitude=-2.6009 --maxGeoResults=40 bsv



        3) EXTRACT PROJECTIONS USING THE PYTHON SCRIPT

            python src/main/python/equirectangular-toolbox/nfov.py /home/chris/bsv/output 0.45 800

            - First argument is the directory to process - REPLACE WITH YOUR USERNAME!
            - Second is the FOV, recommended value is 0.45
            - Third is the height of the image (Width = 2 * height)



        4) BACKUP THE DATABASE AND THE RTREE

            cp /home/chris/bsv/rtree.tree rtree_backup.tree
            mysqldump -N -u root -p --databases bristol_streetview_schema > backup.sql

            Password:
            3Mc!^0aylO03L2!p


        5) IF NEEDED, RESTORE THE BACKUPS

            cp rtree_backup.tree /home/chris/bsv/rtree.tree
            mysql -N -u root -p  < backup.sql

            Password:
            3Mc!^0aylO03L2!p

            [INSERT RTREE RESTORE COMMAND]


todo
Create upload, sql and geoSearch scripts that include mysql and rtree backups


-- dump the file after each client exit
-- read in the dump on every client run

-- instructions for how to run the android app / the hardware


