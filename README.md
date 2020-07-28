##	Instrukcja uruchamiania aplikacji klienckiej
Kolejne kroki przeprowadzają użytkownika przez proces konfiguracyjny aplikacji klienckiej. Konfiguracja tej części
jest zalecana po konfiguracji aplikacji serwerowej z powodu zależnych od siebie w kolejności kroków. 
W zależności od zabezpieczeń sieci oraz komputera istnieje prawdopodobieństwo na dopasowanie instrukcji do posiadanego 
sprzętu.

## Prerekwizyty
Proszę upewnić się, że posiadasz skonfigurowane narzędzia:
* Maven - [Download & Install Maven](https://maven.apache.org/)
* OpenSSH Client - [Download & Install OpenSSH Client](https://www.openssh.com/)
* Java - [Download & Install Java](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) 

### Konfiguracja pliku application.properties
Kiedy użytkownik wypakuje pliki do lokacji posiadającej wolne miejsce na dysku, pierwszym krokiem konfiguracyjnym 
jest ustawienie wartości właściwości w pliku application.properties. Plik ten znajduje się w ścieżce:
```
file-synchronizer-client/src/main/resources/application.properties
```
Jest to zwykły plik tekstowy, korzystający ze wbudowanych właściwości Spring oraz pozwalający na definiowanie nowych 
zmiennych przez użytkownika.
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
- logging.level.root — określa stopień widoczności logów wyświetlanych przez aplikację. Wszystkimi możliwościami 
podanymi od najmniejszej widoczności są: OFF, FATAL, ERROR, WARN, INFO, DEBUG. TRACE oraz ALL. Zalecane 
pozostawienie domyślnej wartości INFO, ponieważ większa widoczność logów powoduje ich mniejszą przejrzystość i zalecana 
jest w przypadku diagnozowania błędów programisty.
Do właściwości zdefiniowanych przez użytkownika należą:
- client.name — jest to nazwa identyfikująca aplikację kliencką. Wymagane jest, aby dla każdej aplikacji wybrać 
unikalną nazwę, w przypadku identycznych nazw aplikacja nie będzie działać prawidłowo.
- environment — nazwa środowiska (obecnie tylko PROD i TEST). Zmienna wykorzystywana do testów. Proszę nie zmieniać.
- file.synchronizer.fileList.endpoint — statyczny endpoint potrzebny do api.
- file.synchronizer.setModificationDate.endpoint - statyczny endpoint potrzebny do api.
- file.synchronizer.removeFiles.endpoint — statyczny endpoint potrzebny do api.
- file.synchronizer.logfile.endpoint — statyczny endpoint potrzebny do api.
- user.local.directory - ścieżka folderu synchronizacyjnego klienta. przykład: /home/osboxes/clientFiles
- rsync.remote.shell — dostępne tylko ssh. Proszę nie zmieniać.
- ssh.hostname - nazwa serwera z pliku "config" z katalogu ".ssh".
 
### Konfiguracja rsync
#### Linux
W przypadku systemu Linux, narzędzie rsync jest domyślnie dostępne na większości systemów. W przypadku jego braku
wymagane jest zainstalowanie narzędzia zgodnie z systemem operacyjnym użytkownika.
#### Windows
Aplikacja kliencka korzysta z biblioteki Rsync4J, która każdorazowo podczas procesu uruchamiania sprawdza istnienie 
plików binarnych rsync, w przypadku nie znalezienia, pobiera wymagane pliki. Domyślnym miejsce pobrania plików 
jest: 
```
"C:/Użytkownicy/Użytkownik/rsync4j"
```
Aby zmienić domyślną lokalizację, należy dodać zmienną środowiskową o nazwie RSYNC4J_HOME do ścieżek w liście “Path”. 
Zalecane jest zresetowanie stacji roboczej w celu odświeżenia oprogramowania i wczytania nowej zmiennej przez system.

### Instalacja zależności
W celu pobrania wszystkich wymaganych zależności należy otworzyć dowolny wiersz poleceń i uruchomić:
```
mvn clean install           
```                                              
Spowoduje to pobranie wszystkich wymaganych zależności określonych w pliku pom.xml przez Apache Maven. W przypadku 
wyświetlenia w wierszu poleceń komunikatu “BUILD SUCCESS” instalacja przebiegła pomyślnie.

### Konfiguracja OpenSSH Client
#### Generowanie kluczy ssh
##### Windows
Proszę otworzyć dowolny wiersz linii komend w miejscu, które wybrałeś na przechowywanie plików binarnych rsync i wpisać:
```
ssh-keygen
```
##### Linux
Proszę otwórz swój lokalny folder ".ssh" znajdujący się w katalogu użytkownika i wpisać:
```
ssh-keygen
```
Program najpierw zapyta użytkownika o nazwę pliku przechowującego klucz ssh, jednak zalecane jest niezmienianie wartości
domyślnych. Wiersz poleceń wyświetli pytanie o wybranie hasła, z perspektywy rozwiązanie wymagane jest niewybieranie 
żadnego hasła i pozostawienie tych pól (hasło i potwierdzenie hasła) jako pustych. Po wykonaniu zaleceń 
 program powinien wygenerować dwa klucze: 
- id_rsa — klucz prywatny 
- id_rsa.pub — klucz publiczny.

#### Generowanie pliku konfiguracyjnego
Klient protokołu ssh pobiera informacje ułatwiające komunikację z serwerem poprzez plik “config”. Rozwiązanie 
zakłada utworzenie pliku konfiguracyjnego w celu ułatwienia komunikacji.
##### Windows
Aby go utworzyć, proszę otworzyć dowolny edytor plików tekstowych i zapisać plik bez rozszerzenia z nazwą “config” 
w lokalizacji, która została wybrana na przechowywanie plików binarnych rsync, czyli wybrana_ścieżka/home/Użytkownik/.ssh/.
##### Linux
Aby go utworzyć, proszę otworzyć dowolny edytor plików tekstowych i zapisać plik bez rozszerzenia z nazwą “config” 
w domyślnej lokalizacji ssh tj. /home/Użytkownik/.ssh/.
```
Host server: 
     HostName 10.10.10.10  
     User username
```
Powyższy fragment zawiera przykładowy plik “config” gdzie:
- Host — oznacza nazwę, jaką wybieramy na nazwanie naszego komputera z serwerem ssh. Wymagane jest, aby była identyczna 
ze zmienną “ssh.hostname” z pliku application.properties.
- HostName — adres sieciowy komputera z uruchomionym serwerem ssh.
- User — nazwa użytkownika, który uruchamia w swojej przestrzeni dyskowej aplikację serwerową rozwiązania.
Należy pamiętać o wzorze, jaki należy przestrzegać wypełniając wartości pliku “config”. Każda właściwość definiowana 
dla wybranego hosta, musimy być oddalona od początku linii. Nieprzestrzeganie wzorca powoduje niewczytanie 
pliku “config” przez klienta ssh.

#### Zmiana zabezpieczeń pliku “config” oraz kluczy ssh
##### Windows
Z powodu zaprojektowania SSH pod systemy rodziny UNIX, musimy zmienić uprawnienia pliku “config” oraz obu kluczy ssh, 
aby były poprawnie odczytywane przez protokół.

Dla każdego z wymienionych plików:
- Otwórz “Właściwości”
- Przejdź do zakładki “Zabezpieczenia”
- Kliknij przycisk “Zaawansowane”
- Wybierz “Wyłącz dziedziczenie”
- Usuń dostęp wszystkich użytkowników z wyjątkiem użytkownika “System” oraz obecnie zalogowane
- Wciśnij przycisk “Zastosuj”
#####Linux
W przypadku systemu Linux należy sprawdzić, czy zalecane dostępy do plików różnią się od posiadanych.
Zalecane zabezpieczenia:
- chmod 700 ~/.ssh
- chmod 644 ~/.ssh/authorized_keys
- chmod 644 ~/.ssh/known_hosts
- chmod 644 ~/.ssh/config
- chmod 600 ~/.ssh/id_rsa
- chmod 644 ~/.ssh/id_rsa.pub
 
#### Wysyłanie klucza SSH na serwer
Aby bezpiecznie skopiować klucz, należy najpierw przeprowadzić konfigurację aplikacji serwera, gdzie tworzymy plik 
“authorized_keys”, który będzie przechowywać nasz klucz publiczny.
Przykładem bezpiecznego przesłania kluczy jest:
```
scp .ssh/id_rsa.pub SshHost:.ssh/authorized_keys
```
gdzie:
- SshHost — to nazwa hosta, którą wybraliśmy w pliku “config”.

#### Sprawdzanie poprawności konfiguracji
Aby sprawdzić łączność z serwerem, należy otworzyć dowolny wiersz poleceń wpisać:
```
ssh sshHost
```
Jeżeli po wpisaniu komendy zostałeś poprawnie połączony z serwerem ssh, konfiguracja przebiegła pomyślnie, w przeciwnym
razie zalecane jest powtórzenie konfiguracji aplikacji od początku.

### Uruchamianie testów
Aby uruchomić testy aplikacji, proszę uruchomić wiersz linii komend w lokalizacji projektu, a następnie wpisać:
```
mvn test  
```
W przypadku wyświetlenia komunikatu “BUILD SUCCESS” testy przebiegły pomyślnie i  aplikacja jest gotowa do uruchomienia.

### Uruchamianie aplikacji
W celu uruchomienia aplikacji proszę otworzyć dowolny wiersz poleceń w lokalizacji projektu i wpisać:
```
mvn clean spring-boot:run  
```
Wpisanie wymienionych komendy spowoduje zbudowanie projektu, następnie usunięciu niepotrzebnych plików, by na końcu 
uruchomić aplikację.

### Tworzenie pliku wykonawczego
Aby utworzyć plik wykonawczy aplikacji serwera, proszę otworzyć wiersz poleceń w lokalizacji projektu oraz wpisać:
```
mvn clean package spring-boot:repackage 
```
Spowoduje to utworzenie pliku wykonawczego filesynchronizer-client-1.0.0.jar w lokalizacji target/.

### Uruchamianie pliku wykonawczego
Po poprawnym wykonaniu poleceń z poprzedniego podpunktu proszę uruchomić dowolny wiersz poleceń, odnaleźć utworzony 
plik oraz wpisać:
```
java -jar filesynchronizer-client-1.0.0.jar
```

## Autor
* **Paweł Jelonek** 
