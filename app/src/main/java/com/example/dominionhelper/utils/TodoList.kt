package com.example.dominionhelper.utils

// Random info:
// Prop drilling: passing lots of data down through multiple levels (bad)
// by lazy: loading data only when needed (good)
// Flows: automatically updates UI elements when data changes
// mutableStateOf automatically updates UI elements reliant on the values when they change
// When passing a lambda function like (Card) -> Unit down to another function, should you put in the parameter as early as possible?
// -> As early as possible is good for clarity and separation of concerns. Capture card as early as possible

// It's a strong convention in Jetpack Compose that most non-trivial composables accept a modifier: Modifier = Modifier as their first optional parameter.

// adb pair <ip>:<port>
// adb tcpip 5555

// TODO Master list
// Rethink database scheme, especially expansions and editions suck
// CRASHLYTICS / Firebase
// Split piles!!
// Coffers?

// UI
// - Some cards' text is too long (League of Shopkeepers, Way of the Chameleon
// - Invisible top bar / only when header is relevant? (See Auth app) Back arrow when back is possible?
// - Remove search and FAB from irrelevant views (Or enable to search expansion / kingdom?)
// - Kingdom landscape view to show all cards images (5x2)?
// - Add (banned) card count to expansion list items
// - Enable all cards button (settings?)
// - Rework Settings UI
// - Explanation (i) icon for some stuff
// - Make no search results, search result count pretty
// - Add sorting for expansions
// - Costs: 6*, 4+, 0 (different from <no cost>) -> Show 0
// - Try bottom navigation bar instead of drawer
// - Try "invisible" list items
// - Try round FAB

// UX
// - Warning when navigating back from generated kingom (or kingdom history)?
// - Make stuff like debt searchable
// - In expansion view: if both edition are shown, show icon or text on each card
// - Swap icon in expansion list depending on expansion owned (Also in kingdom list)
// - Split expansion cards in normal and landscape cards
// - Landscape cards are low res
// -Cornucopia & Guilds - don't switch ownership at the same time, not all regions merged them

// Generation
// - Always use 2 different landscape cards. When rerolling, be sure to take the same type AND same expansion
// - When randomizing, check for 10 ENABLED cards

// Code
// - Use _somethingFromViewModel.update {} instead of setting _something.value
// - Check logs for redundancy
// - When scrolling through detail cards in search, every index is the same
// - Changing search text reloads expansion list
// - I think 'set' property can be removed from sets.json
// - Wenn man auf Basic geht sind Copper etc mittendrin. Wenn man ein kingdom generated und dann noch mal reingeht, sind die basic cards unten
// - 1st edition of Cornucopia has 2nd edition cards (at least Rewards) (?)

// TODO LOW(er) PRIO
// Code
// - Use coil or glide or fresco to load images to avoid "image decoding logging dropped" warnings
// - Applicationscope vs LifecycleScope vs CoroutineScope vs whatever
// - Flows instead of lists from DAO?
// - Save sort type between sessions
// - Icons.Filled.??
// - Try to thin out some parameters (TopBar)
// - After generating kingdom and changing sort type, it is reset after generating a new kingdom
// - Add modifier parameter to all Composables?

// Features
// - VP counter
// - Rating a kingdom afterwards (+ uploading)
// - Other languages

// Design
// - Show behind top + nav bar?
// - Check image sizes, turn placeholders to webp
// - Font: Princeps
// - Search bar: Not aligned with title, show hamburger instead of <-
// - Curse money card bei wunderheilerin
// - Check all cards / expansions

// Bugs
// - Nothing happens when clicking an edition
// - I think list state is shared between search / expansion and random cards (doesn't reset)
// -> Seems fine between expansion and random cards, expansion to search needs to reset
// Going back from expansion list resets even though it shouldn't