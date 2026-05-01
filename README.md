# Scavenger Hunt

Android app for keeping track of cool places and turning them into clues for friends.

Built for COMP3074 — Mobile App Development.

## Look & feel

Recently overhauled the visual design — moved from the default purple to a
warm teal + coral palette, swapped the bare `EditText` fields for proper
Material `TextInputLayout`s (so we get floating labels and the password
reveal toggle for free), and added a gradient backdrop on the auth screens.

## Features

- **Login / sign-up** with a local user database
- Auto-login next launch (session is remembered)
- **Live search** by name or tag, filters as you type
- **Sort** by newest, name, or rating (toolbar)
- Tap a hunt → details. **Long-press** a hunt → delete with **undo** (snackbar)
- Add / edit hunts: name, tag, address, city, description, star rating
- **Mark as Done** — green check on the row, "DONE" badge on the detail screen
- **Directions** opens the address in your maps app, with a browser fallback
  if no maps app is installed
- **Share** any hunt via the system share sheet (SMS, email, anything)
- 5 sample Toronto hunts seeded the first time you open the app
- Logout confirmation so you don't tap it by accident

## File layout

```
app/src/main/java/ca/gbc/comp3074/scavengerhunt/
├── MainActivity.kt              login screen + session check
├── SignUpActivity.kt            sign-up screen
├── HuntListActivity.kt          home screen (list + search + sort)
├── HuntDetailActivity.kt        single hunt details + actions
├── AddEditHuntActivity.kt       form, used for both create and edit
├── AboutActivity.kt             static "about" page
├── HuntAdapter.kt               RecyclerView adapter for the list
├── ScavengerHuntApp.kt          Application — singletons + first-run seed
├── SessionManager.kt            SharedPreferences wrapper for login session
├── Extensions.kt                tiny one-liner helpers (toast, showIf, repo)
│
├── data/
│   ├── AppDatabase.kt           Room database (singleton)
│   ├── User.kt + UserDao.kt     users table
│   ├── Hunt.kt + HuntDao.kt     hunts table + sort variants
│   └── HuntRepository.kt        thin wrapper over the two DAOs
│
└── viewmodel/
    ├── AuthViewModel.kt         login + sign-up
    └── HuntViewModel.kt         list / detail / add-edit
```

## Why directions used to silently fail (and how it's fixed)

Three things were wrong with the v1 directions button:

1. **Android 11 package visibility.** Since API 30, your app can't see what
   other apps are installed unless you declare a `<queries>` block. Without
   it, `Intent.resolveActivity()` returned `null` even when Google Maps
   *was* installed. The manifest now has the right `<queries>` for `geo:`,
   `https:`, and `text/plain` SEND.
2. **No web fallback.** Some emulators / minimal devices have no maps app
   at all. The new code catches `ActivityNotFoundException` from the geo:
   intent and falls back to opening the Google Maps web URL — basically
   guaranteed to work as long as a browser is installed.
3. **No error feedback.** If even the web fallback has no handler, you now
   get a toast instead of nothing happening.

## Tech bits

- Kotlin + view binding (no `findViewById` everywhere)
- Room + KSP for the DB
- ViewModel + LiveData (survives rotation, no DB on UI thread)
- `MediatorLiveData` to merge "current search query" + "current sort order"
  into one stream the list activity observes
- `ListAdapter` + `DiffUtil` for animated row updates
- Material 3 components (TextInputLayout, MaterialCardView, MaterialButton,
  Snackbar, FAB)
- `enableEdgeToEdge()` + window-insets handling on the login screen so the
  layout doesn't get clipped by the notch / nav bar

## Run

Open in Android Studio (Hedgehog or newer), let Gradle sync, hit Run.
First launch creates the DB and drops in the sample hunts.

## Caveats

> ⚠ Passwords are stored in plain text. Fine for a school project that runs
> only on-device — wouldn't ship like this. If this ever became real I'd
> hash with bcrypt or Argon2, and probably move the session token to
> EncryptedSharedPreferences.
