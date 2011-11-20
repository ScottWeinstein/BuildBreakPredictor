package org.SW.Serialization

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import net.liftweb.json._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.text.SimpleDateFormat

@RunWith(classOf[JUnitRunner])
class jsonParseTest extends Spec with MustMatchers {

  describe("jsonParse") {

    val testJsonString = """{
    "id":20452,
    "number":"651",
    "status":"SUCCESS",
    "href":"/httpAuth/app/rest/builds/id:20452",
    "webUrl":"http://abc.lab49.com/viewLog.html?buildId=20452&buildTypeId=bt43",
    "personal":false,
    "history":false,
    "pinned":false,
    "statusText":"Tests passed: 45",
    "buildType": {
        "id":"bt43",
        "name":"Debug - master",
        "href":"/httpAuth/app/rest/buildTypes/id:bt43",
        "projectName":"abc",
        "projectId":"project28",
        "webUrl":"http://abc.lab49.com/viewType.html?buildTypeId=bt43"
    },
    "startDate":"20111122T000153-0500",
    "finishDate":"20111118T170818-0500",
    "agent":{"name":"TC-BuildAgent01","id":2,"href":"/httpAuth/app/rest/agents/id:2"},
    "tags":null,
    "properties":null,
    "revisions":{
        "revision":[
                {
                    "display-version":"7343231598618f925c0196d7574fb1c6ebb9e37c",
                    "vcs-root":{
                      "href":"/httpAuth/app/rest/vcs-roots/id:36,ver:10",
                      "name":"abc master"
                      }
                }
            ]
    },
    "changes":{
            "href":"/httpAuth/app/rest/changes?build=id:20452",
            "count":1
    },
    "relatedIssues":null
}"""
    implicit val formats = new DefaultFormats {
            override def dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
           }


    val json = parse(testJsonString)
    val bs = json.extract[BuildStatus]

    it("can parse basic BuildStatus") {
      bs.status must equal ("SUCCESS")
      bs.changes.count must equal (1)
      bs.revisions.head("revision")(0).`vcs-root`.name must equal ("abc master")
    }

    it("can parse Dates") {
      val dt =  new org.joda.time.DateTime(bs.finishDate.head)
      dt.getDayOfMonth() must equal (18)
      dt.getHourOfDay() must equal (17)
      dt.getMinuteOfHour() must  equal (8)
    }

    it("can parse as List") {
    val listEx = """{
  "@count":"2",
  "change":[{
    "@webLink":"http://localhost:8080/viewModification.html?modId=6&personal=false",
    "@version":"b51fde683e206826f32951750ccf34b14bead9ca",
    "@id":"6",
    "@href":"/httpAuth/app/rest/changes/id:6"
  },{
    "@webLink":"http://localhost:8080/viewModification.html?modId=5&personal=false",
    "@version":"826626ff5d6bc95b32c7f03c3357b31e4bf81842",
    "@id":"5",
    "@href":"/httpAuth/app/rest/changes/id:5"
  }]
}"""
      parse(listEx).extract[ChangeItem] // OK
    }

    it("can parse as single") {
      val singleEx = """{
  "@count":"1",
  "change":{
    "@webLink":"http://localhost:8080/viewModification.html?modId=8&personal=false",
    "@version":"803f9c1cd2c553c3b3fb0c950585be868331b3b1",
    "@id":"8",
    "@href":"/httpAuth/app/rest/changes/id:8"
  }
}"""
      val p = parse(singleEx) transform {
        case JField("change", o: JObject) => JField("change", JArray(o :: Nil))
      }
        p.extract[ChangeItem] // throws
    }

  }
}
