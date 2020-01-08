#!/bin/bash
# Based on http://sharetheconversation.blogspot.com/2012/01/setting-up-self-signed-ssl-certificates.html
# For passphrases use: flexchall12345

openssl genrsa -des3 -out flexchall.key 2048
cp flexchall.key flexchall.orig.key
openssl rsa -in flexchall.orig.key -out flexchall.key
openssl req -new -key flexchall.key -out flexchall.csr -config ./localhost.cnf
openssl req -new -x509 -key flexchall.key -out flexchallx509.crt -config ./localhost.cnf
openssl pkcs12 -inkey flexchall.key -in flexchallx509.crt -export -out flexchall.pkcs12
keytool -importkeystore -srckeystore flexchall.pkcs12 -srcstoretype PKCS12 -destkeystore flexchallpkcs12.keystore

# Just to check
keytool -list -v -keystore flexchallpkcs12.keystore

# 443 is not accessible when starting jetty in non-sudo mode, use 8443 instead
