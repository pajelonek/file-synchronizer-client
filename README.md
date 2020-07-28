##	Instrukcja uruchamiania aplikacji klienckiej
Kolejne kroki przeprowadzają użytkownika przez proces konfiguracyjny aplikacji klienckiej. Konfiguracja tej części
jest zalecana po konfiguracji aplikacji serwerowej z powodu zależnych od siebie w kolejności kroków. 
W zależności od zabezpieczeń sieci oraz komputera, istnieje prawdopodobieństwo na dopasowanie instrukcji do posiadanego 
sprzętu.
## Prerekwizyty
Upewnij się, że zgodnie z częścią teoretyczną pracy posiadasz skonfigurowane narzędzia:
* Git - [Download & Install Git](https://git-scm.com/downloads)
* Maven - [Download & Install Maven](https://maven.apache.org/) 
* OpenSSH Client - [Download & Install OpenSSH Server](https://www.bleepingcomputer.com/news/microsoft/how-to-install-the-built-in-windows-10-openssh-server/)
* Java - [Download & Install Java](https://maven.apache.org/)


###1.	Konfiguracja pliku application-properties
Kiedy użytkownik wypakuje pliki do lokacji posiadającej wolne miejsce na dysku, pierwszym krokiem konfiguracyjnym 
jest ustawienie wartości właściwości w pliku application-properties. Plik ten znajduje się w ścieżce:
```
\src\main\resources\application-properties
```
Jest to zwykły plik tekstowy, korzystający z wbudowanych właściwości pozwalający na definiowanie nowych 
przez użytkownika.
```
client.name = PC1   
environment = PROD   
logging.level.root = INFO   
file.synchronizer.address = http://IP:PORT   
file.synchronizer.fileList.endpoint=/getFileList  
file.synchronizer.registerFiles.endpoint=/registerFiles
file.synchronizer.removeFiles.endpoint=/removeFiles  
file.synchronizer.logfile.endpoint=/getFileLogList  
user.local.directory=/home/osboxes/clientFiles
rsync.remote.shell=ssh  
ssh.hostname=server   
 ```
Przedstawiony fragment kodu przedstawia plik należący do rozwiązania wchodzącego w skład kodu źródłowego.
Do właściwości zdefiniowanych przez Spring należą:
-	logging.level.root - określa stopień widoczności logów wyświetlanych przez aplikację. Wszystkimi możliwościami 
podanymi od najmniejszej widoczności są: OFF, FATAL, ERROR, WARN, INFO, DEBUG. TRACE oraz ALL. Zalecane 
pozostawienie domyślnej wartości INFO ponieważ większa widoczność logów powoduje ich mniejszą przejrzystość i zalecana 
jest w przypadku diagnozowania błędów programisty.
Do właściwości zdefiniowanych należą:
-	client.name - jest to nazwa identyfikująca aplikację kliencką. Zalecane jest, aby dla każdej aplikacji wybrać 
unikalną nazwę
-	environment - nazwa środowiska (obecnie tylko PROD i TEST). Nie zmieniać.
-	file.synchronizer.fileList.endpoint - statyczny endpoint potrzebny do api. Nie zmieniać.
-	file.synchronizer.setModificationDate.endpoint - statyczny endpoint potrzebny do api. Nie zmieniać.
-	file.synchronizer.removeFiles.endpoint - statyczny endpoint potrzebny do api. Nie zmieniać.
-	file.synchronizer.logfile.endpoint - statyczny endpoint potrzebny do api. Nie zmieniać.
-	user.local.directory - Scieżka folderu synchronizacyjnego klienta. przykład: /home/osboxes/clientFiles
-	rsync.remote.shell - dostepne tylko ssh. nie zmieniać.
-	ssh.hostname - Nazwa serwera z pliku config ssh.
 
###2.	Konfiguracja rsync
####Linux
W przypadku systemu Linux, narzędzie rsync jest domyślnie dostępne.
####Windows
Aplikacja kliencka korzysta z biblioteki Rsync4J, która każdorazowo podczas procesu uruchamiania sprawdź istnienie 
plików binarnych rsync, w przypadku nie znalezienia, pobiera wymagane pliki. Domyślnym miejsce pobrania plików 
jest C:/Użytkownicy/Użytkownik/rsync4j.  
Aby zmienić domyślną lokalizację, należy dodać zmienną środowiskową  o nazwie RSYNC4J_HOME do ścieżek w liście “Path”. 
Zalecane  jest zresetowanie stacji roboczej w celu odświeżenia oprogramowania i wczytania nowej zmiennej 
przez system.
###3.	Instalacja zależności
W celu pobrania wszystkich wymaganych zależności należy otworzyć dowolny wiersz poleceń i uruchomić:
 ```
mvn clean install           
 ```                                              
Spowoduje to pobranie wszystkich wymaganych zależności określonych w pliku pom.xml przez Apache Maven. W przypadku 
wyświetlenia w wierszu poleceń komunikatu “BUILD SUCCESS” instalacja przebiegła pomyślnie.
###4.	Konfiguracja OpenSSH Client
####4.1	Generowanie kluczy ssh
#####Windows
Otwórz dowolny wiersz linii komend w miejscu, które wybrałeś na przechowywanie plików binarnych rsync i następnie wpisz:
 ```
ssh-keygen
 ```
#####Linux
Otwórz swój lokalny folder .ssh znajdujący się w katalogu użytkownika i wpisz:
 ```
ssh-keygen
 ```
Program najpierw zapyta użytkownika o nazwę pliku przechowującego klucz ssh jednak zalecane jest nie zmienianie wartości
 domyślnych. Następnie wiersz poleceń wyświetli pytanie o wybranie hasła, z perspektywy rozwiązanie wymagane jest nie 
 wybieranie żadnego hasła i pozostawienie tych pól(hasło i potwierdzenie hasła) jako pustych. Po wykonaniu zaleceń 
 program powinien wygenerować dwa klucze: 
- id_rsa - klucz prywatny 
- id_rsa.pub - klucz publiczny.
####4.2	Generowanie pliku konfiguracyjnego
Klient protokołu ssh pobiera informacje ułatwiające komunikację z serwerem poprzez plik “config”. Rozwiązanie 
zakłada utworzenie pliku konfiguracyjnego w celu ułatwienia komunikacji.
 
Aby go utworzyć, otwórz dowolny edytor plików tekstowych i zapisz plik bez rozszerzenia z nazwą “config” w lokalizacji,
 który została wybrana na przechowywanie plików binarnych rsync, czyli wybrana_ścieżka/home/Użytkownik/.ssh/.
```
Host server: 
     HostName 10.10.10.10  
     User username
```
Powyższy fragment kodu zawiera przykładowy plik “config” gdzie:
-	Host - oznacza nazwę jaką wybieramy na nazwanie naszego komputera z serwerem ssh. Musimy być identyczne jak 
zmienna “ssh.hostname” z pliku application.properties.
-	HostName - adres sieciowy komputera z uruchomionym serwerem ssh.
-	User - nazwa użytkownika, który uruchamia w swojej przestrzeni dyskowej aplikacji serwerową rozwiązania
Należy pamiętać o wzorze, jaki należy przestrzegać wypełniając wartości pliku “config”. Każda właściwość definiowana 
dla wybranego hosta, musimy być oddalona od początku linii. Nieprzestrzeganie wzorca powoduje nie wczytanie 
pliku “config” przez klienta ssh.
####4.3	Zmiana zabezpieczeń pliku “config” oraz kluczy ssh
#####Windows
Z powodu zaprojektowania SSH pod systemy rodziny UNIX, musimy zmienić uprawnienia pliku “config” oraz obu kluczy ssh 
aby były poprawnie odczytywane przez protokół.

Dla każdego z wymienionych plików:
1.	Otwórz “Właściwości”
2.	Przejdź do zakładki “Zabezpieczenia”
3.	Kliknij przycisk “Zaawansowane”
4.	Wybierz “Wyłącz dziedziczenie”
5.	Usuń dostęp wszystkich użytkowników z wyjątkiem użytkownika “System” oraz obecnie zalogowane
6.	Wciśnij przycisk “Zastosuj”
#####Linux
W przypadku systemu Linux, należy sprawdzić czy zalecane dostępy do plików różnią się od posiadanych.
Zalecane zabezpieczenia:
 - chmod 700 ~/.ssh
 - chmod 644 ~/.ssh/authorized_keys
 - chmod 644 ~/.ssh/known_hosts
 - chmod 644 ~/.ssh/config
 - chmod 600 ~/.ssh/id_rsa
 - chmod 644 ~/.ssh/id_rsa.pub
####4.4 Wysyłanie klucza SSH na serwer
Aby bezpiecznie skopiować klucz, należy najpierw przeprowadzić konfigurację aplikacji serwera, gdzie tworzymy plik 
“authorized_keys”, który będzie przechowywać nasz klucz publiczny.
Aby bezpiecznie skopiować pliki, należy otworzyć wiersz poleceń oraz wpisać:
```
scp .ssh/id_rsa.pub SshHost:.ssh/authorized_keys
```
gdzie:
 - 	SshHost - to nazwa hosta, którą wybraliśmy w pliku “config”.
 
####4.5 Sprawdzanie poprawności konfiguracji
Aby sprawdzić łączność z serwerem, należy otworzyć dowolny wiersz poleceń wpisać:
```
ssh sshHost
```
Jeżeli po wpisaniu komendy zostałeś poprawnie połączony z serwerem ssh, konfiguracja przebiegła pomyślnie, w przeciwnym 
w przeciwnym razie zalecane jest powtórzenie konfiguracji aplikacji od początku.
###5.	Uruchamianie testów
Aby uruchomić testy aplikacji, uruchom wiersz linii komend w lokalizacji projektu a następnie wpisz:
```
mvn test  
```
W przypadku wyświetlenia komunikatu “BUILD SUCCESS” testy przebiegły pomyślnie i  aplikacja jest gotowa do uruchomienia.
###6.	Uruchamianie aplikacji
W celu uruchomienia aplikacji, otwórz dowolny wiersz poleceń w lokalizacji projektu i wpisz:
```
mvn clean spring-boot:run  
```
Wpisanie wymienionych komendy spowoduje zbudowanie projektu, następnie usunięciu niepotrzebnych plików, by na końcu uruchomić aplikację.
###7.	Tworzenie pliku wykonawczego
Aby utworzyć plik wykonawczy aplikacji serwera należy otworzyć wiersz poleceń w lokalizacji projektu, po czym wpisać:
```
mvn clean package spring-boot:repackage 
```
Spowoduje to utworzenie pliku wykonawczego filesynchronizer-client-1.0.0.jar w lokalizacji target/.
###8.	Uruchamianie pliku wykonawczego
Po poprawnym wykonaniu poleceń z poprzedniego podpunktu, należy uruchomić dowolny wiersz poleceń, odnaleźć utworzony 
plik oraz wpisać:
```
java -jar filesynchronizer-client-1.0.0.jar
```
## Autor
* **Paweł Jelonek** 
