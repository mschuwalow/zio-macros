import sbt._
import Keys._

import explicitdeps.ExplicitDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.CrossType
import dotty.tools.sbtplugin.DottyPlugin.autoImport._
import sbtbuildinfo._
import BuildInfoKeys._

object BuildHelper {
  val zioVersion = "1.0.0-RC17"

  private val testDeps = Seq(
    "dev.zio" %% "zio-test"     % zioVersion % "test",
    "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
  )

  private def compileOnlyDeps(scalaVersion: String) = {
    val stdCompileOnlyDeps = Seq.empty
    val scala2CompileOnlyDeps = Seq(
      "com.github.ghik" %% "silencer-lib" % "1.4.2" % "provided",
      compilerPlugin("org.typelevel"   %% "kind-projector"  % "0.10.3"),
      compilerPlugin("com.github.ghik" %% "silencer-plugin" % "1.4.2")
    )

    val extraCompileOnlyDeps = CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, x)) if x <= 12 =>
        scala2CompileOnlyDeps ++ Seq(
          compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full))
        )
      case Some((2, _)) => scala2CompileOnlyDeps
      case _ => Seq.empty
    }
    stdCompileOnlyDeps ++ extraCompileOnlyDeps
  }

  private def compilerOptions(scalaVersion: String, optimize: Boolean) = {
    val stdOptions = Seq(
      //"-Ymacro-debug-lite",
      //"-Ymacro-debug-verbose",
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings"
    )

    val scala2Options = Seq(
      "-language:higherKinds",
      "-language:existentials",
      "-explaintypes",
      "-Yrangepos",
      "-Xsource:2.13",
      "-Xlint:_,-type-parameter-shadow",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard"
    )

    val extraOptions = CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) =>
        scala2Options ++ Seq(
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused",
          "-Ymacro-annotations"
        )
      case Some((2, 12)) =>
        scala2Options ++ Seq(
          "-Ypartial-unification",
          "-opt-warnings",
          "-Ywarn-extra-implicit",
          "-Ywarn-unused",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit"
        )
      case Some((2, 11)) =>
       scala2Options ++ Seq(
          "-Ypartial-unification",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Xexperimental",
          "-Ywarn-unused-import"
        )
      case _ => Seq.empty
    }

    val optimizerOptions =
      if (optimize)
        CrossVersion.partialVersion(scalaVersion) match {
          case Some((2, 11)) =>
            Seq.empty
          case _ =>
            Seq("-opt:l:inline")
        }
      else Seq.empty

    stdOptions ++ extraOptions ++ optimizerOptions
  }

  val buildInfoSettings = Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
    buildInfoPackage := "zio",
    buildInfoObject := "BuildInfoZioMacros"
  )

  def stdSettings(prjName: String) =
    Seq(
      name := s"$prjName",
      crossScalaVersions := Seq("2.13.0", "2.12.8", "2.11.12"),
      scalaVersion in ThisBuild := crossScalaVersions.value.head,
      scalacOptions := compilerOptions(scalaVersion.value, optimize = !isSnapshot.value),
      libraryDependencies ++= compileOnlyDeps(scalaVersion.value) ++ testDeps,
      parallelExecution in Test := true,
      incOptions ~= (_.withLogRecompileOnMacro(true)),
      autoAPIMappings := true,
      unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library"),
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      Compile / unmanagedSourceDirectories ++= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, _)) =>
            Seq(
              CrossType.Full.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + "-2.x")),
              CrossType.Full.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + "-2.x"))
            ).flatten
          case _ =>
            Seq(
              CrossType.Full.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + "-dotty")),
              CrossType.Full.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + "-dotty"))
            ).flatten
        }
      }
    )

  def dottySettings() =
    Seq(
      crossScalaVersions += "0.20.0-RC1"
    )

  def macroSettings() =
    Seq(
      scalacOptions --= (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("-deprecation", "-Xfatal-warnings")
        case _ => Seq.empty
      }),
      libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq(
            "org.scala-lang" % "scala-reflect"  % scalaVersion.value % "provided",
            "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
          )
        case _ => Seq.empty
      })
    )

  def examplesSettings() =
    Seq(
      skip in publish := true,
      libraryDependencies += "dev.zio" %% "zio" % zioVersion
    )

  def testSettings() =
    Seq(
      skip in publish := true,
      scalacOptions ++= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, x)) if x <= 11 => Seq("-Ywarn-unused:false")
          case Some((2, _))            => Seq("-Ywarn-unused:-explicits,_")
          case _ => Seq.empty
        }
      }
    )
}
