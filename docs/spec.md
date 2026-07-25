# Aplikacja "Wyjazdy" — spec projektu

Rodzinna aplikacja Android do śledzenia wyjazdów służbowych (szkolenia, spotkania) z widocznością dla całej rodziny i przypomnieniami push.

## 1. Cel

- 4 osoby w rodzinie, 2 najczęściej dodają wpisy — pozostali głównie przeglądają.
- Każdy widzi dashboard z wyjazdami całej rodziny: gdzie, kiedy, jak długo, jak dojeżdża, gdzie nocuje.
- Push przypomina o zbliżającym się wyjeździe.
- Element bezpieczeństwa: status "w drodze / dojechała / wraca" + kontakt awaryjny.
- Pierwsza apka mobilna — cel to też zdobyć pewność co do przepływu Android + Firebase, mając wcześniejsze doświadczenie tylko z lokalnymi apkami Electron.

## 2. Role użytkowników

Każdy członek rodziny **dodaje własne wyjazdy** i **widzi wyjazdy wszystkich**. To nie apka jednego "właściciela" z widzami — to wspólna rozpiska całej rodziny.

| Rola | Uprawnienia |
|---|---|
| **Admin** | wszystko co członek + zarządza członkami rodziny (zaprasza/usuwa), może przekazać rolę admina, może edytować/usuwać cudzy wyjazd (np. gdy ktoś zapomni zaktualizować) |
| **Członek rodziny** | dodaje/edytuje/usuwa **własne** wyjazdy, widzi wyjazdy wszystkich, dostaje push, komentuje cudze wyjazdy |

- Dołączanie do rodziny: kod zaproszenia / link (generowany przez admina).
- Zmiana admina: prosty transfer roli w ustawieniach (dowolny czas).
- Każdy wyjazd ma `ownerUserId` — na liście widać awatar/inicjały osoby, żeby od razu było wiadomo "kto gdzie jedzie" bez wchodzenia w szczegóły.

## 3. Model danych (Firestore)

```
families/{familyId}
  name: string
  adminUserId: string
  memberUserIds: string[]
  createdAt: timestamp

families/{familyId}/trips/{tripId}
  ownerUserId: string
  city: string                    // gdzie
  dateStart: timestamp            // kiedy - data/godzina wyjazdu
  dateEnd: timestamp              // kiedy - data/godzina powrotu
  transportType: enum             // car | train | bus | plane | other
  ticketInfo: {
    number: string
    fileUrl: string               // zdjęcie/PDF biletu w Firebase Storage
  } | null
  hotel: {
    name: string
    address: string
    checkIn: timestamp
    checkOut: timestamp
  } | null
  venueAddress: string            // adres miejsca szkolenia/spotkania
  venueLat: number
  venueLng: number
  eventType: enum                 // szkolenie | wydarzenie | spotkanie | inne
  eventTitle: string
  routePolyline: string | null    // zapisana trasa z Google Directions
  comment: string
  status: enum                    // planowany | w_drodze | dojechala | wraca | zakonczony
  emergencyContact: {
    name: string
    phone: string
  } | null
  createdAt: timestamp
  updatedAt: timestamp

families/{familyId}/trips/{tripId}/statusUpdates/{updateId}
  status: enum
  timestamp: timestamp
  note: string | null

users/{userId}
  displayName: string
  familyId: string
  fcmTokens: string[]             // wiele urządzeń
  notificationPrefs: {
    reminderHoursBefore: number[] // np. [24, 1]
    statusUpdates: boolean
  }
```

## 4. Funkcje

### MVP (faza 1)
- Logowanie (Google Sign-In przez Firebase Auth)
- Tworzenie/edycja/usuwanie **własnego** wyjazdu — wszystkie pola z sekcji 3
- Lista wyjazdów **całej rodziny** (nadchodzące / historia), z awatarem/inicjałami osoby przy każdej pozycji
- Filtr listy: wszyscy / tylko ja / wybrana osoba
- Szczegóły wyjazdu z mapą (Google Maps — pinezka adresu szkolenia)
- Kalendarz miesięczny z oznaczonymi dniami wyjazdów, kolor/inicjał wg osoby gdy nakładają się wyjazdy
- Lokalne przypomnienia (WorkManager) — 24h i 1h przed własnym wyjazdem
- Dołączanie do rodziny kodem zaproszenia

### Faza 2
- Prawdziwy push (FCM) — powiadomienia dla całej rodziny przy dodaniu/zmianie wyjazdu
- Status bezpieczeństwa — jeden przycisk "Dojechałam" / "Wracam" wysyłający push do rodziny
- Zapisywanie biletu (zdjęcie/PDF) w Firebase Storage
- Rysowanie/zapis trasy przejazdu (Directions API)
- Kontakt awaryjny widoczny na karcie wyjazdu
- Zmiana roli admina
- Statystyki (ile dni w trasie w miesiącu, najczęstsze miasta)

### Faza 3 (opcjonalnie)
- Sync z Google Calendar
- Widget na ekran główny (najbliższy wyjazd, wszystkich domowników)
- Tryb offline z synchronizacją po powrocie zasięgu

## 5. Ekrany (UI/UX)

