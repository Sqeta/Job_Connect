# JobConnect

## About the Project

JobConnect is an Android mobile application designed to make searching and applying for jobs easier and more organised. The application gives job seekers one place where they can search for opportunities, read job descriptions, save vacancies and keep track of their applications.

The application was developed by Karabo Mohapi as part of the APPR6312 Mobile Application Development Portfolio of Evidence at Rosebank College. It demonstrates the practical use of Kotlin, Android Studio, Firebase, Room, Retrofit, WorkManager, automated unit testing and GitHub Actions.

## The Problem

Searching for employment can become difficult because vacancies are normally spread across many different websites. Job seekers may find several interesting opportunities but later forget where they found them or whether they have already applied.

Managing application progress can also become confusing, especially when a person applies for several positions. It may be difficult to remember which applications are still being reviewed, which have reached the interview stage and which have already been completed.

Another challenge is that most job-search platforms depend entirely on an internet connection. When users lose connectivity, they may not be able to access job information they previously saved. JobConnect was created to bring these activities together and provide limited offline access to important information.

## The JobConnect Solution

JobConnect provides users with a simple platform for managing different parts of their employment journey. A user can register an account, sign in securely and search for live vacancies using a job title, skill or location.

The application allows users to open a vacancy, read its information, save it for later and visit the original website when they are ready to apply. Saved jobs are associated with the signed-in account, which ensures that every user has their own private list.

JobConnect also includes an application tracker, a CV checklist, achievement badges, a personal profile and job-alert preferences. These features make the application more than a basic job-search tool because they help users prepare for opportunities and remain organised throughout the application process.

## Registration and Login

New users can create an account by providing their personal information, email address and password. Firebase Authentication is used to create and protect the account, while additional user information is stored in Cloud Firestore.

Registered users can sign in using their email address and password. If a user forgets their password, they can request a password-reset email. The application checks whether a valid user is signed in before opening pages containing personal information.

After signing in, the user is taken to the home page, where a personalised greeting is displayed. The greeting changes according to the time of day and uses the name available on the user’s profile.

## Searching for Jobs

JobConnect uses the Adzuna Jobs API to retrieve live employment opportunities. Users enter a job title or skill and then provide a city or province. The application sends this information to Adzuna and displays the matching results.

Each result can include the job title, company name, location, contract type and vacancy description. If no results are available, the application displays a helpful message instead of leaving the page empty. It also informs the user when a request cannot be completed because of a connection problem.

When a user selects a result, the job-details page displays more information about the opportunity. The user can save the job or open the original vacancy website to continue with the application process.

## Saved Jobs and Offline Access

Users can save interesting opportunities to their accounts. These jobs are stored in Cloud Firestore under the unique ID of the signed-in user, which prevents information from different accounts from being mixed together.

JobConnect also uses Room to keep a local copy of saved jobs on the Android device. When the application successfully retrieves saved jobs from Firestore, the information is added to the local Room database.

If the internet connection becomes unavailable, the application can display jobs that were previously cached. This does not make every feature fully available offline, but it allows users to access important saved information without using mobile data.

The local records were successfully verified using Android Studio’s Database Inspector. The inspector displayed the saved-job information stored inside the application’s local database.

## Application Tracker

The application tracker helps users organise the progress of their applications. Instead of trying to remember every vacancy, users can record applications and view their current stages in one place.

An application can move through stages such as saved, applied or interview. This gives users a clearer view of their employment journey and helps them identify applications that may require follow-up.

Application information is saved under the authenticated user’s Firestore account. This keeps each person’s records private and separate from the information belonging to other users.

## CV Checklist

The CV checklist helps users prepare before applying for employment. It focuses on important areas such as contact details, education, skills and projects.

As the user completes these sections, JobConnect calculates their progress and stores the result in Firestore. The checklist allows users to see which parts have been completed and which still require attention.

This feature is useful for students and graduates who may not be certain whether they have included all the important information needed for a professional CV.

## Achievements

