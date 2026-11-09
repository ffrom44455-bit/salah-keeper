# 🌙 Salah Keeper

> **A native Android prayer-alarm app that keeps ringing until you start praying, and logs whether you actually prayed.**

Built with **Kotlin + Jetpack Compose (Material 3)**, styled with an ultra-aesthetic Gen Z glassmorphism interface powered by **Haze (Chris Banes)**, offline prayer calculations via the **Adhan** library, and continuous cloud-based CI/CD via **GitHub Actions**.

---

## ✨ Features

- **Offline Prayer Calculations**: Uses `com.batoulapps.adhan` with configurable calculation methods (Karachi University default) and Hanafi/Shafi'i Asr juristic methods.
- **Location Intelligence**: GPS via `FusedLocationProviderClient` with fallback to a 20+ city worldwide picker or manual 5-prayer time entry.
- **Exact Alarm Cycle**: Uses `AlarmManager.setAlarmClock` to bypass Android Doze mode. Rings for 5 minutes, pauses for 1 minute, and repeats continuously until stopped.
- **Lockscreen Full-Screen Experience**: Launches an interactive full-screen intent directly over the Android lock screen.
- **Voice Dua Recognition**: To stop the alarm, recite the Prophet's Dua:
  > **اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ**  
  > *Allahumma a'inni 'ala dhikrika wa shukrika wa husni 'ibadatik*  
  Listens via speech recognition with diacritic normalization and forgiving fuzzy-matching.
- **Emergency Stop**: 3-second hold button for driving/emergencies (logs prayer as *Unconfirmed*).
- **20-Minute Post-Prayer Follow-Up**: High-priority check-in reminder: *"Did you pray {Prayer}?"* with 3 quick options:
  - ✓ **Yes (on time)**
  - ⏳ **Yes (late)**
  - ✕ **Missed** (automatically adds to your Qaza debt tracker)
- **Streaks & Heatmap Scoring**:
  - Live circular countdown to the next prayer
  - 28-day glowing calendar devotion heatmap
  - Weekly and monthly score percentages
  - Instagram & WhatsApp Status **Streak-Share Card** generator with custom author signature
- **OEM Task-Killer Survival Guide**: In-app instructions for Xiaomi (HyperOS/MIUI), Samsung (One UI), and Oppo/OnePlus.
- **Gen Z Glassmorphic Aesthetics**:
  - Real backdrop blur via Haze
  - Dynamic time-of-day mesh gradients shifting through Fajr, Dhuhr, Asr, Maghrib, and Isha
  - Floating pill glass navigation bar
  - "Reduce effects" switch for low-end device optimization

---

## 🚀 How to Download Your APK (Cloud Build)

Because you do not need the Android SDK installed locally, the APK is built automatically in GitHub Actions on every push or manual run!

### Step 1: Open GitHub Actions
1. Go to your repository page on GitHub: `https://github.com/<YOUR_USERNAME>/salah-keeper`
2. Click on the **Actions** tab at the top.

### Step 2: Select the Build Run
1. Under "All workflows", click on **Build Salah Keeper APK**.
2. Click on the most recent workflow run (marked with a green checkmark `✓`).

### Step 3: Download the APK
1. Scroll down to the bottom of the run page to the **Artifacts** section.
2. Click on **`SalahKeeper-APK`**.
3. A `.zip` file will download to your device containing the ready-to-install `app-release.apk`!
4. Extract the `.zip` and transfer the `.apk` file to your Android phone to sideload.

---

## 🔐 (Optional) Setting Up Production Keystore Signing

By default, the GitHub Actions workflow automatically falls back to debug signing so your APK builds and runs immediately without any setup. 

When you are ready to sign it with your own permanent private keystore, follow these simple steps:

### 1. Generate a Keystore File
Open PowerShell (or Terminal) and run:
```bash
keytool -genkey -v -keystore salahkeeper.jks -keyalg RSA -keysize 2048 -validity 10000 -alias salahkeeper
```
*You will be prompted to create a password (e.g. `MySecurePassword123`) and enter basic details.*

> ⚠️ **CRITICAL WARNING**: Back up your `salahkeeper.jks` file to a secure cloud drive (e.g., Google Drive or encrypted USB). If you lose this keystore, you will not be able to sign updates for your installed app!

### 2. Convert Keystore to Base64 String
Run this command in PowerShell to convert your keystore into a base64 text string:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("salahkeeper.jks")) | Set-Content "keystore_base64.txt"
```
*(On macOS/Linux: `base64 -i salahkeeper.jks -o keystore_base64.txt`)*

### 3. Add GitHub Secrets
1. In your GitHub repository, click **Settings** > **Secrets and variables** > **Actions**.
2. Click **New repository secret** and add the following 4 secrets:
   - `KEYSTORE_BASE64`: Paste the entire contents of `keystore_base64.txt`
   - `KEYSTORE_PASSWORD`: The password you chose for the keystore
   - `KEY_ALIAS`: `salahkeeper`
   - `KEY_PASSWORD`: The password you chose for the key alias

Once added, every new push or manual run will automatically produce an official release-signed APK!

---

## 🛠 Local Development & Testing

If you ever decide to run the project locally with Android Studio:
```bash
# Clone the repository
git clone https://github.com/<YOUR_USERNAME>/salah-keeper.git
cd salah-keeper

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

---

## 📜 Permissions Handled
- `SCHEDULE_EXACT_ALARM` & `USE_EXACT_ALARM`: Ensures millisecond-accurate prayer calls.
- `USE_FULL_SCREEN_INTENT`: Awakes the lock screen with the full glass alarm UI.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Continuous looping adhan audio.
- `RECORD_AUDIO`: On-device speech recognition for reciting the Dua.
- `POST_NOTIFICATIONS`: 20-minute post-prayer follow-up status check.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Prevents OS battery killers from terminating the service.

---

*“Indeed, prayer has been decreed upon the believers a decree of specified times.” — Surah An-Nisa (4:103)*
