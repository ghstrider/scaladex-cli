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

case class GitHubRelease(
    tag_name: String,
    name: String,
    published_at: String,
    body: String
) derives ReadWriter
