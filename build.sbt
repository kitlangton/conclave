name := "zeetup"
description := "A full-stack Scala application powered by ZIO and Laminar."
version := "0.1"

val animusVersion    = "0.1.9"
val boopickleVersion = "1.4.0"
val laminarVersion   = "0.13.1"
val laminextVersion  = "0.13.10"
val postgresVersion  = "42.2.23"
val sttpVersion      = "3.3.13"
val zioAppVersion    = "0.2.5+9-2e55fbbd+20210618-1100-SNAPSHOT"
val zioConfigVersion = "1.0.6"
val zioHttpVersion   = "1.0.0.0-RC17"
val zioMagicVersion  = "0.3.7"
val zioQueryVersion  = "0.2.10"
val zioQuillVersion  = "3.9.0"
val zioVersion       = "1.0.10"

val sharedSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= Seq("-Xfatal-warnings"),
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
  ),
  libraryDependencies ++= Seq(
    "io.github.kitlangton"           %% "zio-app"     % zioAppVersion,
    "io.suzaku"                     %%% "boopickle"   % boopickleVersion,
    "dev.zio"                       %%% "zio"         % zioVersion,
    "dev.zio"                       %%% "zio-streams" % zioVersion,
    "dev.zio"                       %%% "zio-macros"  % zioVersion,
    "dev.zio"                       %%% "zio-test"    % zioVersion % Test,
    "dev.zio"                       %%% "zio-json"    % "0.1.5",
    "io.github.kitlangton"          %%% "zio-app"     % zioAppVersion,
    "com.softwaremill.sttp.client3" %%% "core"        % sttpVersion
  ),
  scalacOptions ++= Seq("-Ymacro-annotations", "-Xfatal-warnings", "-deprecation"),
  scalaVersion := "2.13.6",
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val backend = project
  .in(file("backend"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    sharedSettings,
    Compile / run / mainClass := Some("zymposium.Backend"),
    libraryDependencies ++= Seq(
      "io.github.kitlangton"          %% "zio-magic"              % zioMagicVersion,
      "dev.zio"                       %% "zio-config"             % zioConfigVersion,
      "dev.zio"                       %% "zio-query"              % zioQueryVersion,
      "dev.zio"                       %% "zio-config-yaml"        % zioConfigVersion,
      "dev.zio"                       %% "zio-config-magnolia"    % zioConfigVersion,
      "io.d11"                        %% "zhttp"                  % zioHttpVersion,
      "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % sttpVersion,
      "org.postgresql"                 % "postgresql"             % postgresVersion,
      "io.getquill"                   %% "quill-jdbc-zio"         % zioQuillVersion,
      "com.github.jwt-scala"          %% "jwt-core"               % "9.0.0"
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
      "io.github.cquiroz"    %%% "scala-java-time"      % "2.3.0",
      "io.github.cquiroz"    %%% "scala-java-time-tzdb" % "2.3.0",
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
