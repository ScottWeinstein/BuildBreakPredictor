package org.SW.Serialization

import java.util.Date


case class BuildStatusRefList(count: Int, build: List[BuildStatusRef])
case class BuildStatusRef(id: String, number: String, status: String, buildTypeId: String, startDate: Option[String], href: String, webUrl: String)
case class BuildStatus(id: Int,
                       number: String, //it's quoted...
                       status: String,
                       href: String,
                       webUrl: String,
                       personal: Boolean,
                       history: Boolean,
                       pinned: Boolean,
                       statusText: String,
                       buildType: Map[String, String],
                       startDate: Option[Date],
                       finishDate: Option[Date],
                       agent: Map[String, String],
                       tags: Option[Map[String, String]],
                       properties: Option[Map[String, String]],
                       revisions: Option[Map[String,List[Revision]]],
                       changes: ChangeRef)

case class Revision(`display-version`: String, `vcs-root`: VCSRoot)

case class VCSRoot(href: String, name: String)

case class ChangeRef(href: String, count: Int)

case class RunParams(url: String, userName: String, password: String)

case class ChangeItem(`@count`: String, change: List[ChangeItemDetail]) {
  def this(`@count`: String, change: ChangeItemDetail) = this(`@count`, List(change))
}
case class ChangeItemDetail(`@webLink`:String, `@version`:String, `@id`:String, `@href`: String)

case class ChangeDetail(username: String,
                        date: Option[Date],
                        webLink: String,
                        version: String,
                        id: Int,
                        href: String,
                        comment: String,
                        files: Option[Map[String, List[ChangeDetailFileItem]]])
case class ChangeDetailFileItem(`relative-file`: String, file: String, `after-revision`: String, `before-revision`: String)

