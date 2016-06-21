Library containing file access and sql benchmarks

Compile
-------

    mvn package
	
It will generate two jars:

* support-tools-2.0-SNAPSHOT.jar Contains just the project class files
* support-tools.jar Contains also the dependencies


Usage
-----

To run the disk access benchmark:

    java -Djava.io.tmpdir=/directory/to/test -jar support-tools.jar
	

To run the SQL benchmark:

    java -cp support-tools.jar \
	    com.atlassian.util.benchmark.JIRASQLPerformance
    jira_home jira_install_dir [numberOfRuns]

or

    java -cp support-tools.jar:com.atlassian.util.benchmark.JIRASQLPerformance:/path/to/your/jdbc-driver.jar \
	    com.atlassian.util.benchmark.JIRASQLPerformance
    user passwrod driverclass jdbc-url driver-classname [numberOfRuns]

To run DB status:

    java -cp support-tools.jar \
	    com.atlassian.util.status.JIRADatabaseStatus
		jira_home jira_install_dir

or:

    java -cp support-tools.jar \
	    com.atlassian.util.status.JIRADatabaseStatus
		user passwrod driverclass jdbc-url driver-classname