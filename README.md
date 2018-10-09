# Play & Akka Remote working together

## Disclaimer

Don't use Akka Remote directly on your application. We recommend [Akka Cluster](https://doc.akka.io/docs/akka/2.5/cluster-usage.html) over using remoting directly. As remoting is the underlying module that allows for Cluster, it is still useful to understand details about it though. Akka remoting is designed for communication in a peer-to-peer fashion and it is not a good fit for client-server setups.

## Introduction

This sample Play application shows how to use Play and Akka Remote together.

It is heavily based on [Akka Samples](https://github.com/akka/akka-samples) so you may also want to take a look at the `akka-sample-remote-java` project there. In summary, this is what the application does:

> In order to showcase the [remote capabilities of Akka](http://doc.akka.io/docs/akka/2.5/java/remoting.html) we thought a remote calculator could do the trick. This sample demonstrates both remote deployment and look-up of remote actors.

## Project structure

There are two separated applications here:

1. `akka-remote-service`: it is the Akka application that exposes the remote actor.
2. `play-client-app`: it is the Play application that lookup the remote actor.

The two applications are present ion this repository just for convenience, but in a real application, they may be completely separated and must run and be deployed in isolation.

## Step by Step guide

This a step-by-step guide describing how this application was created. If you want to see if running, jump to 

### Add Akka Remote dependency to your project

Add the following dependency to your project:

```scala
// When using sbt
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.17"
```

```groovy
// When using Gradle
compile group: "com.typesafe.akka", name: "akka-remote_${scalaVersion}", version: "${akkaVersion}"
```

### Copy the code from Akka Sample

We have copied the code from Akka Sample to Play. But we need to adjust the structure since Play uses its own directory layout. See [Play Anatomy](https://www.playframework.com/documentation/2.6.x/Anatomy) documentation for a better explaining of Play directory layout. The code was then copied to `play-akka-remote/app/actors` where `actors` is just a regular package name inside `app` source folder. After copying we need to adjust the package declaration from:

```java
package sample.remote.calculator;
```

To:

```java
package actors;
```

We then replace all the `System.out.println` calls with [a Logger that can be configured](https://www.playframework.com/documentation/2.6.x/SettingsLogger).

### Configuration

Akka sample splits the configuration in [multiple files](https://github.com/akka/akka-samples/tree/2.5/akka-sample-remote-java/src/main/resources) that are later loaded depending on which actor you decide to run. Let's consider that our Play application will only lookup for the actor, but it WILL NOT create remote actors to be accessed by other applications. So, when combining `common.conf` and `remotelookup.conf`, we will have the following content in Play's `conf/application.conf` file:

```hocon
akka {

  actor {
    provider = remote
  }

  remote {
    netty.tcp {
      port = 2553
      hostname = "127.0.0.1"
    }
  }

}
```

### LookupActor and Dependency injection

We now need to make our lookup actor start when the application starts.

In the case of Akka Sample, that was done by a class with a main method. But we don't have that in Play. The recommended way to do that is to use [Dependency Injection](https://www.playframework.com/documentation/2.6.x/JavaDependencyInjection) and use an [eager binding](https://www.playframework.com/documentation/2.6.x/JavaDependencyInjection#Eager-bindings) to do the look up as soon as the application starts. Play has specific helpers to help integrating [Actors and Dependency injection](https://www.playframework.com/documentation/2.6.x/JavaAkka#Dependency-injecting-actors), so we used them to create `actors.ActorsModule`.

`LookupActor` receives a path that can be configured, so let's add a constructor to receive a `Config` and get the `path` from there:

```java
@Inject
public LookupActor(Config config) {
    this(config.getString("lookup.path"));
}
```

We then add the following configuration to `conf/application.conf`:

```hocon
lookup.path="akka.tcp://CalculatorSystem@127.0.0.1:2552/user/calculator"
```

After that, we can inject `LookupActor` wherever we want. At this example, we will inject it into `HomeController`:

```java
private final ActorRef lookupActor;

@Inject
public HomeController(@Named("lookup-actor") ActorRef lookupActor) {
    this.lookupActor = lookupActor;
}
```

If we start the application at this point using `sbt run`, we will see the following logs:

```
[info] a.r.Remoting - Starting remoting
[info] a.r.Remoting - Remoting started; listening on addresses :[akka.tcp://application@127.0.0.1:2553]
[info] a.r.Remoting - Remoting now listens on addresses: [akka.tcp://application@127.0.0.1:2553]
[warn] a.r.ReliableDeliverySupervisor - Association with remote system [akka.tcp://application@127.0.0.1:2552] has failed, address is now gated for [5000] ms. Reason: [Association failed with [akka.tcp://application@127.0.0.1:2552]] Caused by: [Connection refused: /127.0.0.1:2552]
[warn] a.r.t.n.NettyTransport - Remote connection to [null] failed with java.net.ConnectException: Connection refused: /127.0.0.1:2552
[info] a.LookupActor - Remote actor not available: akka.tcp://application@127.0.0.1:2552/user/calculator
```

It is failing because we don't have a `Calculator` actor running anywhere. This is expected we were just trying to verify if everything was working as expected.

## Running

You need to start both the remove Akka service, which will exposes the `Calculator` actor, and also the Play application which will use this remove actor.

### Starting a remote calculator Actor

To start a remote calculator actor, we can adapt the code from Akka Sample and add a new class to our project. The new class is `actors.Calculator` application which has a `main` method. Also, we need specific configuration to this "application". So we created a `conf/calculator.conf` file. To run it successfully, run the following command in a terminal:

```bash
sbt akka-remote-service/run
```

Or for Gradle:

```bash
./gradlew akka-remote-service:run
```

This is just emulating a remote application.

### Start the Play application

Now start the Play application in another terminal:

```bash
sbt play-client-app/run
```

Or for Gradle:

```bash
./gradlew play-client-app:runPlayBinary
```

Open <http://localhost:9000> and you will then see the following logs:

```java
[info] a.r.Remoting - Starting remoting
[info] a.r.Remoting - Remoting started; listening on addresses :[akka.tcp://application@127.0.0.1:2553]
[info] a.r.Remoting - Remoting now listens on addresses: [akka.tcp://application@127.0.0.1:2553]
[info] p.a.h.EnabledFilters - Enabled Filters (see <https://www.playframework.com/documentation/latest/Filters>):

    play.filters.csrf.CSRFFilter
    play.filters.headers.SecurityHeadersFilter
    play.filters.hosts.AllowedHostsFilter

[info] play.api.Play - Application started (Dev)
```

So, no warnings about remote actor not being available. You can then make a request that sends a message to the remove `Calculator` actor: <http://localhost:9000/add/1/20> (adds numbers `1` and `20`). Open `HomeController` again to see how `add` action works.