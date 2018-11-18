# BRISTOL STREET VIEW DESKTOP CLIENT 1.0

This is the second part of work that I did for my Summer 2018 Internship at the University of Bristol's Visual Information Lab. The main aim of the project was to create a platform to collect and store 1000s of 360 degree images of Bristol.

[READ THE PROJECT SUMMARY!](https://bitbucket.org/kg17815/bristol-street-view-app/src/master/Bristol%20Street%20View%20PROJECT%20SUMMARY.pdf)

The repository contains a Java desktop client. It process photos taken during a trip, uploads them to a database and allows for them to be easily searched. Searching can be done using a standard SQL query or by latitude and longitude - both of which are explained in more detail below.
 
For the first part of the project, have a look at the" Bristol Street View Android App" repo.


## CONTENTS:
1. PROJECT SETUP
2. COMPILING AND USAGE
3. RESTORING EXISTING DATABASES
4. UPLOADING YOUR DATA
5. RUNNING SQL QUERIES
6. RUNNING GEOGRAPHIC QUERIES
7. BACKING UP THE DATABASES

## TERMINOLOGY:

`BUCKET` - Directory where photos will be stored. The bucket will be stored in your home folder:
            `$HOME/[BUCKET NAME]`

`RTREE` - Database for multi-dimensional (here geographical) data


### PROJECT SETUP:

1. Make sure trip 2 photos are copied to: db-client/src/test/resources/trip2

Tests will fail without these photos

(Alternatively uncomment @Ignore annotation on lines 111, 229, 290 of BucketHandlerTest)


2. Install mysql 5.7 (e.g. using Ubuntu APT)

3. Execute:

        sudo mysql -u root < init.sql

4. Verify the installation by running all client tests:

        mvn -Dtest=ClientTests test


#### NOTES:

* If you see this message: "getPhotoTest(client.BucketHandlerTest): Incorrect number of images in the database expected:<79> but was:<78>" simply re-run the tests.

* Tests might fail if you already uploaded any own data.

* Tests manipulate buckets and the databases.

* Therefore DO NOT RUN TESTS WHEN YOU ALREADY HAVE UPLOADED OWN DATA!

* NEVER have two clients running concurrently.
    *The images and the metadata database will survive - however the internal spatial database (RTree) will get destroyed.


### COMPILING AND USAGE:

1. Create an executable JAR:

`./src/main/scripts/build.sh [INSTALL DIRECTORY]`

RECOMMENDED:

`./src/main/scripts/build.sh ~/client`

2. Run the java code directly:

        cd ~/client
        java -jar client.jar  [+OPTIONS]

        For help and available commands:
        java -jar client.jar -h


### RESTORING EXISTING DATABASES

        ./restore.sql [BUCKET]

* This script will look for the files:
    * rtree_backup.tree
    * backup.sql
    
in: `[INSTALL DIRECTORY]/[BUCKET]`

This operation deletes any data there might already be in the databases

### UPLOADING YOUR DATA

        ./upload.sh [BUCKET] [ROUTE NUMBER]

* The script attempts to find connected MTP devices (i.e. Android phones) and looks for the Ricoh directory
* If found, the contents are uploaded to the DB
* An automatic backup is created before upload is started
* If the upload fails, or the client is forced to close, make sure you restore the backup
* Don't forget to delete the files from the phone (only after a successful upload!)

To upload the test trips:

            java -jar client.jar -b=bsv -vepf=$HOME/client/log.txt -r=2 -gu=[PATH TO RESOURCES]/trip2
            java -jar client.jar -b=bsv -vepf=$HOME/client/log.txt -r=3 -gu=[PATH TO RESOURCES]/trip3
            java -jar client.jar -b=bsv -vepf=$HOME/client/log.txt -r=4 -gu=[PATH TO RESOURCES]/trip4

### RUNNING SQL QUERIES

`./sql.sh [BUCKET]`

* This script uses the '@' sign - it means that contents of the out.txt file are attached to the end of the command
* It runs script.sql (See its content for an example query)
* It's a nested script in the form:

`SELECT id FROM (   [YOUR ACTUAL QUERY...]   ) a;`



### RUNNING GEOGRAPHIC QUERIES

1) Run a geographic query directly on the client

        `java -jar client.jar -b=bsv --vepf=$HOME/client/log.txt --geo=20 --latitude=51.45722 --longitude=-2.6009 --maxGeoResults=40`
        
2) Extract projections

        `python nfov.py $HOME/bsv/output 0.45 800`

* First argument is the directory to process - REPLACE WITH YOUR BUCKET NAME
* Second is the FOV, recommended value is 0.45
* Third is the height of the image (width = 2 * height)

### BACKING UP THE DATABASES
        
        ./backup.sh [BUCKET]

This script creates the files:

* rtree_backup.tree
* backup.sql

in: `INSTALL DIRECTORY/BUCKET`





