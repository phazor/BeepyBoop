# BeepyBoop
An ISS Tracker for Android

------------

## Setup

This module contains a submodule, Volley by Google.

Volley is pulled in by depending on the submodule. Unfortunately, Volley does not compile in AIDE (as of version 3.2.160525) and complains about a ternary within a log statement. The specific error is that one output of the ternary is an int, while the other is null. The way to 'work around' this is to modify the null to be an arbitrary int.

## Attribution

This app uses REST Apis provided by Sunrise-Sunset, available at http://api.sunrise-sunset.org

------------
TODO
 - come up with a better name
