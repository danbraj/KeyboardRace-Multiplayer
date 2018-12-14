---
layout: default
---

1. Spis treści
{:toc}

## Opis projektu

Aplikacja do ćwiczenia szybkiego pisania na klawiaturze z elementami rywalizacji. W grze może uczestniczyć do sześciu graczy. Zadaniem użytkowników jest przepisanie tekstu najszybciej ze wszystkich. Serwer losuje (z puli tekstów) tekst do przepisania. W trakcie zabawy gracze dostają "punkty akcji", które mogą wykorzystać, by przeszkodzić konkurentom, na umiejętności specjalne takie jak ukrycie wprowadzanych liter, pomieszanie liter w przepisywanym tekście tudzież odwrócenie wyrazów w tekście. Dodatkowo, każdy użytkownik może wpłynąć na rozwój aplikacji, dodając ciekawe teksty do przepisania. Dodawany tekst może się pojawić, dopiero po potwierdzeniu ze strony serwera.

"Wyścig na klawiaturze" to nie tylko rozrywka, ale także przydatne ćwiczenie, z którego faktycznie można skorzystać, gdyż umiejętność szybkiego pisania jest bardzo ważna zwłaszcza dla informatyka. Szybsze pisanie = przyśpieszenie pracy.

![Zrzut ekranu aplikacji klienta i serwera](assets\img\apps-presentation.png)

## Instalacja

Aby uruchamiać aplikacje napisane w języku Java, potrzebne jest JRE.

1. Pobrać skompilowaną aplikację.
1. (TYLKO DLA SERWERA!) Utworzyć folder `Texts` w głównym katalogu, gdzie znajduje się serwer i dodać kilka plików tekstowych (`.txt`). 
1. Uruchomić aplikację.

## Związki pomiędzy poszczególnymi komponentami (uproszczony diagram UML)

![Schemat](assets\svg\simple-uml-schema.min.svg?sanitize=true)

### Wykorzystane klasy i ich opis

#### Wspólne dla klienta i serwera

* **App** - Klasa zawierająca stałe statyczne wykorzystywane w aplikacjach klienta oraz serwera. Pełni rolę podstawowych ustawień aplikacji. Ustawieniom podlega:
  * nazwa aplikacji,
  * wersja aplikacji,
  * domyślny port połączenia,
  * maksymalna liczba użytkowników, którzy mogą się zalogować do serwera.
* **Status** - Pseudo- typ wyliczeniowy pełniący funkcję flagi. Informuje w jakim stanie znajduje się aplikacja.

#### Serwer

* **ServerApp** - Główna klasa serwera. Inicjuje obiekt ServerGUI a przed tym deklaruje wszystkie początkowe wartości zmiennych serwera oraz ustala:
  * maksymalną liczbę próśb z nowymi zadaniami, które serwer może zmagazynować (ochrona przed spamem),
  * czy w ogóle jest dostępna możliwość wysyłania próśb.
* **ServerGUI** - Deklaruje wszystkie parametry GUI oraz tworzy graficzny interfejs aplikacji serwera. Klasa ta zajmuje się także losowaniem zadań (plików z zadaniem w folderze Texts, z których tworzony jest obiekt klasy Zadanie) do wykonania.
* **ServerGUI.Obsluga** - Klasa implementująca klasę ActionListener zajmująca się obsługą zdarzeń przycisków znajdujących się w aplikacji. Klasa obsługuje dwa zdarzenia:
  * przełącznik uruchomienia/zatrzymania serwera
  * wyświetlenie wyskakującego okna z przesłanym do klienta zadaniem
