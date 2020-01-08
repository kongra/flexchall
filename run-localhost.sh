#!/bin/sh
java -server \
     -Dclojure.compiler.direct-linking=true \
     -Dclojure.spec.compile-asserts=false \
     \
     -Dcljc.flexchall.base-url=https://localhost:8443 \
     \
     -Dcljc.flexchall.service.port=8080 \
     -Dcljc.flexchall.service.ssl?=true \
     -Dcljc.flexchall.service.ssl.port=8443 \
     \
     -Xms256m -Xmx256m -XX:+UseStringDeduplication -XX:+DoEscapeAnalysis -XX:+UseCompressedOops \
     -classpath target/flexchall-0.1.0-standalone.jar \
     cljc.flexchall.core
