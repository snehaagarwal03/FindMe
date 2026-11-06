# FindMe — Campus Lost & Found Android Application

**FindMe** is a native Android application designed to help university students and campus staff report, track, and recover lost and found belongings. Built with an offline-first architecture, the application relies entirely on native Android components, local persistence with Room Database, and on-device private storage without any backend or third-party cloud dependencies.

---

## Screen Highlights

| Home Screen | Report Item |
| :---: | :---: |
| <img src="screenshots/home.png" width="280" alt="Home Screen — Browse & Search" /><br><sub><b>Browse, Search & Filter Items</b></sub> | <img src="screenshots/report.png" width="280" alt="Report Screen — Submission Form" /><br><sub><b>Item Reporting & Photo Capture</b></sub> |

| My Reports | Item Details & Completion |
| :---: | :---: |
| <img src="screenshots/my_reports.png" width="280" alt="My Reports — Active & Past" /><br><sub><b>Personal Reports (Active / Past)</b></sub> | <img src="screenshots/item_detail.png" width="280" alt="Item Details — Status & Actions" /><br><sub><b>Item Details & Completion Workflow</b></sub> |

> **Note**: Place your exported screen captures inside a `screenshots/` directory at the project root named `home.png`, `report.png`, `my_reports.png`, and `item_detail.png`.

---

## Key Features

- **Lost & Found Reporting**: Create listings categorized into Electronics, Bags, Accessories, Documents/ID, Books/Stationery, Clothing, or custom "Other" categories.
- **Mandatory Photo Capture & Selection**:
  - **Camera Capture**: Native system camera integration via `ActivityResultContracts.TakePicture` and Android `FileProvider`.
  - **Photo Picker**: Android Photo Picker via `ActivityResultContracts.PickVisualMedia` with zero storage permissions required.
  - **On-Device Storage**: Picked and captured images are securely copied to app-private internal storage (`filesDir/items/`).
- **Report Completion Workflow**:
  - Report owners can mark their active listings as completed from the Item Detail screen.
  - Contextual action prompts:
    - **LOST items**: *"I have found this item now."*
    - **FOUND items**: *"This item has been handed over to the owner."*
  - Confirmation dialog protects against accidental completions.
  - Completed items automatically disappear from the public Home feed and move into **My Reports → Past**.
- **Reactive Search & Filtering**: Instant search across item titles, descriptions, locations, and categories, combined with quick filters for **All**, **Lost**, and **Found** items.
- **Personal Report Hub**: Dual-tab view (**Active** vs. **Past**) allowing users to manage current submissions and review recovery history.
- **Local Notifications**: Dispatches an on-device Android notification upon successful report submission (with Android 13+ `POST_NOTIFICATIONS` runtime permission handling).
- **Persistent Preferences**: Saves user filter settings across sessions using Jetpack DataStore Preferences.

---

## Tech Stack & Architecture

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **UI & Layouts**: Native XML layouts, Material Design 3 (`MaterialCardView`, `TabLayout`, `BottomNavigationView`, `MaterialAutoCompleteTextView`, `MaterialButtonToggleGroup`), ViewBinding
- **Local Persistence**: Jetpack Room Database (SQLite) with non-destructive schema migrations
- **Preferences**: Jetpack DataStore Preferences
- **Asynchronous / Concurrency**: Kotlin Coroutines & reactive `Flow` (`StateFlow`, `SharedFlow`)
- **System Integration & Media**:
  - Android Photo Picker (`ActivityResultContracts.PickVisualMedia`)
  - Camera Capture (`ActivityResultContracts.TakePicture`)
  - Android `FileProvider` for secure content URI sharing
  - Native `BitmapFactory` sample sizing & `android.media.ExifInterface` rotation handling
  - `NotificationManagerCompat` & Notification Channels

---

## What We Have Done

