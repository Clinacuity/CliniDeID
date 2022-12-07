On-premises CliniDeID installation instructions for Windows 8.1, 10, and Server 2012 R2.
Version VERSION_NUMBER, October 21, 2021

OpenJDK® 17 will be setup as part of this process.
See https://jdk.java.net/17 for more information on OpenJDK
An embedded version of Python 3.8® is included 

0) Expand (unzip) CliniDeID-Windows.zip by right clicking on it and choosing expand all. If you double clicked on the zip file and opened it, then you will need to close that window, right click on the zip file and choose expand.
1) Start a command prompt. This can be done by clicking "Window" and typing "command", "Command Prompt" should be in the list. If you get an error that states a command must be run as an administrator, then repeat this but right click on "Command Prompt" and select Run as administrator.
2) Navigate to where CliniDeID was expanded to, something like 'cd c:\users\yourUser\Desktop\CliniDeID-Windows'
3) Type 'setupCliniDeID.bat' and hit enter. This will unzip Java, specifically OpenJDK 17 and an embedded python
4) Check total memory in Windows. Physical RAM plus virtual memory must be at least 28GB. To check/change this:
	a) Go to control panel and select system (you may need to change "View by" to "Small icons" to see it)
	b) Click on Advanced system settings
	c) In the new window, select Advanced tab
	d) In the Performance section, click Settings...
	e) In the new window, go to the Advanced tab, in the Virtual memory section, click Change...
	f) In the new window, 
		uncheck "Automatically manage paging file size for all drives"
		Select C: drive (main drive)
		Select Custom size, 28000 (larger is OK) for both initial and maximum
		Click Set button
		Clear the windows by clicking OK, OK, OK. The machine will need to restart.

After installing, to run CliniDeID, simply double click on the runCliniDeID.vbs file.
Documentation for using CliniDeID is available within the program or from data\Documentation.pdf

Note that during execution CliniDeID connects to the license server through port 443 (standard for https).
You must enter your license key the first time CliniDeID is run. It will be required stored in the file LICENSE.KEY. If that file is removed, then the key must be entered in the next time CliniDeID is run.

Command Line Interface

CliniDeID can also be run from the command line. After completing the above installation instructions, the command line version is started by typing:
runCliniDeIDcommandLine.bat  

The command line version supports the following options:
 -cda,--cda                                Use HL7 CDA XML format for input and output
 -dc,--dbSchema <arg>                      PreQuery statement for input database
 -di,--dbColumnId <arg>                    Column name for ID for input database
 -dm,--dbms <arg>                          Type of input database, one of: [postgresql, sqlserver,
                                           mysql, db2, ms sql server]
 -dn,--dbName <arg>                        Database name for input
 -dp,--dbPassword <arg>                    Password for input database
 -dq,--dbQuery <arg>                       Where clause for input database select statement
 -ds,--dbServer <arg>                      Server for input database
 -dt,--dbTableName <arg>                   Name of table with notes for input database
 -du,--dbUsername <arg>                    Username for input database
 -dx,--dbColumnText <arg>                  Column name for text notes for input database
 -h,--help                                 Prints this message.
 -id,--inputDir <arg>                      Input directory from which to read the files.
 -idb,--inputDatabase                      Input source is a database.
 -if,--inputFile                           Input source is directory of text files.
 -l,--level <arg>                          Level of deidentification [beyond, strict, limited,
                                           custom], default is strict.
 -od,--outputDir <arg>                     Output directory for processed file list, annotations,
                                           and notes (if outputFile chosen).
 -odb,--outputDatabase                     If present, outputs written to CliniDeID database.
 -of,--outputFile                          If present, outputs written to files in output directory.
 -pii,--piiOptions <arg>                   Individual options for pii as PiiSubtype-true|false pairs
                                           in comma separated list. See below for details. If level
                                           or loadPiiConfiguration is used then they are done first
                                           with these changes afterwards.
 -piiConfig,--loadPiiConfiguration <arg>   Filename of pii configuration file to use. Will replace
                                           any level value given
 -r,--rnnPort <arg>                        Port number for RNN to use. Defaults to finding open
                                           port.
 -t,--outputTypes <arg>                    resynthesis, generaltag, categorytag, detectedPII,
                                           complete, filtered, map, or all.  Combine with ',' (no
                                           spaces) as in 'resynthesis,filtered,generaltag'. Default
                                           is resynthesis.
 -txt,--text                               Use plain text (default) format for input and output
 -x,--exclude <arg>                        Annotators to exclude, one or more of RNN MIRA CRF SVM,
                                           combine with ',' as in 'RNN,MIRA'. Default is not to
                                           exclude any.

When database input (--inputDatabase) is chosen, the query used to obtain data is 'SELECT dbColumnId, dbColumnText from dbTableName' (using values from the command line options). The PreQuery statement option (--dbSchema) is executed before the select query on the input database. It is used for things like setting the schema, view, or any USE commands needed for the select to work. The Where clause (--dbQuery) is appended to the end of the select query and is used to filter results. 

