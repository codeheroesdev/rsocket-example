name := "application"

version := "1.0"

scalaVersion := "2.12.2"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "io.reactivesocket" % "reactivesocket-core" % "0.5.3-SNAPSHOT"
libraryDependencies += "io.reactivesocket" % "reactivesocket-client" % "0.5.3-SNAPSHOT"
libraryDependencies += "io.reactivesocket" % "reactivesocket-transport-tcp" % "0.5.3-SNAPSHOT"
libraryDependencies += "io.reactivesocket" % "reactivesocket-transport-netty" % "0.5.3-SNAPSHOT"
libraryDependencies += "io.projectreactor.ipc" % "reactor-netty" % "0.6.2.RELEASE"
libraryDependencies += "io.projectreactor" % "reactor-core" % "3.0.6.RELEASE"

libraryDependencies += "io.reactivex.rxjava2" % "rxjava" % "2.0.9"
libraryDependencies += "org.agrona" % "Agrona" % "0.9.1"


resolvers += "OSS" at "https://oss.jfrog.org/libs-snapshot"