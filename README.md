
# Capturing remote/ui acceptance test code coverage results using JaCoCo

| ​ | ​ |
| --------- | -------------------------------------------------------
| **NOTE**  | this is an example for a NOTE box with invisible header chars


## Overview

Jacoco comes with out-of-the-box implementations for capturing test coverage information from a remote process over TCP by ['output=tcpserver|tcpclient' agent configuration](http://www.eclemma.org/jacoco/trunk/doc/agent.html), but in modern Cloud-deployment environments (e.g. Cloudfoundry) it is not always possible or desirable to know the concrete TCP addresses in advance or connect directly to a port other than HTTP/HTTPS.

This sample explores how to capture the test coverage information from a remote process using [Jacoco](http://www.eclemma.org/jacoco/index.html) and [SonarQube](http://www.sonarqube.org/) over HTTP. This is particularly useful for HTTP-based applications.

## Prerequisites

The below describes an example setup of Sonar using MySQL as db backend (see also http://chapter31.com/2013/05/02/installing-sonar-source-on-mac-osx/) and has been
tested with MySQL 5.6, SonarQube 4.3.1 and Maven 3.0.5

1. MySQL for SonarQube (e.g.)

    OSX: $>brew install mysql  
    
    create the sonar database using e.g. [create_database.sql](https://github.com/SonarSource/sonar-examples/blob/master/scripts/database/mysql/create_database.sql)
    
    ```
# File: create_database.sql
# Create SonarQube database and user.
# Command: mysql -u root -p < create_database.sql
#
CREATE DATABASE sonar CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE USER 'sonar' IDENTIFIED BY 'sonar';
GRANT ALL ON sonar.* TO 'sonar'@'%' IDENTIFIED BY 'sonar';
GRANT ALL ON sonar.* TO 'sonar'@'localhost' IDENTIFIED BY 'sonar';
FLUSH PRIVILEGES;
    ```

2. Download and run [SonarQube](http://www.sonarsource.org/downloads/) 4.3 or higher

    OSX: 
    
    a) $>brew install sonar

    b) with (at least) version 4.3.1 there may be a conflict with the currently installed version of ruby on your system (see https://jira.codehaus.org/browse/SONAR-3579)
    in order to fix that, open /usr/local/Cellar/sonar/4.3.1/libexec/web/WEB-INF/config/environment.rb and after the line
  
    ```
ENV['GEM_HOME'] = $servlet_context.getRealPath('/WEB-INF/gems')
    ```

    add  

    ```
ENV['GEM_HOME'] = $servlet_context.getRealPath('/WEB-INF/gems')
# avoid ruby version conflicts
ENV['GEM_PATH']=''
    ```

    c) ensure you have the Sonar Java plugin installed
     
        open http://localhost:9000, login as admin/admin and go to Settings->System->Update Center to install the Java plugin
        
3. Maven 3.0.5

  OSX: $>brew install maven30

## Running

from the toplevel folder of the project run

```
$>mvn clean install 
$>mvn sonar:sonar
```

Now open the project dashboard in Sonar. You should see the IT coverage at 64.3%. Drill down and you should see that both methods (home(), edit()) in HomeController are covered by integration tests. 


## Basic steps to configure your project for Sonar unit- & integration test coverage

1. Add Sonar properties to your root POM  

    see $/pom.xml:35ff, sections (1), (2) and (3)

    Note in particular the setting of an __absolute__ file path for _sonar.jacoco.itReportPath_. Integration test coverage reporting only works properly when all module reports are merged into the same file before sending to Sonar for analysis.

        <properties>
          <!-- (1) Sonar connection properties - typically specified on commandline -->
          <sonar.host.url>http://localhost:9000</sonar.host.url>
          <sonar.jdbc.url>jdbc:mysql://localhost:3306/sonar?useUnicode=true&amp;characterEncoding=utf8</sonar.jdbc.url>
          <sonar.jdbc.username>sonar</sonar.jdbc.username>
          <sonar.jdbc.password>sonar</sonar.jdbc.password>
          <!-- end (1) -->
          
          <!-- (2) Help Sonar to be sure about language and version -->
          <sonar.language>java</sonar.language>
          <sonar.java.source>1.7</sonar.java.source>
          <!-- end (2) -->
  
          <!-- (3) mandatory Sonar properties for capturing raw coverage report output -->
          <sonar.dynamicAnalysis>reuseReports</sonar.dynamicAnalysis> <!-- don't run tests if report file already exists -->
  
          <!-- where sonar will find the junit reports -->
          <sonar.junit.reportsPath>target/surefire-reports</sonar.junit.reportsPath>
          <sonar.junit.itReportsPath>target/failsafe-reports</sonar.junit.itReportsPath>
  
          <!-- write unit test coverage info to each module's target folder -->
          <sonar.jacoco.reportPath>target/jacoco-ut.exec</sonar.jacoco.reportPath>
          <!-- append integration test coverage from each module to the same(!) file -->
          <sonar.jacoco.itReportPath>${project.root.basedir}/target/jacoco-it.exec</sonar.jacoco.itReportPath>
          <!-- end (3) -->
          ...
        </properties>  

2. Add the __jacoco-maven-plugin__ to your root POM

    see $/pom.xml:84, section (4)
    
    This plugin will define a maven property _'jacoco.agent.argLine'_ containing the necessary JVM jacoco agent commandline 
    
        <!-- (4) configure jacoco-maven-plugin -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>${jacoco.version}</version>
            <configuration>
                <append>true</append>
                <propertyName>jacoco.agent.argLine</propertyName>
            </configuration>
            <executions>
                <execution>
                    <id>jacoco-initialize</id>
                    <phase>initialize</phase>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                    <configuration>
                        <destFile>${sonar.jacoco.reportPath}</destFile>
                    </configuration>
                </execution>
                <execution>
                    <id>jacoco-integration-initialize</id>
                    <phase>pre-integration-test</phase>
                    <goals>
                        <goal>prepare-agent-integration</goal>
                    </goals>
                    <configuration>
                        <destFile>${sonar.jacoco.itReportPath}</destFile>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    
    During the "initialise" phase this will define a property
    
        jacoco.agent.argLine=-javaagent=<path/to/jacoco-agent.jar>,destFile=${sonar.jacoco.reportPath}
    
    During the "pre-integration-test" phase it will redefine that property to 
    
        jacoco.agent.argLine=-javaagent=<path/to/jacoco-agent.jar>,destFile=${sonar.jacoco.itReportPath}
    
    
3. Surefire must launch the forked test jvm with the jvm argument created by jacoco-maven-plugin

    see $/pom.xml:211 section (5)
 
    surefire must create junit xml reports so that sonar can find it during analysis (5.a). The jvm must be launched with the jvm javaagent argument created by the jacoco-maven-plugin (5.b). 
    
    Note: optionally if you want per-test coverage you must add Jacoco's _JUnitListener_ to your surefire configuration (5.c). In this case don't forget to add the necessary test dependencies to your project (see $/pom.xml:68, section 6). 
       
        <!-- (5) configure surefire to launch forked jvm with jacoco-agent -->
        <plugin>
              <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.16</version>
            <configuration>
                <testFailureIgnore>false</testFailureIgnore>
                <useFile>false</useFile>
                <!-- (5.a) surefire must create junit report where sonar can find it later during analysis --> 
                <reportsDirectory>${project.basedir}/${sonar.junit.reportsPath}</reportsDirectory>
                <!-- (5.b) add jacoco-agent jvm argument to forked test jvm startup command -->
                <argLine>${jacoco.agent.argLine}</argLine>
                <properties>
                    <!-- (5.c) configure Jacoco's JUnitListener to capture per-test coverage -->                
                    <property>
                        <name>listener</name>
                        <value>org.sonar.java.jacoco.JUnitListener</value>
                    </property>
                </properties>
            </configuration>
        </plugin>

4. Integration Test surefire config
        
    see $/pom.xml:211 section (5)
        
        <!-- (5) configure surefire to launch forked jvm with jacoco-agent -->
        <plugin>
              <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.16</version>
            <configuration>
                <testFailureIgnore>false</testFailureIgnore>
                <useFile>false</useFile>
                <!-- (5.a) surefire must create junit report where sonar can find it later during analysis --> 
                <reportsDirectory>${project.basedir}/${sonar.junit.reportsPath}</reportsDirectory>
                <!-- (5.b) add jacoco-agent jvm argument to forked test jvm startup command -->
                <argLine>${jacoco.agent.argLine}</argLine>
                <!-- (5.c) add jacoco-agent jvm argument to forked test jvm startup command -->
                <properties>
                    <property>
                        <name>listener</name>
                        <value>org.sonar.java.jacoco.JUnitListener</value>
                    </property>
                </properties>
            </configuration>
            <executions>
                <!-- (5.d) configure surefire to run all test classes matching "*IT" as integration tests -->
                <execution>
                    <id>integration-test</id>
                    <phase>integration-test</phase>
                    <goals>
                        <goal>test</goal>
                    </goals>
                    <configuration>
                        <reportsDirectory>${project.basedir}/${sonar.junit.itReportsPath}</reportsDirectory>
                        <argLine>${jacoco.agent.argLine}</argLine>
                        <includes>
                            <include>**/*IT.class</include>
                        </includes>
                    </configuration>
                </execution>
            </executions>
        </plugin>
  
## Steps to configure your project for Sonar remote application integration test coverage

In order to capture coverage information from a remote process, after each test we fetch the captured coverage data and write it to the local disk. In order to fetch coverage data from the remote process, the following must be configured:

1. Add the Jacoco javaagent with output=none mode to the JVM's startup arguments

        java -cp ... -javaagent:/path/to/org.jacoco.agent-0.7.1.201405082137-runtime.jar=output=none
    
For tomcat, you typically set this via CATALINA_OPTS.

2. Add the sonar-jacoco-remotelistener dependency to your war module and configure the proxy servletfilter in your web.xml

        <!-- war.pom -->
        <dependency>
            <groupId>sample</groupId>
            <artifactId>sonar-jacoco-remotelistener</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>


        <!-- web.xml -->
        <filter>
            <filter-name>JacocoAgentProxyServletFilter</filter-name>
            <filter-class>jacoco.JacocoAgentProxyServletFilter</filter-class>
        </filter>
        <filter-mapping>
            <filter-name>JacocoAgentProxyServletFilter</filter-name>
            <url-pattern>/jacoco/*</url-pattern>
            <dispatcher>REQUEST</dispatcher>
        </filter-mapping>

3. Configure Surefire or Failsafe to use JUnitHttpListener as RunListener for your tests

    see $/pom.xml:211ff section (6)

    You must configure JUnitHttpListener's _targeturl_ and _destfile_ properties as system properties so that it knows where to fetch the coverage data from and which file to append to.
     
    
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            ...
            <executions>
                ...
                <!-- (6) configure surefire for running remote (e.g. Browser or REST) acceptance tests  -->
                <execution>
                    <id>acceptance-test</id>
                    <phase>integration-test</phase>
                    <goals>
                        <goal>test</goal>
                    </goals>
                    <configuration>
                        <!-- (6.a) make sure, surefire and sonar use the same location for junit integration test reports -->
                        <reportsDirectory>${project.basedir}/${sonar.junit.itReportsPath}</reportsDirectory>
                        <!-- (6.b) DON'T use jacoco agent for (remote) acceptance tests JVM 
                                  we fetch the data from the remote JVM instead) 
                        -->
                        <argLine/>
                        <!-- (6.c) run all test classes matching the name pattern *AT -->
                        <includes>
                            <include>**/*AT.class</include>
                        </includes>
                        <!-- (6.d) configure JUnitHttpListener to fetch coverage data after each test from {targeturl} 
                                   and write it to {destfile} 
                        -->
                        <properties>
                            <property>
                                <name>listener</name>
                                <value>jacoco.JUnitHttpListener</value>
                            </property>
                        </properties>
                        <systemPropertyVariables>
                            <targeturl>http://localhost:8080/jacoco/</targeturl>
                            <destfile>${sonar.jacoco.itReportPath}</destfile>
                        </systemPropertyVariables>
                    </configuration>
                </execution>
            </executions>
        </plugin>


## Important Notes

For integration test coverage, you __must__ configure an __absolute__ file path for jacoco-it.exec coverage record and have all integration test modules append their reports to the __same__ file.

In maven multi-module projects this is not trivial. You can specify the absolute path by e.g. defining a property

```xml
<properties>
  <sonar.jacoco.itReportPath>${project.root.basedir}/target/jacoco-it.exec</sonar.jacoco.itReportPath>
  ...
</properties>  
```
    
and use that property to configure the jacoco-maven-plugin and surefire/failsafe  
    

## TODOs

*) maybe no need to dump() after every request? IT don't support per-test coverage anyway

## Useful Links

http://docs.codehaus.org/display/SONAR/Analysis+Parameters
http://www.eclemma.org/jacoco/trunk/doc/examples/java/ExecutionDataClient.java
http://www.eclemma.org/jacoco/trunk/doc/examples/java/ReportGenerator.java
https://groups.google.com/forum/#!topic/jacoco/04MOA-C22SM
http://docs.codehaus.org/display/SONAR/Code+Coverage+by+Unit+Tests+for+Java+Project
http://dougonjava.blogspot.co.il/2013/07/integration-testing-using-maven-tomcat.html
https://github.com/SonarSource/sonar-examples
http://itestfirst.wordpress.com/2013/04/25/sonar-unit-and-integration-test-coverage-with-maven/