* **Server** - Klasa implementująca interfejs Runnable. Uruchomienie serwera wiąże się z utworzeniem obiektu tej klasy oraz uruchomieniem go jako wątek. Zatrzymanie serwera to zakończenie działania wątku serwera. Operacji uruchomienia, bądź zatrzymania serwera towarzyszy także zmiana ustawień odpowiednich komponentów w klasie ServerGUI.
* **ServerConnection** - Klasa implementująca interfejs Runnable tudzież rozszerzająca klasę Connection. Obiekt tej klasy jest tworzony po pomyślnym połączeniu się do serwera. Po utworzeniu obiektu uruchamiany jest jako wątek, gdzie przechodzi w stan nasłuchu na obiekty klasy Packet lub dziedziczące klasę Packet (ExtendedPacket) od klienta. Po otrzymaniu zserializowanego obiektu żądanego typu, wykonuje różne akcje zależne od protokołu oraz parametrów otrzymywanych od klienta.
* **FilesService** - Klasa statyczna pełniąca funkcję obsługi plików w aplikacji serwera. Za jej pomocą można pobrać wszystkie pliki tekstowe znajdujące się w ustalonej w kodzie lokalizacji lub zapisać przesłany w parametrze tekst w pliku.

#### Klient

* **ClientApp** - Główna klasa klienta. Inicjuje obiekt ClientGUI a przed tym deklaruje wszystkie początkowe wartości zmiennych klienta.
* **ClientGUI** - Deklaruje wszystkie parametry GUI oraz tworzy graficzny interfejs aplikacji klienta. ClientGUI posiada metody, które zmieniają ustawienia komponentów w tej klasie. Zmiany te to:
  * aktualizacja UI w zależności od stanu połączenia,
  * niewidzialny tekst w polu tekstowym znajdującym się na dole UI aplikacji,
  * odwrócenie wyrazów w tekście, w miejscu gdzie znajduje się treść zadania,
  * pomieszanie środkowych liter wyrazów w tekście, w miejscu gdzie znajduje się treść zadania,
  * ucinanie pierwszego wyrazu w tekście, w miejscu gdzie znajduje się treść zadania,
  * przywrócenie tekstu do zgodnego z zadaniem.
* **ClientGUI.Obsluga** - Klasa implementująca klasę ActionListener zajmująca się obsługą zdarzeń przycisków znajdujących się w aplikacji. Klasa obsługuje trzy zdarzenia:
  * przełącznik zalogowania się do (wylogowania z) serwera,
  * przełącznik gotowości aktualnie zalogowanego użytkownika,
  * wyświetlenie wyskakującego okna z polem tekstowym, do którego należy skopiować lub napisać tekst, który można później wysłać do serwera, jako prośbę o dodanie takowego do aplikacji.
* **PanelPlayer** - Klasa rozszerzająca klasę JPanel. Jest to niestandardowa płyta, która pełni rolę slotu dla użytkownika. Każdy zalogowany użytkownik zajmuje miejsce w tablicy elementów klasy PanelPlayer. W tablicy są prezentowane wszystkie informacje o użytkownikach i ich działaniach, które są istotne dla klientów. Klasa zawiera metody, które:
  * zmieniają nazwę użytkownika,
  * zmieniają wartość paska postępu,
  * zmieniają kolor tła, na którym znajduje się nazwa użytkownika (zmiana gotowości),
  * zmieniają liczbę punktów akcji,
  * ustawiają miejsce, które użytkownik zajął po ukończeniu zadania,
  * zmieniają aktywność lub kolor przycisków przy użytkownikach.
* **ObslugaUmiejetnosci** - Klasa implementująca klasę ActionListener zajmująca się obsługą zdarzeń przycisków umiejętności znajdujących się w panelach użytkowników. Każdy przycisk to użycie konkretnej umiejętności na danym użytkowniku.
* **ClientConnection** - Klasa implementująca interfejs Runnable tudzież rozszerzająca klasę Connection. Połączenie z serwerem wiąże się z utworzeniem obiektu tej klasy oraz uruchomieniem go jako wątek. Po pomyślnym połączeniu wątek przechodzi w stan nasłuchu na obiekty klasy Packet lub dziedziczące klasę Packet (ExtendedPacket) od serwera. Po otrzymaniu zserializowanego obiektu żądanego typu, wykonuje różne akcje zależne od protokołu oraz parametrów otrzymywanych od serwera.
* Typ wyliczeniowy **Variety** - Zawiera szczegółowe dane dotyczące wszystkich umiejętności dostępnych w aplikacji. Każda umiejętność zawiera:
  * identyfikator umiejętności,
  * skrót (oznaczenie na przycisku),
  * koszt wyrażony w punktach akcji,
  * czas trwania wyrażony w sekundach,
  * kolor aktywnej umiejętności. 

