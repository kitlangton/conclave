name := "conclave"
description := "A full-stack Scala application powered by ZIO and Laminar."
version := "0.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

val animusVersion     = "0.1.11"
val boopickleVersion  = "1.4.0"
val laminarVersion    = "0.14.2"
val laminextVersion   = "0.14.3"
val postgresVersion   = "42.3.3"
val sttpVersion       = "3.5.1"
val zioAppVersion     = "0.3.0-RC2+0-4d75de29+20220412-1815-SNAPSHOT"
val zioConfigVersion  = "3.0.0-RC3"
val zioHttpVersion    = "2.0.0-RC4"
val zioQueryVersion   = "0.3.0-RC2"
val zioQuillVersion   = "3.16.3"
val zioVersion        = "2.0.0-RC2"
val zioPreludeVersion = "1.0.0-RC10"

val sharedSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= Seq("-Xfatal-warnings"),
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
  ),
  libraryDependencies ++= Seq(
    "com.lihaoyi"                   %%% "upickle"     % "1.6.0",
    "io.suzaku"                     %%% "boopickle"   % boopickleVersion,
    "dev.zio"                       %%% "zio"         % zioVersion,
    "dev.zio"                       %%% "zio-prelude" % zioPreludeVersion,
    "dev.zio"                       %%% "zio-streams" % zioVersion,
    "dev.zio"                       %%% "zio-macros"  % zioVersion,
    "dev.zio"                       %%% "zio-test"    % zioVersion % Test,
    "dev.zio"                       %%% "zio-json"    % "0.3.0-RC3",
    "io.github.kitlangton"          %%% "zio-app"     % zioAppVersion,
    "com.softwaremill.sttp.client3" %%% "core"        % sttpVersion
  ),
  scalacOptions ++= Seq("-Ymacro-annotations", "-Xfatal-warnings", "-deprecation"),
  scalaVersion := "2.13.8",
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val backend = project
  .in(file("backend"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    sharedSettings,
    Compile / run / mainClass := Some("conclave.Backend"),
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio-config"             % zioConfigVersion,
      "dev.zio"                       %% "zio-query"              % zioQueryVersion,
      "dev.zio"                       %% "zio-config-yaml"        % zioConfigVersion,
      "dev.zio"                       %% "zio-config-magnolia"    % zioConfigVersion,
      "io.d11"                        %% "zhttp"                  % zioHttpVersion,
      "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % sttpVersion,
      "org.postgresql"                 % "postgresql"             % postgresVersion,
      "io.getquill"                   %% "quill-jdbc-zio"         % zioQuillVersion,
      "com.github.t3hnar"             %% "scala-bcrypt"           % "4.3.0",
      "com.github.jwt-scala"          %% "jwt-core"               % "9.0.4"
    )
  )
  .dependsOn(shared, macros)

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.kitlangton" %%% "animus"               % animusVersion,
      "com.raquo"            %%% "laminar"              % laminarVersion,
      "io.laminext"          %%% "core"                 % laminextVersion,
      "io.github.cquiroz"    %%% "scala-java-time"      % "2.3.0",
      "io.github.cquiroz"    %%% "scala-java-time-tzdb" % "2.3.0",
      "com.raquo"            %%% "waypoint"             % "0.5.0",
      "io.laminext"          %%% "websocket"            % laminextVersion
    )
  )
  .settings(sharedSettings)
  .dependsOn(shared)

lazy val macros = project
  .enablePlugins(ScalaJSPlugin)
  .in(file("macros"))
  .settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) }
  )

lazy val shared = project
  .enablePlugins(ScalaJSPlugin)
  .in(file("shared"))
  .settings(
    sharedSettings,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) }
  )
