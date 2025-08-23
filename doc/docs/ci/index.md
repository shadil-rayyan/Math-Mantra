

# Developer Documentation – GitHub Actions: Release Bundle with Tag, Cache, and Version Bump

## 📌 Overview

This workflow automates the **release process** for the Android app.
It performs the following tasks:

1. **Checks out the repository** and sets up the Java environment.
2. **Caches Gradle dependencies** to speed up builds.
3. **Decodes the Keystore** from GitHub Secrets (used for signing builds).
4. **Reads and bumps the app version** in `version.properties`.
5. **Commits version changes, tags the release, and pushes tags**.
6. **Builds both AAB and APK artifacts** (signed).
7. **Uploads artifacts to GitHub Actions** for download.
8. **Creates a GitHub Release** with the artifacts attached.

---

## ⚙️ Workflow File Location

```plaintext
.github/workflows/release.yml
```

---

## 🛠️ Trigger

```yaml
on:
  workflow_dispatch:
```

* This workflow **runs manually** via the GitHub Actions UI.
* Useful for controlled releases (instead of automatic CI/CD builds).

---

## 🔑 Environment Variables / Secrets

The following **GitHub Secrets** must be set in your repository:

| Secret Name            | Description                                                          |
| ---------------------- | -------------------------------------------------------------------- |
| `KEYSTORE_FILE`        | Base64-encoded keystore file.                                        |
| `KEYSTORE_PASSWORD`    | Password for the keystore.                                           |
| `SIGNING_KEY_ALIAS`    | Alias of the signing key.                                            |
| `SIGNING_KEY_PASSWORD` | Password for the signing key.                                        |
| `GITHUB_TOKEN`         | Default GitHub Actions token (used for commits, tags, and releases). |

---

## 🚀 Steps Breakdown

### 1. Checkout Code

```yaml
- name: Checkout code
  uses: actions/checkout@v4
  with:
    fetch-depth: 0  # Required for tagging
```

* Fetches the entire repo (not shallow), so tags and history are available.

---

### 2. Set Up JDK

```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '17'
```

* Configures Java **17 (Temurin distribution)** for Gradle builds.

---

### 3. Setup Gradle Cache

```yaml
- name: Setup Gradle cache
  uses: gradle/actions/setup-gradle@v3
```

* Speeds up builds by caching Gradle dependencies.

---

### 4. Decode Keystore

```yaml
- name: Decode Keystore
  run: echo "$KEYSTORE_FILE" | base64 -d > app/playstore.keystore.jks
```

* Converts the base64 keystore secret into a physical `.jks` file required for signing.

---

### 5. Read and Bump Version

```yaml
- name: Read and Bump Version
  id: versioning
  run: |
    FILE=version.properties
    VERSION=$(grep VERSION_NAME $FILE | cut -d '=' -f2)
    ...
```

* Reads `VERSION_NAME` from `version.properties`.
* Bumps **patch version** (e.g., `1.2.3 → 1.2.4`).
* Commits changes to `main`.
* Creates a **Git tag** `vX.Y.Z`.
* Pushes commit + tags to origin.

> ⚠️ This modifies your repo during workflow execution, so version history is always updated alongside releases.

---

### 6. Build AAB & APK

```yaml
./gradlew bundleRelease ...
./gradlew assembleRelease ...
```

* Builds signed **App Bundle (.aab)** and **APK (.apk)**.
* Uses keystore + signing configs from environment variables.

---

### 7. Upload Artifacts

```yaml
- name: Upload AAB Artifact
  uses: actions/upload-artifact@v4
  with:
    name: release-aab
    path: app/build/outputs/bundle/release/*.aab
```

* Makes AAB and APK files available for download in GitHub Actions.

---

### 8. Create GitHub Release

```yaml
- name: Create GitHub Release
  uses: softprops/action-gh-release@v2
  with:
    tag_name: v${{ steps.versioning.outputs.version }}
    name: Release v${{ steps.versioning.outputs.version }}
```

* Publishes a **new GitHub Release** with:

  * Correct **tag** (`vX.Y.Z`).
  * **Release notes title** (e.g., `Release v1.2.4`).
  * Attached **APK and AAB artifacts**.

---

## 📂 Artifacts

After a successful run, you will find:

* **In GitHub Actions (workflow run)** →

  * `release-aab`: App Bundle for Play Store.
  * `release-apk`: Signed APK.
* **In GitHub Releases (repository)** →

  * APK + AAB files attached to the tagged release.

---

## 🔮 Future Improvements

* Add **automatic changelog generation** from commit messages.
* Support **version bump strategies** (major, minor, patch).
* Upload artifacts directly to **Google Play Console** (using [gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher)).
* Add **matrix builds** (different flavors/variants).

