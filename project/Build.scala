import sbt._
import Keys._
import com.github.joprice.Jni
import Jni.Keys._
import java.io.File

object ZstdBuild extends Build {

  lazy val buildVersion = "0.0.2"

  lazy val root = Project(id="zstd-jni", base = file("."), settings = Project.defaultSettings).
  settings(
    Jni.settings : _*
  ).settings(
    scalaVersion := "2.11.5",
    version := buildVersion,
    organization := "com.github.luben",
    libraryName := "libzstd",
    libraryDependencies ++= Seq(
      "org.scalatest"  %% "scalatest"  % "2.2.4"  % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
    ),
    gccFlags ++= {
      val default = Seq( "-std=c99", "-Wundef", "-Wshadow", "-Wcast-align", "-Wstrict-prototypes",
                         "-Wno-unused-variable", "-funroll-loops")
      val arch = System.getProperty("os.arch") match {
        case "amd64"|"x86_64"   => Seq("-msse4")
        case "i386"             => Seq("-msse4")
        case _                  => Seq()
      }
      default ++ arch
    },
    nativeCompiler := "gcc",
    includes += "-I" + nativeSource.value.toString,
    cppExtensions := Seq("zstd.c"),
    cpp11 := false,
    jniClasses := Seq(
        "com.github.luben.zstd.Zstd",
        "com.github.luben.zstd.ZstdInputStream",
        "com.github.luben.zstd.ZstdOutputStream"
    ),
    binPath := {
      val os = System.getProperty("os.name").toLowerCase.replace(' ','_')
      val arch =  System.getProperty("os.arch")
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((major,minor)) => (target in Compile).value / s"scala-$major.$minor" / "classes" / os / arch
        case None => sys.error("Can't find Scala version")
      }
    },
    headersPath := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((major,minor)) => (target in Compile).value / s"scala-$major.$minor" / "classes" / "include"
        case None => sys.error("Can't find Scala version")
      }
    }
  )
}
