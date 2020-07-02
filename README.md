# File-synchronizer client

It's a client side of the project regarding file synchronizing through ssh with rsync on Windows 10.

The client is responsible for detecting changes in watching directory, sending files through ssh protocol with the use of rsync
 

Except installing necessary feature it is required to configure application.properties file as well as ssh configuration
which is described below.

## Prerequisites
Make sure you have installed all of the following prerequisites on your development machine:
* Git - [Download & Install Git](https://git-scm.com/downloads). OSX and Linux machines typically have this already installed.
* Maven - [Download & Install Maven](https://maven.apache.org/) - Dependency Management
* OpenSSH Client - [Download & Install OpenSSH Server](https://www.bleepingcomputer.com/news/microsoft/how-to-install-the-built-in-windows-10-openssh-server/). Windows 10 should already have this feature
enabled. No action needed right now.
* Java - [Download & Install Java](https://maven.apache.org/) - Runtime Environment essential to run application. At least 1.8 version. 


## Clone repository
To clone repository copy link from github, run git, go to the directory where you want
to copy repository and then type:

```
git clone [copied link from github]
```

### Configuration of application-properties
```
client.name = PC1 /* This name notifies server which client applied changes to server directory
can be changed to any name of your choose. Remember to name clients differently!  */
environment = PROD  /* Property for testing purposes, do not change */
logging.level.root = INFO /* For futher development you may want to change to DEBUG */

file.synchronizer.address = http://IP:PORT  /* Type your server IP address and port  */

/* Four properties below describe endpoints for specific actions on server, do not change unless
you change endpoint on server */
file.synchronizer.fileList.endpoint=/getFileList
file.synchronizer.setModificationDate.endpoint=/setModificationDate
file.synchronizer.removeFiles.endpoint=/removeFiles
file.synchronizer.logfile.endpoint=/getFileLogList

/* Provide the directory where you want to synchronize files with server */
user.local.directory=C:\\clientFiles
/* Remote shell to use via rsync, application only provies ssh, do not change */
rsync.remote.shell=ssh

/* Name of the server from ssh config, configuration will be in part "SSH configuration" */
ssh.hostname=server
```
### Installing

A step by step series of examples that tell you how to get a development env running

Firstly we need to install project along with all required dependencies:

Go to the folder where you cloned repository and run:
```
mvn clean install
```
Maven will download rsync binaries from Cygwin distribution which are required in this project.
If build is completed successfully, continue to the rsync configuration.

## Rsync configuration
We need rsync binaries to add rsync as systen environment variable.
Default location is:
```
C:\Users\User\rsync4j
```
It is possible to use another location than C:\Users\User\rsync4j. You only have to set up the RSYNC4J_HOME 
environment variable to point to the top-level directory (doesn't have to exist) where you want to house the binaries 
and keys.
##### !Remember! Changing the location will automatically mean to set rsync as environment variable. Leaving files in default location do not have this condition
Now, if you decided to change location, you can copy files from default location and place them in your new path or delete
files from previous directory and again run:
```
mvn clean install
```
to trigger download to the new path.

Run in command line to check if your files and environment variable is working successfully:
```
where rsync
rsync --version
```
If everything works right now, you can go on to ssh configuration.
## SSH Client configuration
/*
Z GRZESKIEM
DODAJEMY PO ZMIANIE LOKALIZACJI FOLDERU BINARKI DO PATH(RESTARTUJEMY) I SPRAWDZAMY CZY SSH I RSYNC DZIALAJA
teraz ssh-keygen
NO PASSPHRASE
OTWIERAMY W NOTEPADD IDRSA.PUB I KOPIUJEMY ZAWARTOSC
ROBIMY CONFIG NOTEPADEM NARAZIE NIE PRZYSYŁAMY KLUCZY SSH 
ZMIENIAMY DO NIEGO UPRAWNIENIA 
WYSYŁAMY KLUCZ Z CLIENTA DO SERVEERA
WYSYLAMY GO Z GIT CONSOLE KOMENDA CAT DZIEKI TEMU DODAMY HOST DO KNOWN_HOSTS
DZIALA TYLKO JAK WYSYLAMY ZA POMOCA SCP 
scp .ssh/id_rsa.pub Grzesiek@192.168.1.16:.ssh/authorized_keys tak zadziałało

POTEM KOMENDY SA OK
SPRAWDZ DEPENDENCJE BO NA KLIENCIE SIE TOMCAT ODPALA

*/
Make sure you have enabled and downloaded ssh files. They should be in this directory:
```
C:\Windows\System32\OpenSSH
```

If they are not, go back to Prerequisites and follow instructions on how to enable this feature.

###Important notes:
 Because OpenSSH is linux based protocol. We need to set manually access to file
## Running the tests

To run test run:

```
mvn test
```

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
