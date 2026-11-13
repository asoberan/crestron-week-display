# Crestron Week Display

A simple web application that uses the Microsoft Graph API to generate a weekly view of a calendar.
The intention is for this to be displayed on a Crestron TS-1080, as the default Crestron Scheduling application does not allow for a week view of a resource calendar.

## TODO: Configuration

The configuration is an EDN file with the following values:

```edn
{:tenant "YOUR_TENANT_ID"
 :client-id "YOUR_CLIENT_ID"
 :client-secret "YOUR_CLIENT_SECRET"
 :port 4141 # Optional. Defaults to 3000.
 :time-zone "Europe/London" # Optional. Defaults to America/New_York
 :rooms {"ROOM NAME" {:name "ROOM NAME"
                      :email "ROOM_EMAIL@EXAMPLE.ORG"
					  :weekdays? true # Whether to display the weekdays for this room. Optional. Defaults to false.
}}
```

## Development

If you are using Guix, you can create a development shell with Java, Clojure, Clojure CLI tools, and Git preinstalled as follows:

```bash
guix time-machine -C channels.scm -- shell -m manifest.scm
```

Once the shell has been initialized, you can run the project.

```bash
clojure -M:run-m /path/to/config.edn
```

## TODO: Installation

```bash
clojure -T:build ci
```

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF` directory inside `target/classes` and the uberjar in `target`.
You can update the version (and SCM tag) information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version` from `build.clj`.

Run that uberjar:

```bash
java -jar target/net.clojars.asoberan/crestron-week-display-0.1.0-SNAPSHOT.jar
```

## License

Copyright © 2026 Asoberan

Distributed under the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0)