## Przesyłanie danych pomiędzy serwerem a klientem

![Schemat](assets\svg\communication-schema.min.svg?sanitize=true)

### Opis klas wykorzystywanych przy komunikacji klient-serwer

* Typ wyliczeniowy **Command** - pełni funkcję protokołu komunikacji pomiędzy klientem a serwerem.
* Typ wyliczeniowy **Debuff** - zbiór umiejętności, które można zastosować w trakcie rozgrywki.
* **Player** - klasa zawierająca informacje o danym użytkowniku, które są istotne dla serwera i pozostałych użytkowników np. identyfikator, pseudonim, gotowość.
* **Zadanie** - klasa zawierająca i obsługująca zadanie do wykonania przez użytkowników. 
* **Packet** - klasa, używana przy przesyłaniu danych pomiędzy serwerem a klientami. Komunikacja pomiędzy serwerem a klientem odbywa się poprzez wymianę obiektów tego typu. Obiekt tej klasy musi zawierać protokół (typ wyliczeniowy Command) oraz opcjonalne parametry typu int, string, bool, enum Debuff.
* **ExtendedPacket** - klasa dziedzicząca po klasie Packet. W tej klasie dodatkowymi parametrami przesyłanymi wraz z protokołem poza tymi z klasy Packet są:
  * lista użytkowników (ArrayList<Player>)
  * zadanie (Zadanie).

### Sposób komunikacji klient-serwer

Aplikacja, do komunikacji pomiędzy klientami a serwerem, wykorzystuje sockety a przy tym serializację obiektów (klasa Packet i ExtendedPacket) zawierących stosowne właściwości, zależne od właściwości protokołu (typ wyliczeniowy Command).

Protokół | Efekt
-------- | -----
`NONE` |
`LOGIN_REQUEST` | przekazanie prośby o możliwość zalogowania się do serwera
`LOGIN_RESPONSE` | przekazanie odpowiedzi pozytywnej na prośbę o zalogowanie
`NICK_SET` | przekazanie informacji serwerowi o nadanym pseudonimie
`LOGOUT` | wylogowanie się
`UPDATE_PLAYERS_LIST` | aktualizacja użytkowników dla nowego użytkownika oraz poinformowanie innych użytkowników o nowym użytkowniku
`LOGOUT_PLAYER_NOTIFY` | poinformowanie pozostałych użytkowników o wylogowującym się użytkowniku
`CHANGE_READY` | przekazywanie gotowości użytkownika
`START_GAME` | rozpoczęcie rozgrywki
`PROGRESS` | przekazywanie poziomu ukończenia rozgrywki przez użytkownika
`WIN` | przekazanie informacji o ukończeniu zadania przez użytkownika
`SEND_TEXT_REQUEST` | wysłanie zapytania o możliwość przesłania nowego zadania do serwera aplikacji
`SEND_TEXT_RESPONSE` | wysłanie odpowiedzi na prośbę o możliwość przesłania zadania
`SEND_TEXT` | przesłanie do serwera zadania (tekstu do przepisywania)
`RESET` | zakończenie rozrywki, ogłoszenie tablicy wyników oraz przywrócenie UI do stanu początkowego
`DEBUFF_CAST` | przekazanie informacji o użyciu danej umiejętności i uruchomienie działania na danym użytkowniku
`DEBUFF_CLEAR` | przekazanie informacji o przeminięciu danej umiejętności i anulowanie działania na danym użytkowniku

