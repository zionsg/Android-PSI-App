# Android PSI App

## To-do List
A list of things not done yet due to time constraints:
- Refactoring of code. Functions for manipulating the map, calling the API, parsing the response should reside in their own classes.
- More fine-grained error reporting. If an API call fails, it could be due to API key, network, server, permissions, etc.
- Unit tests.
- Adding of a field to change the date for the PSI readings.
- Additional information when the map is zoomed in?
- Menu, which can contain information on the app, help and links.
- App icon.

## Requirements
- Android >= 5.0 (Lollipop)

## Installation
- Copy `gradle.properties.dist` to `gradle.properties`. Fill in the API keys for Google Maps and data.gov.sg. Links to getting the keys are provided in the comments.
- Open the project in Android Studio and run.

## References
- MyTransport app
- https://developer.android.com/training/maps/index.html
- https://developers.google.com/maps/documentation/android-api/start
- https://developers.google.com/maps/documentation/android-api/code-samples
- https://developer.android.com/training/volley/index.html
- https://android--examples.blogspot.sg/2017/02/android-volley-singleton-pattern-example.html for importing Volley singleton
- https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android/#44162377
- https://data.gov.sg/dataset/psi
- https://developers.data.gov.sg/
- https://developers.data.gov.sg/data-gov-sg-apis/apis
- https://developers.data.gov.sg/environment/psi
- http://blog.codylab.com/keep-api-secret/
- https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request

## Timeline
- Mon 23 Oct 2017
  + 21:00 - 22:00 (1h)
    * Create empty GitHub repo
    * Create Android Map project
    * Get Google Maps API key and save in project
    * Initial build and run on phone
    * Center map on Singapore
  + 22:20 - 22:35 (15m)
    * Initial sync of project with GitHub
    * Enable MyLocation button
    * Do simple HTTP request with Volley
    * Show dialog
- Wed 25 Oct 2017
  + 07:25 - 08:25 (1h)
    * Register to use Data.gov.sg API
    * Create app on data.gov.sg and get API key
    * Store API keys for Google Maps and data.gov.sg more securely without committing to GitHub
  + 21:35 - 22:35 (1h)
    * Fix Google Maps API key not being read
    * Regenerate Google Maps API key
    * Call data.gov.sg API
  + 23:10 - 00:50 (1h 40m)
    * Parse JSON response from data.gov.sg
- Thu 26 Oct 2017
  + 21:25
    * Create .dist for gradle properties
    * Put to-do list and installation instructions in README.md
