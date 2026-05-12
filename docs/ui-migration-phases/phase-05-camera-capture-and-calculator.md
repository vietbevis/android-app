# Phase 05 - Camera Capture And Calculator

## Goal

Upgrade capture from a simple form into the reference-style full-screen capture and calculator flow.

## Current State

- `CaptureScreen` supports CameraX permission, preview, flash, lens switch, captured photo preview, amount text input, type radio buttons, wallet/category chips, note, current timestamp, save, and save without photo.
- There is no gallery import, calculator keypad, date picker, polished full-screen camera UI, or modal launch from FAB.

## Target UX

- Initial capture screen is full-screen dark camera with cancel, flash, camera switch, 1x/2x selector, shutter ring, gallery import, and skip photo.
- After photo or skip, user enters amount through a calculator keypad.
- Amount panel overlays the photo preview and shows signed amount, currency, and a detail field.
- Category, account, income/expense toggle, and date are compact chips.
- Save action is prominent and fast; validation errors are inline and never lose photo/input state.

## Implementation Tasks

- Split capture into states:
  - `Camera`
  - `ReviewPhoto`
  - `EditTransaction`
  - `Saving`
  - `Saved`
- Add gallery picker using Android photo picker where available.
- Add calculator engine supporting digits, decimal, triple zero, clear, backspace, plus, minus, multiply, divide, equals, and sign-safe money output.
- Replace amount text field with keypad-driven amount state.
- Add date selector chip with at least today/default and a date picker route/dialog.
- Add income/expense segmented toggle with visual colors matching the reference.
- Preserve existing upload flow and `saveWithoutPhoto`.
- Ensure capture modal can receive prefilled wallet/category/date/type from Home, Budget, or Account contexts.
- Add haptic feedback for keypad, shutter, save, and validation failure if available.

## Data/API Changes

- Existing `transactions` and `transaction_photos` remain sufficient for basic capture.
- Optional transaction metadata can be added later: merchant, location, source, calculator expression.
- Capture repository API should accept nullable photo path and prefilled context cleanly.

## Edge Cases

- User denies camera permission but still wants gallery or manual entry.
- Upload fails after transaction save; keep existing behavior allowing retry or save without photo.
- Calculator must reject zero, negative final amount, invalid expression, and amounts beyond database numeric limits.
- Orientation and small screens must not hide the save button or keypad rows.

## Tests

- Unit tests for calculator operations, formatting, clear/backspace, decimal, triple zero, and invalid input.
- ViewModel tests for camera-to-edit transition, gallery import, skip photo, save success, upload failure, and validation errors.
- Compose UI tests for keypad input and required field validation.
- Manual device tests for camera, gallery picker, permission denial, upload, and save without photo.

## Acceptance Criteria

- Capture visually matches the reference flow and no longer feels like a generic form.
- Users can create a transaction with photo, gallery image, or no image.
- Calculator input is reliable and covered by tests.
- Existing Supabase photo upload and transaction create behavior remains intact.

