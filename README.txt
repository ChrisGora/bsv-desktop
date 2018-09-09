PROJECT SETUP:

1. Install mysql 5.7 (e.g. using Ubuntu APT)

2. Execute:

        sudo mysql -u root < script.sql

3. Verify the installation by running all client tests:

        mvn -Dtest=ClientTests test

4. Run the GUI client using:

        mvn exec:java -D"exec.mainClass"="client.MainGUI"