JobConnect includes achievements to encourage users to complete important activities. Instead of displaying achievements randomly, the application checks the user’s actual information in Firestore.

A user can unlock the Profile Starter achievement by completing enough of the CV checklist. The First Application achievement is unlocked after recording an application, while the Consistent Searcher achievement recognises users who save several job opportunities.

This feature adds motivation and allows users to see the progress they have made while using the application.

## Profile Management

The profile page displays personal and career-related information belonging to the signed-in user. This includes the user’s name, email address, education, skills, preferred locations and preferred job types.

Users can edit this information through the profile-editing feature. Once the changes have been saved, the Firestore user document is updated and the new information appears on the profile page.

Keeping this information in one place allows JobConnect to provide a more personalised experience and creates a foundation for improved job recommendations in future versions.

## Settings and Job Alerts

The settings page allows users to manage application preferences and sign out of their accounts. Users can select a preferred language and choose whether job-alert notifications should be enabled.

WorkManager is used to schedule background job-alert tasks. This Android component is suitable because it can manage work that should continue reliably even after the user leaves the application.

When job alerts are enabled, JobConnect schedules background work and can display a job-search reminder. When the user disables alerts, the scheduled background work is cancelled.

The selected language is currently stored as part of the user’s settings. Complete translation of every application screen will be introduced as a future improvement.

## Technologies Used

JobConnect was developed using Kotlin and Android Studio. XML was used to design the application screens, while AndroidX and Material Components provided the main interface elements.

Firebase Authentication manages registration, login, password recovery and sign-out. Cloud Firestore stores online information such as user profiles, saved jobs, CV progress, settings and application records.

Room provides the local database used to cache saved jobs. Retrofit connects the application to the Adzuna Jobs API, while Gson converts the returned information into Kotlin objects that can be displayed inside the application.

RecyclerView is used to display dynamic lists of jobs and applications. WorkManager manages background tasks, JUnit supports automated testing and GitHub Actions automatically tests and builds the project when changes are uploaded.

## Adzuna Jobs API

The Adzuna Jobs API supplies the vacancy information displayed by JobConnect. When a search is performed, the application sends the selected keyword and location together with the required Adzuna credentials.

Retrofit manages the request and receives the response. The returned information is converted into Kotlin job objects and passed to the adapter responsible for displaying the results.

For security reasons, the Adzuna Application ID and Application Key are not written directly inside the Kotlin source code. They are placed in the local `local.properties` file and accessed through generated `BuildConfig` values.

## Firebase and Data Storage

Firebase Authentication is responsible for securely managing user accounts. Passwords are handled by Firebase and are not stored directly by JobConnect inside Firestore.

Cloud Firestore stores the online information used by the application. Every user document is identified by the unique ID provided by Firebase Authentication. The user’s saved jobs, applications and CV progress are stored under that user document.

Firestore security rules ensure that users can access only their own information. A signed-in user cannot read or modify the personal records belonging to another account.

Room works alongside Firestore by storing selected information on the Android device. Firestore remains the main online database, while Room provides a local cache for previously saved jobs.

## Security

Security was considered throughout the development of JobConnect. Firebase Authentication protects account access, while Firestore rules restrict users to their own documents and collections.

The application checks that a user is authenticated before displaying private pages. Important input is validated before it is processed, and passwords are never stored directly in the application database.

Sensitive local information, including API credentials, should be excluded from Git version control. This prevents private values from being exposed when the project is uploaded to GitHub.


## Automated Testing

Automated unit testing was added to confirm that the application’s input-validation functions behave correctly. These tests cover valid and invalid names, email-address formats, passwords, password confirmation and blank input.

A total of 16 automated unit tests were created and all 16 tests passed successfully. This provides evidence that the tested validation functions produce the expected results for both acceptable and unacceptable information.


<img width="940" height="529" alt="image" src="https://github.com/user-attachments/assets/2a6a5835-2a0f-4243-be39-a56a44672648" />


<img width="940" height="473" alt="image" src="https://github.com/user-attachments/assets/3b7b4090-5e62-46be-8d43-99d6c13c2939" />


