# JobConnect

**Karabo Mohapi – ST10452934**  
**Letlotlo Kwadi – ST10453490**  
**Sabelo Dlomo – ST10436196**

## Video Link
https://advtechonline-my.sharepoint.com/:v:/r/personal/st10453490_rcconnect_edu_za/_layouts/15/stream.aspx?id=%2Fpersonal%2Fst10453490_rcconnect_edu_za%2FDocuments%2FWhatsApp%20Video%202026-11-05%20at%2018.35.32.mp4&share=cQqSU9r3IzSBQbcBYhl_OlZBEgUCvkeiHOf8GEPgr46iXGhtnQ&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0 


## About the Project

JobConnect is an Android job-search application designed to help job seekers find employment opportunities and manage their job-seeking journey from one platform.

Users can create an account, sign in securely, search for live vacancies, view job details, save opportunities and track applications. The application also includes profile management, a CV checklist, achievements, multilingual support, push notifications and offline synchronisation.

JobConnect was developed for the APPR6312 Mobile Application Development Portfolio of Evidence at Rosebank College.

## The Problem

Employment opportunities are often spread across different websites. Job seekers may save several vacancies but later forget where they found them or whether they have already applied.

Many job-search platforms also depend completely on an internet connection. This makes it difficult for users to perform important actions when their connection is unavailable.

JobConnect addresses these problems by combining job searching, saved opportunities, application tracking and career-preparation tools in one application.

## Main Features

- Email and password registration and login
- Google Single Sign-On
- Firebase password recovery
- Live job searching through the Adzuna Jobs API
- Job details and external application links
- Saved-job management
- Offline job saving using Room
- Automatic synchronisation after reconnection
- Application tracker
- CV checklist and progress tracking
- Achievement badges
- Profile viewing and editing
- English, isiZulu and Sesotho support
- Firebase Cloud Messaging push notifications
- WorkManager job-alert reminders
- Firebase Firestore cloud storage
- Automated unit testing
- GitHub Actions continuous integration
- Signed APK and Android App Bundle

## Authentication

Firebase Authentication manages account registration, login, password recovery and session management.

Users can sign in using:

- Email and password
- Google Single Sign-On

Additional profile information is stored in Cloud Firestore under the authenticated user’s unique Firebase ID. The application checks that a valid user is signed in before displaying private information.

## Job Searching

JobConnect uses the Adzuna Jobs API to retrieve live employment opportunities.

Users can search using:

- A job title
- A skill
- A city
- A province

Retrofit sends the request to Adzuna, while Gson converts the returned JSON information into Kotlin objects. Results are displayed using RecyclerView.

A job result can include its title, company, location, contract type and description. Selecting a result opens the job-details page, where the user can save the opportunity or visit the original application website.

## Saved Jobs and Offline Synchronisation

Saved jobs are stored online under the signed-in user’s Firestore account. Room is also used as the local Android database.

When a user saves a job while offline, JobConnect:

1. Stores the job in the local Room database.
2. Marks the record as waiting for synchronisation.
3. Schedules an `OfflineJobSyncWorker`.
4. Waits until an internet connection is available.
5. Uploads the pending job to Cloud Firestore.
6. Marks the local record as synchronised.

This allows an important action to be completed without an active internet connection and synchronised automatically after reconnection.

## Application Tracker

The application tracker helps users monitor the progress of their job applications.

Applications are stored under the authenticated user’s Firestore account and can move through different stages, including applied and interview stages.

This gives users a clearer view of their employment journey and reduces the need to remember every application manually.

## CV Checklist

The CV checklist helps users prepare a more complete professional CV.

It focuses on areas such as:

- Contact information
- Education
- Skills
- Projects
- Employment-related information

The user’s progress is calculated and stored in Firestore. This makes it easier to identify incomplete sections before applying for employment.

## Achievements

JobConnect includes achievement badges that are unlocked using the user’s actual progress.

Examples include:

- Completing enough of the CV checklist
- Recording a first application
- Saving multiple job opportunities

The achievements encourage users to remain active and complete important job-seeking tasks.

## Profile Management

The profile page displays the user’s:

- Full name
- Email address
- Education
- Skills
- Preferred locations
- Preferred job types

Users can edit their information, and the updated values are saved in Cloud Firestore.

## Multilingual Support

JobConnect contains real translated content in three languages:

- English
- isiZulu
- Sesotho

The selected language is applied using Android application locales and retained for later sessions. Important screens such as login, registration, home, search, saved jobs, profile and settings use Android string resources so that their text can change according to the selected language.

## Notifications

JobConnect uses two notification approaches.

### Firebase Push Notifications

Firebase Cloud Messaging is used to deliver real-time push notifications to the Android device. The application retrieves an FCM registration token and uses a custom `FirebaseMessagingService` to receive and display messages.

