# Wyjazdy

Rodzinna apka Android do śledzenia wyjazdów służbowych — wspólny dashboard, kalendarz, lokalne przypomnienia. Zbudowana wg specyfikacji w `docs/spec.md` (faza 1 / MVP, kroki 1-8 z sekcji 8 specyfikacji: setup, Auth, CRUD wyjazdów, rodziny, mapy, kalendarz, bilet w Storage, lokalne przypomnienia). FCM push i przycisk statusu bezpieczeństwa (faza 2) nie są jeszcze zaimplementowane.

## Wymagania

- Android Studio (najnowsza stabilna wersja) z zainstalowanym Android SDK (compileSdk/targetSdk 34, minSdk 26).
- Konto Google + projekt w [Firebase Console](https://console.firebase.google.com).
- Klucz Google Maps API (Google Cloud Console).

## 1. Setup Firebase (~15 minut)

1. Wejdź na [Firebase Console](https://console.firebase.google.com) → **Add project** → nazwij np. "Wyjazdy".
2. **Authentication** → Sign-in method → włącz **Google**.
3. **Firestore Database** → Create database → wybierz region (np. `europe-west1`) → start w trybie production.
4. **Storage** (opcjonalnie) → Create bucket, na potrzeby zdjęć/PDF biletów. **Uwaga:** od pewnego czasu Google wymaga do Storage planu Blaze (pay-as-you-go) nawet przy zerowym realnym zużyciu (karta płatnicza, ale w praktyce bez opłat przy skali rodzinnej). Jeśli tego nie chcesz, po prostu pomiń ten punkt — apka działa bez tego, tylko przycisk "Dodaj bilet" w formularzu wyjazdu zwróci błąd (patrz sekcja "Storage wyłączony" poniżej). Firestore i Auth (Spark, darmowy) nie wymagają Blaze.
5. W ustawieniach projektu (⚙️ → Project settings) dodaj aplikację Android:
   - Package name: `com.rodzina.wyjazdy`
   - Pobierz wygenerowany `google-services.json` i **zastąp nim** plik `app/google-services.json` w tym repo (obecny to placeholder z fałszywymi danymi — bez tego apka się skompiluje, ale Auth/Firestore nie zadziałają).
6. Wgraj reguły bezpieczeństwa:
   - Firestore → Rules → wklej zawartość `firestore.rules` z tego repo.
   - Storage → Rules → wklej zawartość `storage.rules` z tego repo (tylko jeśli włączyłeś Storage w kroku 4).
7. W Authentication → Sign-in method → Google, skopiuj **Web client ID** (SHA-1 klucza podpisywania debug dodaj też w Project settings → Your apps, `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`). Wklej Web client ID do `app/src/main/res/values/strings.xml` jako `default_web_client_id` (patrz komentarz w tym pliku).

## 2. Setup Google Maps

1. [Google Cloud Console](https://console.cloud.google.com) → wybierz ten sam projekt co Firebase (Firebase projekty to projekty GCP) → włącz **Maps SDK for Android** i **Directions API**.
2. Utwórz klucz API (Credentials → Create credentials → API key), ogranicz go do Androida (package name + SHA-1 debug/release).
3. Skopiuj `local.properties.example` → `local.properties` (w katalogu głównym) i wklej klucz jako `MAPS_API_KEY=...`. Ten plik jest w `.gitignore`, nie trafi do repo.

## 3. Budowanie

Otwórz katalog projektu w Android Studio ("Open" → wskaż ten folder) i poczekaj na Gradle sync. Android Studio może zaproponować aktualizację AGP/Gradle — można ją zaakceptować.

Build z terminala (po zainstalowaniu Android SDK i ustawieniu `sdk.dir` w `local.properties`, co Android Studio robi automatycznie):

```
./gradlew assembleDebug
```

APK wyląduje w `app/build/outputs/apk/debug/app-debug.apk` — wyślij mailem/Driveem, zainstaluj na telefonie z opcją "zainstaluj z nieznanych źródeł" (patrz sekcja 10 specyfikacji).

## Storage wyłączony (bez planu Blaze)

Jeśli nie włączyłeś Firebase Storage (bo wymaga Blaze), wszystko działa normalnie **poza** załączaniem zdjęcia/PDF biletu:

- Formularz wyjazdu da się zapisać bez problemu — pole "Numer biletu" działa zawsze, to tylko tekst w Firestore.
- Jeśli ktoś kliknie "Dodaj bilet (PDF, JPG, PNG)" i wybierze plik, przy zapisie zobaczy komunikat, że wgranie się nie udało (bo nie ma gdzie wgrać) — trzeba wtedy kliknąć "X" przy przycisku, żeby usunąć wybrany plik, i zapisać ponownie.
- Nic więcej w apce (Auth, Firestore, Maps, kalendarz, przypomnienia) nie zależy od Storage.

Jeśli kiedyś zechcesz włączyć Storage, wystarczy w konsoli kliknąć "Create bucket", wkleić `storage.rules` w zakładce Rules — kod jest już gotowy i zacznie działać bez żadnych zmian.

## Struktura projektu

```
app/src/main/java/com/rodzina/wyjazdy/
  data/model/       # Family, Trip, User, enumy - odpowiadają 1:1 sekcji 3 specyfikacji
  data/repository/  # AuthRepository, FamilyRepository, TripRepository, StorageRepository, UserRepository
  ui/                # ekrany Compose wg sekcji 5 specyfikacji, jeden pakiet per ekran + ui/theme, ui/navigation, ui/common
  notifications/     # WorkManager - lokalne przypomnienia 24h/1h przed wyjazdem
```

## Co dalej (faza 2, poza zakresem tego builda)

- Prawdziwy push (FCM + Cloud Function) przy dodaniu/zmianie wyjazdu.
- Przycisk statusu bezpieczeństwa "Dojechałam" / "Wracam" wysyłający push do rodziny.
- Rysowanie/zapis trasy (Directions API) — pole `routePolyline` w modelu już istnieje, ale nic go dziś nie wypełnia.
- Zmiana roli admina ma UI w Ustawieniach rodziny, ale transfer bez potwierdzenia drugiej strony (do przemyślenia w fazie 2).
- Statystyki, sync z Google Calendar, widget, tryb offline (sekcja 4, faza 2/3).
