# File-synchronizer client

It's a client side of the project regarding file synchronizing through ssh with rsync on Windows 10.

The client is responsible for detecting changes in watching directory, sending files through ssh protocol with the use of rsync binaries.
 

Except installing necessary feature it is required to configure application.properties file as well as rsync with ssh configurations
which is more described below.

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
After you downloaded project, you can configure properties for your client.
Application-properties file can be found where you cloned repository under:
```
src/main/resources/application.properties
```
Now configure properties values as suggested below:
```
client.name = PC1 /* This name notifies server which client applies changes to server directory.
This can be changed to any name of your choose. Remember to name clients differently!  */
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
## Rsync configuration
In this project it is essential to use rsync binaries as well as ssh binaries for fluent connectivity.
Default location for those binaries is:
```
C:\Users\User\rsync4j
```
It is possible to use another location than C:\Users\User\rsync4j. You only have to set up the RSYNC4J_HOME 
environment variable to point to the top-level directory (doesn't have to exist) where you want to house the binaries and then restart
your working station.
### Installing

A step by step series of examples that tell you how to get a development environment running

Firstly we need to install project along with all required dependencies:

Go to the folder where you cloned repository and run:
```
mvn clean install
```
Maven will download rsync and ssh binaries from Cygwin distribution which are required in this project.
If build is completed successfully, continue to the rsync configuration.

### SSH Client configuration
##### Generating ssh key pairs
Navigate to the directory where you choose to store binaries and then run:
```
ssh-keygen
```
Firstly you will be asked to choose name for the file storing your key, you can leave it empty as by default.

Then you will be asked to provide password, just click enter to give it empty password. Any other password configuration will 
application to fail. Then you will be asked to enter password again and again leave it empty.

You should now see your public and private key name with your random image. If you have already installed OpenSSH Client(it is by deufalt enabled in Windows 10)
operating system may generate your key in 
```
.C:/Users/Username/.ssh
```
that's why you should check carefully where your keys are.

##### Generate SSH config file
SSH config file contains information necessary for ssh clients to connect to the ssh server via alias.

To create ssh config file, open text editor and create file WITHOUT EXTENSION with name = "config".
Here there is example of the content of config file:
```
Host server:
     HostName 10.10.10.10
     User username
```
In application-properties you could found property:
```
ssh.hostname
```
this property need to be equal to the one provided in ssh config file as hostname of the ssh server with server application.
Remember that config file has specific pattern:
```
Host hostname:
<-5 spaces->Property Value
```
You can read about ssh config here: [How to use SSH Config file](https://www.ssh.com/ssh/config/).

##### Changing permission of config file and ssh keys pair
SSH protocol was designed to work on UNIX-based systems, because of that we need to manually change permissions of config files, as well as ssh keys.

For all 3 files(public key, private key and config file):
- Open properties
- Go to security
- Open Advanced
- Disable inheritance
- Delete all users permissions EXCEPT SYSTEM AND YOURS USER
- Click confirm
### Sending ssh key to your ssh server
##### To securely copy ssh key you should have ssh server on your machine for server application.
##### It is recommended to not find other solution, just come back to this step after finishing "SSH SERVER configuration" in server application README

To copy ssh keys we simply need to copy them to server ssh destination:
```
scp .ssh/id_rsa.pub SshHostnameFromConfig:.ssh/authorized_keys
```
It should properly copy your ssh_key to server file.
If not, check if you have /.ssh/authorized_keys path in your server directory

##### Copying ssh config
Correct destination for ssh config is where you copied your binaries:
```
rsync_binaries_path/home/User/.ssh/config
```
##### Checking ssh
After you placed your config and set-up ssh server on your machine with server application run:
```
ssh sshHostNameFromConfig
```
and check if you can successfully connect to your server

## Running the tests

To run test run:

```
mvn test
```

## Deployment

To create executable jar go to your repository directory and run:
```
mvn clean package spring-boot:repackage
```
and then to execute jar:
```
java -jar project.jar
```
## Authors

* **Paweł Jelonek** - *Initial work* 

#