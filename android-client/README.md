\# Android Client



This folder contains the native Android client for the COMP713 Task Management System.



\## Main Features



The Android client supports:



\- Create a new task

\- Load tasks from the backend

\- Display task details

\- Update task status from TODO to DONE

\- Validate empty task input

\- Show an error message when the backend is unavailable

\- Show Android system notifications when a task is created

\- Show Android system notifications when a task is completed



\## Requirements



\- Android Studio

\- Android SDK

\- Android emulator

\- Backend server running on port 8080

\- MySQL database running



\## Backend Connection



The Android client connects to:



```text

http://localhost:8080

```



When using the Android emulator, ADB reverse is required.



Run:



```powershell

\& "$env:LOCALAPPDATA\\Android\\Sdk\\platform-tools\\adb.exe" reverse tcp:8080 tcp:8080

```



To confirm the emulator is connected:



```powershell

\& "$env:LOCALAPPDATA\\Android\\Sdk\\platform-tools\\adb.exe" devices

```



Example:



```text

emulator-5554    device

```



To check the reverse connection:



```powershell

\& "$env:LOCALAPPDATA\\Android\\Sdk\\platform-tools\\adb.exe" reverse --list

```



\## Running the Android App



1\. Start the MySQL database.

2\. Start the backend server.

3\. Start the Android emulator.

4\. Run the ADB reverse command.

5\. Open the `android-client` folder in Android Studio.

6\. Run the app on the emulator.



\## Building the Android App



Open PowerShell in the `android-client` folder:



```powershell

cd C:\\Users\\13032\\Desktop\\task-management-system\\android-client

```



Run:



```powershell

.\\gradlew.bat assembleDebug

```



The Android project has been tested successfully with Gradle.



Expected result:



```text

BUILD SUCCESSFUL

```



\## Basic Test Workflow



A normal Android test workflow is:



1\. Enter a task title.

2\. Enter a task description.

3\. Select `Create Task`.

4\. A `Task Created` Android notification should appear.

5\. Select `Load Tasks`.

6\. The new task should appear with status `TODO`.

7\. Select `Mark Completed`.

8\. A `Task Completed` Android notification should appear.

9\. Select `Load Tasks` again.

10\. The task status should now show `DONE`.



\## Input Validation



If the task title or description is empty, the Android client displays:



```text

Please enter a title and description.

```



The task is not submitted until the required fields are entered.



\## Backend Failure Handling



If the backend is unavailable and the user selects `Load Tasks`, the Android client displays:



```text

Unable to connect to server.

```



The application remains running and does not crash.



\## Notifications



The Android client uses system notifications as a mobile-specific feature.



Notifications are shown when:



\- A task is successfully created

\- A task is successfully marked as DONE



On newer Android versions, notification permission must be allowed when the app first starts.



\## Current Test Configuration



The Android prototype currently uses:



```text

User ID: 1

```



This is used for local testing with the backend API.



\## Known Limitations



\- The Android client currently uses a fixed user ID for local testing.

\- The Android client focuses on the main task workflow only.

\- The backend and MySQL database must be running for shared task operations.

\- ADB reverse is required when using the Android emulator with `localhost:8080`.



\## Main Android File



The main Android implementation is located at:



```text

app/src/main/java/com/comp713/taskmanagement/MainActivity.kt

```