1. **Logowanie** — Google Sign-In, dołącz do rodziny kodem / stwórz rodzinę
2. **Lista wyjazdów** — karty pogrupowane: "Nadchodzące" / "Historia", awatar osoby + kolor wg statusu, filtr osoby u góry, FAB "+ Dodaj wyjazd"
3. **Dodaj/edytuj wyjazd** — formularz sekcjami: Podstawowe (miasto, data, godziny) → Transport (kategoria + bilet) → Nocleg (hotel, opcjonalnie) → Miejsce szkolenia (adres + mapa) → Komentarz
4. **Szczegóły wyjazdu** — wszystkie dane + mini-mapa + przycisk statusu bezpieczeństwa + historia statusów
5. **Kalendarz** — widok miesiąca, kropka na dniach z wyjazdem, tap → szczegóły
6. **Mapa/trasa** — pełnoekranowa mapa z trasą i pinezką miejsca
7. **Ustawienia rodziny** — lista członków, kod zaproszenia, transfer roli admina
8. **Ustawienia powiadomień** — kiedy przypominać, jakie statusy wysyłać

### Kierunek wizualny
- Czytelność na pierwszy rzut oka (rodzina sprawdza "na szybko")
- Status koduje kolor: planowany (szary/niebieski), w drodze (pomarańczowy), dojechała (zielony), zakończony (szary)
- Karta wyjazdu na liście: miasto + data + ikona transportu + status jednym spojrzeniem

## 6. Stack techniczny

- **Android**: Kotlin, Jetpack Compose, Material 3
- **Backend**: Firebase — Firestore, Auth (Google Sign-In), Cloud Messaging, Cloud Functions (Node.js/TypeScript), Storage
- **Mapy**: Google Maps SDK for Android + Directions API
- **Lokalne przypomnienia**: WorkManager + AlarmManager (dokładne godziny)
- **Kalendarz**: własny widok Compose (biblioteka np. Kizitonwose Calendar) — bez zależności od Google Calendar w MVP
- **Zarządzanie stanem**: ViewModel + StateFlow, architektura MVVM

## 7. Reguły bezpieczeństwa Firestore (szkic)

```
match /families/{familyId} {
  allow read: if request.auth.uid in resource.data.memberUserIds;
  match /trips/{tripId} {
    allow read: if request.auth.uid in get(/databases/$(database)/documents/families/$(familyId)).data.memberUserIds;
    allow write: if request.auth.uid == get(/databases/$(database)/documents/families/$(familyId)).data.adminUserId
                 || request.auth.uid == resource.data.ownerUserId;
  }
}
```

## 8. Kolejność prac (proponowana)

Kolejność ułożona tak, żeby najpierw sprawdzić, że sync w ogóle działa (największe ryzyko przy pierwszej mobilce), a najbardziej "serwerową" część (prawdziwy push) zostawić na koniec.

1. **"Hello Firebase"** — jeden ekran, jedno pole tekstowe, zapis/odczyt z Firestore widoczny na dwóch telefonach naraz. Sprawdzian że backend żyje, zanim zbuduje się cokolwiek więcej.
2. Setup projektu Android Studio + Firebase Auth (Google Sign-In)
3. CRUD własnych wyjazdów + lista rodziny z live sync (bez map, bez pusha)
4. System rodzin (kod zaproszenia, role)
5. Szczegóły wyjazdu + integracja Google Maps
6. Kalendarz
7. Zdjęcie/PDF biletu (Firebase Storage)
8. Lokalne przypomnienia (WorkManager) — działa całkowicie bez backendu
9. Prawdziwy push (FCM + Cloud Function) — najbardziej "serwerowy" element, dorabiany na końcu bez przebudowy reszty
10. Status bezpieczeństwa ("dojechałam")

## 9. Backend — co realnie potrzebujecie

Nie piszecie własnego serwera. Firebase = backend-as-a-service:

- **Firebase Console** (przeglądarka) — założenie projektu, włączenie Auth + Firestore. ~15 minut, samo klikanie.
- **`google-services.json`** — jeden plik z konsoli wklejany do projektu Android Studio. To cała "instalacja" backendu po stronie apki.
- **Reguły bezpieczeństwa Firestore** — jeden plik deklaratywny (szkic w sekcji 7), nie jest to programowanie w klasycznym sensie.
- **Cloud Function do pusha** (faza 2) — jedyny fragment przypominający pisanie backendu: kilkanaście-kilkadziesiąt linijek Node.js/TypeScript, wyzwalane automatycznie przy zmianie w Firestore. Można to napisać razem z Claude Code tak jak resztę.
- **Koszt**: przy 4 osobach darmowy tier Firebase (Spark) starczy z dużym zapasem — nie ma tu ryzyka nieoczekiwanych opłat.

## 10. Dystrybucja

Prywatna apka rodzinna nie wymaga Google Play. Budujecie plik `.apk` w Android Studio, wysyłacie (mail/Drive), instaluje się ręcznie na telefonie ("zainstaluj z nieznanych źródeł"). Bez recenzji, bez opłaty deweloperskiej, bez czekania — podobnie jak dotychczasowe exe, tylko apk zamiast exe.

## 11. Otwarte pytania do doprecyzowania później

- Czy wyjazdy cykliczne (co tydzień ten sam adres) — potrzebny szablon/duplikowanie?
- Ile osób docelowo w rodzinie (wpływa na koszty Firebase — przy małej skali darmowy tier w zupełności wystarczy)?
- Czy potrzebny widok webowy dla reszty rodziny, czy wszyscy mają Androida?
