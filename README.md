# FindMe — Campus Lost & Found Android Application

**FindMe** is a native Android application designed to help university students and campus staff report, track, and recover lost and found belongings. Built with an offline-first architecture, the application relies entirely on native Android components, local persistence with Room Database, and on-device private storage without any backend or third-party cloud dependencies.

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Architecture-MVVM-009688?style=for-the-badge" alt="MVVM" />
  <img src="https://img.shields.io/badge/Database-Room%20SQLite-4285F4?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room SQLite" />
  <img src="https://img.shields.io/badge/UI-Material%20Design%203-FF7043?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material Design 3" />
  <img src="https://img.shields.io/badge/Async-Coroutines%20%26%20Flow-02569B?style=for-the-badge&logo=kotlin&logoColor=white" alt="Coroutines & Flow" />
  <img src="https://img.shields.io/badge/Storage-Jetpack%20DataStore-F4B400?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack DataStore" />
</p>


## Screen Highlights

| Home Screen | Report Item |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/c2ea1070-1c46-4e5b-93f1-cdbdc2c6a0fd" width="280" alt="Home Screen — Browse & Search" /> | <img src="https://github.com/user-attachments/assets/ac68bedc-1bad-4283-ab4b-5b85ba850c9e" width="280" alt="Report Screen — Submission Form" /> |

| My Reports | Item Details & Completion |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/87358332-eea5-433f-ae60-13eec4e9bd5c" width="280" alt="My Reports — Active & Past" /> | <img src="https://github.com/user-attachments/assets/5aaf5736-bc33-4f95-8d74-bd26807814df" width="280" alt="Item Details — Status & Actions" /> |


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


## Tech Stack & Architecture

| Technology / Component | Role & Usage in FindMe |
| :--- | :--- |
| **Kotlin** | Primary language used for the complete codebase. |
| **MVVM + Repository** | Architectural pattern separating UI controllers, state management, and data access. |
| **Room Database (SQLite)** | On-device persistent relational database storing item reports, statuses, and ownership with schema migrations. |
| **Kotlin Coroutines & Flow** | Asynchronous operations and reactive streams (`StateFlow`, `SharedFlow`) that automatically push live updates to the UI. |
| **Material Design 3 & XML** | Native styling, themes, and Material 3 components (`MaterialCardView`, `TabLayout`, `BottomNavigationView`, `MaterialAutoCompleteTextView`, `MaterialButtonToggleGroup`). |
| **ViewBinding** | Compile-time safe view referencing eliminating boilerplate and `findViewById` calls. |
| **Android Photo Picker (`PickVisualMedia`)** | Privacy-first media picker to select gallery photos without requiring broad storage permissions. |
| **System Camera (`TakePicture`) & FileProvider** | Delegates photo capture to the native camera app and receives images via secure content URIs. |
| **Internal App Storage (`filesDir`)** | Sandboxed local directory (`files/items/`) where picked and captured photos are permanently copied. |
| **Native Bitmap Processing & ExifInterface** | Performs dynamic downsampling (`inSampleSize`) to eliminate `OutOfMemoryError` risk and corrects camera EXIF rotation. |
| **Jetpack DataStore Preferences** | Key-value storage for persistent UI preferences (such as selected filter chips and sample data seeding state). |
| **NotificationManagerCompat** | Delivers local submission confirmation notifications and notification channel management (compatible with Android 13+). |


## What This Project Demonstrates

- **Zero Third-Party Dependency Philosophy**: Proves that complete, modern camera capture, gallery selection, image optimization, notifications, and persistent workflows can be built using purely native Android Jetpack and system APIs.
- **Reactive Single Source of Truth**: Room queries emit Kotlin `Flow` directly through the repository and ViewModel to the UI. Marking an item completed in the detail screen instantly updates Home and My Reports without manual cache invalidation or screen reloads.
- **Production-Grade Media Handling**: Prevents `OutOfMemoryError` crashes by dynamically calculating bitmap sample sizes prior to decoding high-resolution smartphone camera pictures, and corrects sideways camera captures via EXIF orientation matrices.
- **Non-Destructive Database Evolution**: Demonstrates production database migration practices by safely migrating SQLite tables between versions while preserving existing records and photos.
- **Privacy & Offline Resilience**: Ensures user lost-and-found data, contact details, and photos remain on the user's device in sandboxed app-specific storage.


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
