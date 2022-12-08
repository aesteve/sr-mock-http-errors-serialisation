ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.2.1"

val vertxVersion = "4.3.5"
val kafkaVersion = "3.3.1"
val cpVersion = "7.3.0"
val logbackVersion = "1.4.5"

val mockSrThrottled = (project in file("sr-throttler"))
  .settings(
    assembly / mainClass := Some("com.github.aesteve.runSchemaRegistryThrottled"),
    assembly / assemblyJarName := "mock-sr-throttled.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("module-info.class") => MergeStrategy.last
      case it if it.endsWith("/module-info.class") => MergeStrategy.last
      case it if it.contains("io.netty.versions.properties") => MergeStrategy.discard
      case it =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(it)
//      case PathList("META-INF", _*) => MergeStrategy.discard
//      case x => MergeStrategy.first
    },
    libraryDependencies ++= Seq(
      "io.vertx"        % "vertx-web"                       % vertxVersion,
      "io.vertx"        % "vertx-micrometer-metrics"        % vertxVersion,
      "io.micrometer"   % "micrometer-registry-prometheus"  % "1.10.2",
      "ch.qos.logback"  % "logback-classic"                 % logbackVersion,
    )
  )

val kafkaProducer = (project in file("avro-producer"))
  .settings(
    resolvers += "Confluent" at "https://packages.confluent.io/maven/",
    assembly / mainClass := Some("com.github.aesteve.runAvroProducerWithNoCache"),
    assembly / assemblyJarName := "schema-registry-throttler.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case "logback.xml" => MergeStrategy.first
      case x => (assembly / assemblyMergeStrategy).value(x)
    },
    libraryDependencies ++= Seq(
      "org.apache.kafka"  % "kafka-clients"           % kafkaVersion,
      "io.confluent"      % "kafka-avro-serializer"   % cpVersion,
      "ch.qos.logback"    % "logback-classic"         % logbackVersion
    )
  )

