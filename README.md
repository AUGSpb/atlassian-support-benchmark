Library containing file access and sql benchmarks

Compile
-------

    mvn package
	
It will generate two jars:

* support-tools-1.0-SNAPSHOT.jar Contains just the project class files
* support-tools.jar Contains also the dependencies


Usage
-----

To run the disk access benchmark: 

    java -jar support-tools.jar -Djava.io.tmpdir=/directory/to/test
	

To run the SQL tests:

    java -cp support-tools.jar com.atlassian.util.benchmark.JIRASQLPerformance:/path/to/your/jdbc-driver.jar \
	    user passwrod driverclass jdbc-url driver-classname
