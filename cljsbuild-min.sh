#!/bin/sh
rm -f resources/public/js/flexchall.js
lein with-profile uberjar cljsbuild once flexchall-min