### WorkManager Reminders

WorkManager schedules reliable background job-alert reminders according to the user’s notification preference.

Firebase Cloud Messaging handles remote push messages, while WorkManager handles scheduled background reminders.

## Technologies Used

- Kotlin
- Android Studio
- Firebase Authentication
- Google Sign-In
- Cloud Firestore
- Firebase Cloud Messaging
- Room Database
- WorkManager
- Retrofit
- Gson
- RecyclerView
- Adzuna Jobs API
- JUnit
- GitHub Actions
- Git and GitHub

## Project Configuration

### Requirements

- Android Studio
- Android SDK 36
- Minimum Android SDK 24
- Java 11
- Firebase project
- Adzuna developer credentials

### Adzuna API Configuration

The Adzuna Application ID and Application Key are not written directly into the Kotlin source code.

### Firebase Configuration

To connect the application to Firebase:

1. Create or open the Firebase project.
2. Register an Android application using:

   `com.example.job_connect`

3. Download `google-services.json`.
4. Place it inside the `app` directory.
5. Enable Email/Password Authentication.
6. Configure Google Sign-In.
7. Add the required SHA-1 certificate fingerprint.
8. Enable Cloud Firestore.
9. Enable Firebase Cloud Messaging.
10. Sync the Gradle project.

## Security

JobConnect uses Firebase Authentication to protect account access. User passwords are managed by Firebase and are not stored directly inside Cloud Firestore.

Firestore information is organised under each authenticated user’s unique ID. Security rules should allow users to read and modify only their own records.

The application validates important input before processing it. Sensitive credentials are read from local configuration and should not be committed to version control.

## Automated Testing

JobConnect includes 16 JUnit unit tests for the `InputValidator` utility.

The tests cover:

- Valid and invalid names
- Valid and invalid email addresses
- Password validation
- Password confirmation
- Blank input
- Comma-separated text conversion
- Removal of blank values
- Removal of duplicate values

All 16 unit tests passed successfully.

Tests can be executed with:

```powershell
.\gradlew.bat testDebugUnitTest
```

## GitHub Actions

The repository contains a GitHub Actions workflow named **JobConnect Android CI**.

The workflow runs automatically when project changes are pushed to GitHub. It checks out the source code, prepares the Java and Android build environment, runs the automated tests and builds the Android project.

Successful workflow runs provide evidence that the project can be tested and built consistently outside the local development environment.

## Evidence

### User Profile

<img width="940" height="467" alt="User profile stored in Firestore" src="https://github.com/user-attachments/assets/e6644004-8b9c-4610-9024-600682aa994c" />

### Saved Jobs

<img width="940" height="477" alt="Saved jobs stored in Firestore" src="https://github.com/user-attachments/assets/bf76df22-8800-4a18-9bdc-228398769d6e" />

### Applications

<img width="940" height="477" alt="Applications stored in Firestore" src="https://github.com/user-attachments/assets/5b98cc02-c324-457a-af84-aef06ce78e86" />

### CV Progress

<img width="940" height="477" alt="CV progress stored in Firestore" src="https://github.com/user-attachments/assets/a5c7933c-d692-4250-be47-83be64b70fb4" />

### Room Database Inspector

<img width="940" height="529" alt="Room database records" src="https://github.com/user-attachments/assets/d9697eac-9f90-4ffd-9de7-5dd7ff912975" />

### Passing Unit Tests

<img width="940" height="529" alt="image" src="https://github.com/user-attachments/assets/1e2a08b6-66aa-44ae-9e02-e6a6414e136c" />


### GitHub Actions

<img width="940" height="529" alt="image" src="https://github.com/user-attachments/assets/4e4afb69-1900-4a0d-a116-ffec926a3c5e" />

## Release Preparation

JobConnect has been prepared for release by generating:

- A signed release APK for Android installation and testing
- A signed Android App Bundle for Google Play Console
- Play Console developer-account preparation evidence
- Automated test evidence
- GitHub Actions build evidence

The application has not been publicly published to Google Play. The Play Console preparation demonstrates that the required release information and Android App Bundle are ready.

## Release Notes

### JobConnect Final PoE Release – Version 1.0

This release represents the completed JobConnect Portfolio of Evidence application.

The final release introduces:

- Google Single Sign-On
- Firebase email and password authentication
- Password recovery
- Live Adzuna job searches
- Saved-job management
- Offline saving using Room
- Automatic synchronisation using WorkManager
- Application tracking
- CV progress tracking
- Achievement badges
- Profile management
- English, isiZulu and Sesotho content
- Firebase Cloud Messaging push notifications
- Background job-alert reminders
- Sixteen passing unit tests
- GitHub Actions continuous integration
- A signed APK
- A signed Android App Bundle
- Google Play Console preparation

The final version was tested on a physical Android device to confirm that the main features operate correctly.

