# TODO: Crestron Week Display

A simple web application that uses the Microsoft Graph API to generate a weekly view of a calendar.
The intention is for this to be displayed on a Crestron TS-1080, or any other similar kiosk-type system.

## TODO: Development

If you are using Guix, you can create a development shell as follows:

```bash
guix time-machine -C channels.scm -- shell -m manifest.scm
```

## TODO: Installation

## TODO: Usage

Run the project directly, via `:exec-fn`:

    $ clojure -X:run-x
    Hello, Clojure!

Run the project, overriding the name to be greeted:

    $ clojure -X:run-x :name '"Someone"'
    Hello, Someone!

Run the project directly, via `:main-opts` (`-m asoberan.crestron-week-display`):

    $ clojure -M:run-m
    Hello, World!

Run the project, overriding the name to be greeted:

    $ clojure -M:run-m Via-Main
    Hello, Via-Main!

Run the project's tests (they'll fail until you edit them):

    $ clojure -T:build test

Run the project's CI pipeline and build an uberjar (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the uberjar in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

If you don't want the `pom.xml` file in your project, you can remove it. The `ci` task will
still generate a minimal `pom.xml` as part of the `uber` task, unless you remove `version`
from `build.clj`.

Run that uberjar:

    $ java -jar target/net.clojars.asoberan/crestron-week-display-0.1.0-SNAPSHOT.jar

## TODO: Configuration

The configuration is an EDN file with the following values:

```edn
{:tenant "YOUR_TENANT_ID"
 :client-id "YOUR_CLIENT_ID"
 :client-secret "YOUR_CLIENT_SECRET"
 :rooms {"ROOM NAME" {:name "ROOM NAME"
                      :email "ROOM_EMAIL@EXAMPLE.ORG"}
 :port 4141 # Optional. Defaults to 3000.
}
```

## License

Copyright © 2026 Asoberan

Distributed under the [Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0)
