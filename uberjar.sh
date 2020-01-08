#!/bin/sh
lein clean
./cljsbuild-min.sh
lein with-profile uberjar uberjar
