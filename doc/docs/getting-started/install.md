

# 🚀 Installing zMantra from GitHub using Android Studio

This guide explains **all possible ways** to set up the zMantra project from GitHub into Android Studio.

---

## 1️⃣ Clone via Android Studio (Built-in Git Integration)

1. Open **Android Studio**.
2. On the **Welcome screen**, click **“Get from VCS”** (or **File → New → Project from Version Control** if a project is already open).
3. In the dialog:

   * **Version control**: Select **Git**.
   * **Repository URL**: Paste the zMantra GitHub repo URL:

     ```
     https://github.com/<username>/zMantra.git
     ```
   * Choose the **local directory** to clone into.
4. Click **Clone**.
5. Android Studio will automatically detect the Gradle project and sync dependencies.

✅ This is the **recommended method**.

---

## 2️⃣ Clone via Command Line (Git CLI) + Open in Android Studio

1. Open a terminal.
2. Run:

   ```bash
   git clone https://github.com/<username>/zMantra.git
   ```

   or with SSH (if set up):

   ```bash
   git clone git@github.com:<username>/zMantra.git
   ```
3. Open **Android Studio** → **File → Open**.
4. Select the cloned project folder.
5. Let Gradle sync finish.

---

## 3️⃣ Download ZIP (No Git Required)

If you don’t have Git installed:

1. Go to the GitHub repository page.
2. Click **Code → Download ZIP**.
3. Extract the ZIP file to a folder.
4. Open **Android Studio** → **File → Open** → select the extracted folder.
5. Wait for Gradle sync.

⚠️ Updates require downloading a new ZIP manually.

---

## 4️⃣ Import via GitHub Plugin (If Connected to GitHub Account)

1. In **Android Studio**:

   * Go to **File → Settings → Version Control → GitHub**.
   * Log in with your GitHub account (using **token authentication**).
2. Go to **File → New → Project from Version Control → GitHub**.
3. Search for the `zMantra` repo under your account or organization.
4. Select and **Clone**.

---

## 5️⃣ Fork and Clone (For Contributors)

If you want to contribute:

1. On GitHub, click **Fork** on the `zMantra` repo.
2. Clone your fork:

   ```bash
   git clone https://github.com/<your-username>/zMantra.git
   ```
3. Add the upstream repo for syncing changes:

   ```bash
   git remote add upstream https://github.com/<original-author>/zMantra.git
   ```
4. Open in Android Studio.

---

## 🛠️ After Import – Setup Checklist

* Ensure **Android Studio Bumblebee (2021.1) or newer**.
* Install **Java 17 (Temurin or OpenJDK recommended)**.
* Sync Gradle: **File → Sync Project with Gradle Files**.
* If using **Hilt + KSP**, ensure `ksp` plugin is enabled (Android Studio will auto-install).
* Build the app with:

  ```bash
  ./gradlew assembleDebug
  ```

---

## ✅ Verification

Once setup is complete:

* Run the app on an **emulator** or **physical Android device**.
* Check TalkBack accessibility works.
* Explore modes like **Quick Play**, **Shake**, **Tap**, **Number Line**, etc.

