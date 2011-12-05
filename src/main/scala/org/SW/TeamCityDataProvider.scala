package org.SW

import java.text.SimpleDateFormat
import net.liftweb.json._
import Serialization._
import dispatch.{url, Request, Http}
import scalala.tensor.mutable._;
import scalala.tensor.dense._;
import scalala.operators.Implicits._
import scalala.tensor.{Matrix, ::}
import scalaz._
import Scalaz._
import collection.immutable
import immutable.{List, Iterable}

class TeamCityDataProvider(creds: RunParams) {
  implicit val formats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyyMMdd'T'hhmmssZ")
  }

  val http = new Http
  val baseUrl: Request = url(creds.url) <:< Map("Accept" -> "application/json") as_!(creds.userName, creds.password)

  def handlejsonDebug(jstr2: String) = {
      val jValue = JsonParser.parse(jstr2)
      println(pretty(render(jValue)))
    }

  def parseJson[T: Manifest](jstr: String) = {
    val jValue = JsonParser.parse(jstr) transform {
      case JField("change", o: JObject) => JField("change", JArray(o :: Nil))
    }
    Extraction.extract[T](jValue)
  }

  def Facts(buildId: String, count:Int = 10000): Iterable[List[BuildChangeFact]] = {
    val url = baseUrl / "httpAuth/app/rest/buildTypes/id:%s/builds/?count=%d".format(buildId, count)
    val buildStatusRefs:List[BuildStatusRef] = http( url >- parseJson[BuildStatusRefList]).build
    buildStatusRefs.map(bsr => bsr.href.substring(1))
                    .flatMap(url => getBuildChangeFact(baseUrl, url))
                    .groupBy(bcf => bcf.id)
                    .map((item) => item._2)
  }

  def getBuildChangeFact(baseUrl: Request, url:String): List[BuildChangeFact] = {
    val bs = http(baseUrl / url >- parseJson[BuildStatus])

    val defaultFileMap = Map("file" -> List[ChangeDetailFileItem]())

    bs.changes.count match {
      case 0 => List()
      case _ => {
        val changeItem = http(baseUrl / bs.changes.href.substring(1) >- parseJson[ChangeItem])
        changeItem.change.map(ch => {
          val cd = http(baseUrl / ch.`@href`.substring(1) >- parseJson[ChangeDetail])
          val fileMap = cd.files.getOrElse(defaultFileMap)
          val fileCounts = fileMap("file").groupBy(f => f.file.split("/").last).mapValues(lst => lst.size)
          val dt = new org.joda.time.DateTime(bs.startDate.head)

          new BuildChangeFact(bs.id, ch.`@id`, cd.id, dt.toString, bs.status == "SUCCESS", dt.getMinuteOfDay, dt.getDayOfWeek, cd.username, fileCounts)
        })
      }
    }
  }

  // matrix will have the Y value, day of week, min - by 1/2 hr, users, files
  def Fact2Matrix(facts: List[BuildChangeFact]):(Matrix[Int], List[String]) = {
    val users = facts.map(f => f.commiters).distinct.toList


    val fileCountThresholdFilter = (p:(String, Int)) => p._2 > 5
    val fileCounts = facts.foldLeft(immutable.Map.empty[String, Int])((map,f) => map |+| f.fileChangeType)
                            .filter(fileCountThresholdFilter);
    val files = fileCounts.keys.toList;

    val numsimiHrs = 24*60/30
    val n:Int = 1 + 7 + numsimiHrs + users.size  + files.size
    val m:Int = facts.size
    val X = DenseMatrix.zeros[Int](m, n)

    def f2v(f:BuildChangeFact): Vector[Int] = {
      val row = DenseVector.zeros[Int](n)
      row(0) = (if (f.success) 1 else 0)
      row(1 + f.runDay-1) = 1; // dayOfWeek is 1-7
      row(1 + 7 + f.startMin/30) = 1
      row(1 + 7 + numsimiHrs + users.indexOf(f.commiters)) = 1

      f.fileChangeType
       .filter(fileCountThresholdFilter)
       .foreach((item) => {
        val (fileName, count) = item
        val fileInd = 1 + 7 + numsimiHrs + users.size + files.indexOf(fileName)
        row(fileInd) = count
      })
      row
    }

    for (ii <- 0 until m) {
      X(ii,::) := f2v(facts(ii))
    }

    val colNames:List[String] = List("Y") ++
                                Range(1,8).map(d => "Day" + d) ++
                                Range(0,numsimiHrs).map(d => "SemHr" + d) ++
                                users ++
                                files
    return (X, colNames)
  }

//  def FeatureNormalize(matrix: Matrix[Double]) = {
//
//  }

}