## Opis interfejsu graficznego serwera

![Zrzut ekranu aplikacji serwera](assets\img\server.png)

Interfejs graficzny serwera składa się z 4 istotnych elementów:
* Przełącznik “Uruchom/Zatrzymaj” **(1)** - służy do uruchamiania i zatrzymywania serwera.
* Pole tekstowe oznaczone etykietą “Port:” **(2)** - służy do ustalenia, na którym porcie uruchomiony zostanie serwer.
* Obszar tekstowy **(3)** - informuje o kluczownych zdarzeniach serwera
* Przycisk “Pokaż odebrane zadania (x)” **(4)** - wyświetla, wysłane przez klienta, zadanie w wyskakującym oknie, gdzie może zostać zaakceptowane w niezmienionej formie, bądź poprawione lub całkiem odrzucone przez administratora serwera.

## Opis interfejsu graficznego klienta

![Zrzut ekranu aplikacji klienta](assets\img\client.png)

Najważniejszym elementem aplikacji jest szary obszar tekstowy **(1)**. Jest on elementem “centralnym”, znaczy to tyle, że przy zmianie wielkości okna jest responsywny w dwóch wymiarach. W nim wyświetlona zostaje treść zadania do wykonania. Poniżej znajduje się pole tekstowe, które służy do wprowadzania tekstu wyświetlającego się powyżej **(2)**. Po prawej stronie znajduje się drugi obszar tekstowy, który to wyświetla różne informacje od serwera lub klienta, czyli pełni funkcję dziennika zdarzeń **(3)**. Poniżej rejestru zdarzeń jest pole tekstowe oznaczone etykietą “Serwer (host:port)”, gdzie należy podać ip i port serwera, z którym ma nastąpić połączenie **(4)**. Zaraz obok znajdują się dwie etykiety. Pierwsza zawiera informację o wersji aplikacji, natomiast druga o stanie połączenia. W prawym dolnym rogu są 4 przyciski **(5)**:
* Przełącznik “Połącz/Rozłącz” - służy do połączenia i rozłączenia się z serwerem.
* Przełącznik “Gotowość” - służy do zmiany gotowości użytkownika.
* Przycisk “Dodaj tekst do gry” - opcja ta, wysyła zapytanie o możliwość przesłania zadania do serwera, a po pomyślnej odpowiedzi, wyświetla wyskakujące okno z obszarem tekstowym, gdzie należy napisać lub wkleić tekst. Po zatwierdzeniu, prośba z tekstem do akceptacji zostaje wysłana do serwera.
* Przycisk “Dokumentacja aplikacji” - przenosi użytkownika do witryny internetowej dokumentacji aplikacji.

Ostatnim obszarem jest, znajdująca się na górze aplikacji lista użytkowników **(6)**. Każdy element listy (użytkownik) zawiera (od lewej):

* Kolor użytkownika, na którym wyświetlane jest także miejsce, które zajął użytkownik po ukończeniu zadania.
* Pseudonim użytkownika, na którym tło symbolizuje gotowość obecnego użytkownika (zielony - gotowy, czerwony - niegotowy).
* Punkty akcji - punkty, które zdobywa się w trakcie rozgrywki, przepisując kolejne wyrazy. Wykorzystywane są do używania umiejętności przeciwko swoim oponentom. Ilość widoczna tylko dla aktualnego użytkownika.
* 3 przyciski - są to umiejętności, które zostają odblokowywane po uzyskaniu konkretnej ilości punktów akcji. Użycie umiejętności odbywa się poprzez kliknięcie na przycisk przy danym użytkowniku. Informacja o działaniu danej umiejętności na konkretnym użytkowniku jest widoczna dla wszystkich (stosowny kolor). Informacje te pomagają przy strategicznym planowaniu ataku na oponentów.
* Pasek postępu wykonania zadania.

**Baw się dobrze!**