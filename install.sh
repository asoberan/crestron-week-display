#!/usr/bin/env bash

mkdir -p /etc/crestron-week-display
chown root:root /etc/crestron-week-display
chmod 600 /etc/crestron-week-display
cp examples/config.edn /etc/crestron-week-display/config.edn

mkdir -p /opt/crestron-week-display
cp target/net.clojars.asoberan/crestron-week-display-0.1.0-SNAPSHOT.jar /opt/crestron-week-display/crestron-week-display.jar
cp examples/crestron-week-display.service /etc/systemd/system
systemctl daemon-reload
systemctl enable --now crestron-week-display
