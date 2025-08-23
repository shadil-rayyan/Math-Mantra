

# 📖 Developer Documentation – Docs Deployment

This project uses **MkDocs** with the **Material for MkDocs** theme to generate and publish documentation automatically to **GitHub Pages**.

## 🔄 Workflow Overview

The GitHub Actions workflow **`Docs`** is responsible for:

1. **Building documentation** from the `doc/` folder.
2. **Uploading an artifact** of the built site.
3. **Deploying** the docs to **GitHub Pages** when changes are pushed to `main` or `dev`.

---

## ⚙️ Workflow Configuration

```yaml
name: Docs

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: ${{ github.ref != 'refs/heads/dev' }}

on:
  workflow_dispatch:
  push:
    branches:
      - main
      - dev

permissions:
  contents: read
  packages: write
  pages: write
  id-token: write

jobs:
  Deploy:
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}

    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - uses: actions/setup-python@v5
        with:
          python-version: '3.x'

      - name: Generate Docs
        run: |
          set -ex
          cd ./doc
          python3 -m venv venv
          source venv/bin/activate
          pip install --upgrade pip
          pip install mkdocs mkdocs-material mkdocs-redirects
          mkdocs build --clean
          deactivate

      - name: Upload documentation zip archive
        uses: actions/upload-artifact@v4
        with:
          name: moove-android-docs
          path: doc/site

      - name: Setup Pages
        uses: actions/configure-pages@v4

      - name: Upload Pages Artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: './doc/site'

      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
```

---

## 📂 Project Documentation Structure

```
project-root/
│── doc/
│   ├── mkdocs.yml        # MkDocs config file
│   ├── docs/             # Markdown source files
│   └── site/             # Auto-generated build output
```

* All **Markdown docs** live in `doc/docs/`.
* The **MkDocs config** (`mkdocs.yml`) defines navigation, theme, and plugins.
* The **`site/`** folder is generated automatically — do **not** commit it.

---

## 🚀 Deployment Process

* On every push to **`main`** or **`dev`**, the workflow:

  1. Builds the docs with MkDocs.
  2. Deploys the output to **GitHub Pages**.
* Manual deployment is also possible using the **workflow\_dispatch** trigger from GitHub UI.

---

## 📎 How Developers Can Contribute to Docs

1. Add or update documentation in `doc/docs/` using Markdown.
2. Commit your changes and push to `main` or `dev`.
3. GitHub Actions will automatically rebuild and deploy docs.
4. Visit the published docs at:


