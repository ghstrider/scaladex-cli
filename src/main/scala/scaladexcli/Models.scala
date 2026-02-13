package scaladexcli

import upickle.default.*

case class SearchResult(
    organization: String,
    repository: String,
    artifacts: List[String]
) derives ReadWriter

case class ProjectDetails(
    artifacts: List[String],
    versions: List[String],
    groupId: String,
    version: String
) derives ReadWriter
