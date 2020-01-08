#!/bin/sh
lein clean
lein compile
lein bikeshed --max-line-length 80
lein eastwood
