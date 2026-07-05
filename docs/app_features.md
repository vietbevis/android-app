# SnapChi (APK Basic) - App Features

This document outlines all the features and capabilities of the SnapChi personal finance application, as derived from the project documentation and design system.

## 1. Authentication & Onboarding

- **User Authentication**: Sign up, login, and logout functionalities using Supabase Auth.

- **Onboarding Bootstrap**: Automatic creation of a user profile upon first login.

- **Default Setup**: Automatic initialization of a default wallet ("Tiền mặt" - Cash) and default categories (income/expense) for new users.

- **Authentication State Management**: Secure handling of the user's logged-in status across the app.

## 2. Dashboard / Home

- **Monthly Overview**: Summary of the current month's financial income and outcome.

- **Balance Banner**: A prominent banner displaying the amount to spend today and this week.

- **Recent Transactions Feed**: A calendar on which users can click on a day to see the transactions they make on that day (how many transactions, the total of income and outcome, a list of transactions which users can click on to see the details of transactions)

## 3. Camera-First Transaction Capture (Capture Flow)

- **CameraX Integration**: Live camera preview directly within the app for quick receipt capture.

- **Camera Controls**: Options to toggle flash and switch between front and rear cameras.

- **Photo Capture & Preview**: Ability to take a photo of a receipt, preview it, and choose to retake or proceed.

- **Transaction Form**: A streamlined form to enter transaction details (amount, category, wallet, date, notes).

- **Cloud Storage**: Secure upload and storage of receipt photos to Supabase Storage (private bucket, compressed to JPEG).

- **Fallback Mode**: Ability to save a transaction without a photo if the upload fails or the user chooses not to take one.

## 4. Transaction History

- **Transaction List**: Detailed view of all transactions for the current month.

- **Transaction Details**: Expanded cards showing full details of a specific transaction, including the attached receipt photo.

- **Edit/Delete**: Edit or delete transactions.

## 5. Budgets

- **Budget setting**: Set budgets for the month and specific categories in that month

- **Budget Tracking**: An interactive tracking for the total budget in the month and specific categories in that month

- **Budget Alerts**: Color-coded warnings to alert users about their spending pace.

## 6. Statistics and exports

- **Monthly statistics**: Aggregated data showing total income, total expenses for the current month, a pie chart displaying the percentage of each category of both income and outcome, and a goal monthly tracking to keep users from spending over the budget limit

- **Yearly statistics**: Aggregated data showing total income, total expenses for the current year, a pie chart displaying the percentage of each category of both income and outcome, and a display of the successful months of saving, and the trending of spending over the year.

- **Export transactions**: Export transactions in CSV format, which can be shared with others.

## 7. Profile & Settings

- **User Profile**: View and manage user account details.

- **Preferences**: App-wide settings and customization.

## 9. Core System & UI/UX Features

- **Material 3 Design**: Modern, responsive UI based on the "SnapChi" design system (warm cream background, navy text, coral accents).

- **Accessibility (a11y)**: Vietnamese descriptive labels for all icon buttons and screen elements (TalkBack support).

- **Optimized Numeric Input**: Numeric keyboards tailored for financial amount entry.

- **Row-Level Security (RLS)**: Strict data isolation ensuring users can only access their own financial data on Supabase.

- **Offline/Loading States**: Graceful handling of network delays with skeleton loaders, empty states, and user-friendly error messages.