HL7 CDA XML requires .xml files that are valid HL7 CDA v1 documents. PII from structured elements within the xml (such as birthTime, names, addresses, and IDs) are processed as well as the contents of <text> and <title> tags. After resynthesis or PII tagging, a new xml file will be generated with the same format and tags of the original but with the PII transformed according to the outputTypes choices.

PII (Personally Identifiable Information) Configuration

-piiConfig or --loadPiiConfiguration can be used to load a previously created xml configuration file. The graphical CliniDeID allows for creation of these files.

-pii or --piiOptions
This can be used to control individual PII categories. They are adjustments to a base configuration from either a level (like strict) or a xml configuration file. Most categories are either included or not. For example,
	--piiOptions Patient-true,Profession-false
Would indicate that the system should identify Patient names and not Professions. Each item is separated by a comma and each item is followed by a dash and then either true or false.
  Three categories are special in this regard: Date, Zip, and Age. For these, the allowed values are false 0 1 or 2. 0 is the same as false. 
  For Date: '2' means that year values are not identified, just months and days (HIPAA Safe Harbor). '1' identifies all parts.
  For Zip: '2' means that only the last 2 digits of a 5 digit zip code will be changed unless the total population of the regions with those first 3 digits is less than 20,000. '1' processes the whole zip code.
  For Age: '2' means that only ages greater than 89 are identified (HIPAA Safe Harbor), '1' identifies all ages.
Example:
    --piiOptions Patient-false,Date-2,Zip-1,Age-false
Would not identify Patient names, would identify month and day (but not year) parts of a Date, would identify all zip code, and would not identify any ages.

Examples:
runCliniDeIDcommandLine.bat --inputFile --inputDir c:\testSet1 --outputFile --outputDir c:\outputSet1 --level beyond --outputTypes complete,resyntheses
 	would read files from c:\testSet1, output would be placed in c:\outputSet1, beyond safe harbor level of de-identification, output would be resynthesized text and complete xml files (for auditing)

runCliniDeIDcommandLine.bat --inputDatabase --dbName DatabaseOfNotes --dbms sqlserver --dbTableName ClinicalText --dbColumnId patientId --dbColumnText notes --outputDatabase --outputDir c:\outputSet1 --level strict --outputTypes detectedPII,resyntheses,filtered
 	would read files the MS Sql Server database DatabaseOfNotes, the table ClinicalText whose id column is patientId and the text column (with text to process) named notes, strict HIPAA safe harbor level of de-identification, output list of found PII in the database, resynthesized text sent to the database, and filtered xml files (for evaluation) placed in c:\outputSet1,
 	

Output Database

CliniDeID can store the outputs of resynthesis, tagging, and PII annotations to a PostgreSQL database. To install this database:
 1) Download the appropriate version of PostgreSQL from https://www.postgresql.org/download and install it
 2) Start the database server according to the instructions for your operating system (https://www.postgresql.org/docs)
 3) Create the database clinideid:
	createdb clinideid
 4) Create the tables needed for CliniDeID:
 	psql -d clinideid -q -f createDbTables.sql

The database server must be running before CliniDeID runs if outputting to a database is desired. The file 'createDbTables.sql' describes the different tables and columns in the clinideid database. 
CliniDeID will connect to the database named clinideid on the same machine and using the default username.
The command 'psql clinideid' will start an SQL prompt connected to the clinideid database. 



Network Usage Details

The desktop application uses only a small number of HTTPS requests. No HTTPS traffic contains any PII.

All HTTPS traffic is limited to licensing information to validate requests with our servers. Below is a description of what is transmitted at different points in the application's life cycle.

When preparing to run a new batch of files, a request is sent out to our licensing server to validate the license key. This request only contains the license key, and returns a simple response with the following details:

* the license key's validity
* the number of "file-equivalents" (i.e., batches of 5 thousand characters) which have been processed with this license key 
* any limitations associated with the license, such as how many file-equivalents can be processed per batch, and the maximum allowed size of each file.

Immediately before starting the batch
Prior to starting the batch and after having validated the license key, a new request is sent out to the licensing server containing basic information about the current batch. The application receives the same object sent but with an ID assigned to it by the server.
* the license key again
* the number of files to process
* (received) an ID assigned to this request

During the running of the batch
License is updated every 10 files. Information is sent is the number of files and the their total size.

Immediately after finishing the batch
After finishing the batch, a final request is sent out to the server confirming the actual number of files processed. The information transmitted in this request is below:

* the license key again
* the number of "file-equivalents" processed
* the ID of the request (received earlier from the server)



Licensing
CliniDeID uses the Microsoft Visual Studio C++ Redistributable runtime.
Licenses can be found in data\licenses folder

"Python" is a registered trademark of the Python Software Foundation”
OpenJDK (the "Name")  Java and OpenJDK are trademarks or registered trademarks of Oracle and/or its affiliates.
