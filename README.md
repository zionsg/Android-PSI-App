# Android PSI App

An Android app to display the PSI index of various regions in Singapore,
using data from https://data.gov.sg/dataset/psi and displaying it on a map.

## To-do List
A list of things not done yet due to time constraints:
- Refactoring of code. Functions for manipulating the map, calling the API, parsing the response
  should reside in their own classes.
- More fine-grained error reporting. If an API call fails, it could be due to API key, network,
  server, permissions, etc.
- Unit tests.
- Adding of a field to change the date for the PSI readings.
- Show additional information when the map is zoomed in, such as weather?
- Menu, which can contain information on the app, help and links.
- App icon.

## Requirements
- Android >= 5.0 (Lollipop)
- Android Studio >= 2.3.3

## Installation
- Copy `gradle.properties.dist` to `gradle.properties`. Fill in the API keys for Google Maps
  and data.gov.sg. Links to getting the keys are provided in the comments.
- Open the project in Android Studio and run.

## Devices tested on
- Xiaomi RedMi 2 Enhanced (Android 5.1.1, 2GB RAM, 16GB storage)

## References
- MyTransport app
- Android Developer Docs
  + https://developer.android.com/training/maps/index.html
  + https://developer.android.com/training/volley/index.html
- Google Maps Docs
  + https://developers.google.com/maps/documentation/android-api/start
  + https://developers.google.com/maps/documentation/android-api/code-samples
  + https://developers.google.com/maps/documentation/android-api/utility/
  + https://github.com/googlemaps/android-maps-utils
- Data.gov.sg
  + https://data.gov.sg/dataset/psi
  + https://developers.data.gov.sg/
  + https://developers.data.gov.sg/data-gov-sg-apis/apis
  + https://developers.data.gov.sg/environment/psi
- Misc
  + https://android--examples.blogspot.sg/2017/02/android-volley-singleton-pattern-example.html
    for importing Volley singleton
  + http://blog.codylab.com/keep-api-secret/
  + https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
  + https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android/#44162377

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
  + 21:25 - 22:45 (1h 20m)
    * Create .dist for gradle properties
    * Put to-do list and installation instructions in README.md
    * Place markers on map for each region with label and PSI reading
  + 23:30 - 00:40 (1h 10m)
    * Use IconGenerator in Google Maps Android API utility library to display text on markers
    * End of part 1
