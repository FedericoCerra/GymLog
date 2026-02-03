#  GymLog, a nice workout tracker!

A modern, feature-rich Android fitness application built with **Jetpack Compose** and **Firebase**. Track your lifts, visualize your progress and hit new Personal Bests.

![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)

##  Features

*   ** Detailed Workout Tracking**: Log sets, reps, and weight with support for different set types (Warmup, Drop sets, Failure).
*   ** Muscle Distribution**: Interactive **Spider Graphs** visualize which muscle groups you've trained over the last 5 weeks.
*   ** Consistency Heatmap**: Track your workout frequency with a visual contribution-style heatmap.
*   ** PR Detection**: Automatic detection of Personal Bests for Max Weight, 1RM, and Total Volume.
*   ** Workout Overlay**: A background service that keeps your rest timer and workout progress visible while using other apps.
*   ** Cloud Sync**: Secure authentication and data storage using **Firebase Auth** and **Firestore**.
*   ** Unit Conversion**: Toggle seamlessly between `kg` and `lbs`.
*   ** Bubble Overlay**: A bubble that stay onscreen whern the workout is ongoing even if the app is in the background.
*   ** Themes**: Color themes!

## 📸 Screenshots

<img width="307" height="683" alt="image" src="https://github.com/user-attachments/assets/9206216f-145d-4508-8458-98688fece917" />
<img width="307" height="683" alt="image" src="https://github.com/user-attachments/assets/084f5d08-cca6-40b1-872d-21f1f9a723f1" />
<img width="307" height="683" alt="image" src="https://github.com/user-attachments/assets/1322d9f6-d9e9-4f2d-a409-c975eee8b706" />


## 🛠 Tech Stack

*   **UI**: Jetpack Compose (Material 3)
*   **Language**: Kotlin
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Backend**: Firebase Auth & Cloud Firestore
*   **Navigation**: Compose Navigation
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Local Storage**: DataStore / Preferences

## 📲 Download

You can download the latest version of the app from the [Releases](https://github.com/yourusername/learningKotlin/releases) page.

1. Download the `.apk` file.
2. Open the file on your Android device.
3. You may need to "Allow installation from unknown sources" in your phone settings.
4. 

*Note for developers: If you are cloning the source code, remember to add your own `google-services.json` to the `/app` folder to enable Firebase features.*

*   **Exercise Database**: The exercise data, images, muscle group mappings, and descriptions are sourced from the [free-exercise-db](https://github.com/yuhonas/free-exercise-db) by [yuhonas](https://github.com/yuhonas).
    
