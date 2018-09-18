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

AUTOMATIC:
        ./src/main/scripts/build.sh ~/client

MANUAL:
        [NAVIGATE TO THE MAIN CLIENT FOLDER]
        mvn clean compile package
        mkdir $HOME/client
        cp target/client-1.0-SNAPSHOT-jar-with-dependencies.jar $HOME/client/client.jar

2. Run the java code directly:

        java -jar ~/client/client.jar  [+OPTIONS]


// ---------------------------------------------------------------------------------------------------------------------
EXAMPLE WORKFLOW: Running queries on the data:
// ---------------------------------------------------------------------------------------------------------------------

        1) UPLOAD YOUR DATA

        AUTOMATIC:
            ./src/main/scripts/upload.sh

        MANUAL:
            cd /run/user/$UID/gvfs/mtp*/
            cd Internal\ storage/Ricoh/
            pwd
            java -jar $HOME/client/client.jar -b=bsv -r=1 -gu="[PASTE THE RESULT OF PWD HERE]"

            (e.g. java -jar $HOME/client/client.jar -b=bsv -r=1 -gu="/run/user/1000/gvfs/mtp:host=%5Busb%3A001%2C006%5D/Internal storage/Ricoh")


            java -jar $HOME/client/client.jar -b=bsv -vepf=$HOME/client/log.txt -r=2 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip2
            java -jar $HOME/client/client.jar -b=bsv -vepf=$HOME/client/log.txt -r=3 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip3
            java -jar $HOME/client/client.jar -b=bsv -vepf=$HOME/client/log.txt -r=4 -gu=/home/chris/Desktop/repos/db-client/src/test/resources/trip4

            - Uploads (i.e. copies) the data from the specified directory into the bucket
            - Make sure to keep track of route numbers. If none are specified 0 will be used.

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

                java -jar $HOME/client/client.jar -b=bsv --vepf=$HOME/client/log.txt -s @out.txt

                - out.txt must contain a list of image IDs and nothing else
                - This command uses the '@' sign - it means that contents of the out.txt file are attached to the end of the command


            ALTERNATIVE: RUN BOTH IN ONE COMMAND

                ./src/main/scripts/sql.sh

                Password:
                v1M4^qVAU!3084NF


        OPTION 2:

            2.2) RUN A GEOGRAPHIC QUERY DIRECTLY ON THE CLIENT

                java -jar $HOME/client/client.jar -b=bsv --vepf=$HOME/client/log.txt --geo=20 --latitude=51.45722 --longitude=-2.6009 --maxGeoResults=40



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

https://theta360.com/uk/support/manual/v/content/prepare/prepare_06.html


