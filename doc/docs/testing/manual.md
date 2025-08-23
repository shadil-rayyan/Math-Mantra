# Manual Testing Guide

Manual testing ensures that all features of **z.Mantra** work as expected by simulating real user interactions.  
This document provides a template for recording test cases, execution results, and defects.  

---

## How to Perform Manual Testing

1. **Understand the Feature**  
   - Review the feature or module you are testing.  
   - Check acceptance criteria and requirements.  

2. **Prepare the Environment**  
   - Install the latest APK build of the project.  
   - Ensure device/emulator matches the required Android version.  
   - Verify any prerequisites (e.g., Excel questions loaded, internet connection, accessibility enabled).  

3. **Execute Test Cases**  
   - Follow the defined **test steps** one by one.  
   - Record whether the outcome matches the **expected results**.  

4. **Log Defects (if any)**  
   - If actual result ≠ expected result, mark the status as **Fail**.  
   - Capture screenshots or videos and link them in the **Defect Artifact URL** column.  
   - Assign a **Defect ID** (to track in issue tracker / GitHub Issues / Jira).  

---

## Test Case Template (Markdown Table)

| Sl. No | Test ID | Test Type | Test Case Name | Priority | Feature | Prerequisites | Test Steps | Expected Results | Status | Actual Result | Remarks | Defect Artifact URL (Image / Video) | Defect ID |
|--------|---------|-----------|----------------|----------|---------|---------------|------------|------------------|--------|---------------|---------|-------------------------------------|-----------|
| 1      | TC001   | Functional | Verify app launches successfully | High | App Launch | APK installed | Open the app | App should launch without crash | Pass | Works as expected | - | - | - |
| 2      | TC002   | Accessibility | Check TalkBack reads menu items | High | Accessibility | Enable TalkBack | Navigate through main menu | TalkBack should announce all items clearly | Fail | TalkBack skips one item | Needs fix | [Video Link](https://example.com) | BUG-001 |

---

## Attaching Excel Sheet

If you prefer to manage test cases in **Excel** (recommended for large projects):  

- Upload the Excel sheet to your repository (e.g., in `docs/testing/Manual_Test_Cases.xlsx`).  
- Or use Google Sheets and provide a shareable link.  

Example:  

- [📄 Download Manual Test Cases (Excel)](./testing/Manual_Test_Cases.xlsx)  
- [🔗 Google Sheet Link](https://docs.google.com/spreadsheets/d/your-link-here)  