1. **Stage 1 — Core UI & Navigation**:
   - Built `MainActivity` hosting a single-activity container with `BottomNavigationView`.
   - Developed `HomeFragment`, `ReportFragment`, `MyReportsFragment`, and `ItemDetailActivity`.
   - Integrated full edge-to-edge support and soft-keyboard window insets (`WindowInsetsCompat.Type.ime()`) to keep bottom navigation and input fields accessible.
2. **Stage 2 — Native Photo Selection & Image Pipeline**:
   - Integrated Android Photo Picker and camera capture via `FileProvider`.
   - Enforced mandatory photo attachment validation on report submissions.
   - Built memory-efficient downsampled bitmap decoding (`inSampleSize`) and EXIF orientation correction using strictly standard Android APIs (no Glide, Picasso, or Coil).
3. **Stage 3 — Persistence with Room Database**:
   - Replaced in-memory sample lists with persistent SQLite storage using Room.
   - Designed `ItemEntity`, `ItemDao`, and `ItemRepository` with reactive `Flow` queries.
   - Seeded initial demonstration items on first launch using DataStore-backed initialization flags.
4. **Stage 4 — Local Notifications**:
   - Created `NotificationHelper` and a dedicated notification channel for report confirmation alerts.
   - Handled Android 13+ `POST_NOTIFICATIONS` runtime permission requests gracefully.
5. **Stage 5 — Report Completion Workflow & Schema Migration**:
   - Added `isCompleted` tracking to `ItemEntity` with non-destructive Room migration (`MIGRATION_1_2`).
   - Implemented owner-only completion checkboxes, confirmation dialogs, and non-editable completed status cards in `ItemDetailActivity`.
   - Updated `HomeFragment` to display active listings only, and integrated **Active** / **Past** tabs in `MyReportsFragment`.

---

## What This Project Demonstrates

- **Zero Third-Party Dependency Philosophy**: Proves that complete, modern camera capture, gallery selection, image optimization, notifications, and persistent workflows can be built using purely native Android Jetpack and system APIs.
- **Reactive Single Source of Truth**: Room queries emit Kotlin `Flow` directly through the repository and ViewModel to the UI. Marking an item completed in the detail screen instantly updates Home and My Reports without manual cache invalidation or screen reloads.
- **Production-Grade Media Handling**: Prevents `OutOfMemoryError` crashes by dynamically calculating bitmap sample sizes prior to decoding high-resolution smartphone camera pictures, and corrects sideways camera captures via EXIF orientation matrices.
- **Non-Destructive Database Evolution**: Demonstrates production database migration practices by safely migrating SQLite tables between versions while preserving existing records and photos.
- **Privacy & Offline Resilience**: Ensures user lost-and-found data, contact details, and photos remain on the user's device in sandboxed app-specific storage.

---

## Project Structure

```text
com.example.findme/
├── data/
│   ├── db/                 # Room Database, ItemEntity, ItemDao, Converters
│   ├── preferences/        # Jetpack DataStore preference management
│   └── repository/         # ItemRepository bridging DAO and ViewModel
├── model/                  # Domain models (ItemModel, ItemType, ItemCategory, SampleData)
├── ui/
│   ├── adapter/            # RecyclerView ItemAdapter
│   ├── detail/             # ItemDetailActivity
│   ├── home/               # HomeFragment (Search & Filter feed)
│   ├── myreports/          # MyReportsFragment (Active vs. Past tabs)
│   ├── report/             # ReportFragment (Item creation form)
│   └── viewmodel/          # FindMeViewModel
├── utils/                  # FileUtils, ImageUtils, NotificationHelper
├── FindMeApplication.kt    # Application class (channel setup & database seeding)
└── MainActivity.kt         # Main container with BottomNavigationView
```

---

## Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/<your-username>/FindMe.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio (Ladybug / Meerkat or newer recommended).
   - Select **Open** and navigate to the cloned project folder.
3. **Build & Run**:
   - Ensure a device or emulator running **API 26 (Android 8.0)** or higher is selected.
   - Click **Run 'app'** (`Shift + F10`).

