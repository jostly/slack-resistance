import sbt._

object ProjectBuild extends Build {

  lazy val common = Project(id = "common-lib", base = file("common-lib"))

  lazy val acceptance = Project(id = "acceptance", base = file("acceptance")) dependsOn(playContext)

  lazy val contract = Project(id = "contract", base = file("play-context/contract")) dependsOn (common)

  lazy val command = Project(id = "command", base = file("play-context/command")) dependsOn (contract)

  lazy val query = Project(id = "query", base = file("play-context/query")) dependsOn (contract)

  lazy val sagas = Project(id = "sagas", base = file("play-context/sagas")) dependsOn(contract, command)

  lazy val application =
    Project(id = "application", base = file("play-context/application")).
    dependsOn (common, contract, command, query, sagas).
    aggregate(command, query, sagas)

  lazy val playContext = Project(id = "play-context", base = file("play-context")) aggregate (application) dependsOn(application)

}
