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
import collection.Iterable
import org.joda.time.{Duration, DateTime}

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
                    .map(item => item._2)
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
          println(bs.startDate.head)
          val dt = new DateTime(bs.startDate.head)

          new BuildChangeFact(bs.id, ch.`@id`, cd.id, dt.toString, bs.status == "SUCCESS", dt.getMinuteOfDay, dt.getDayOfWeek, cd.username, fileCounts)
        })
      }
    }
  }

  // matrix will have the Y value, day of week, min - by 1/2 hr, users, files
  def Fact2Matrix(facts: Iterable[List[BuildChangeFact]], threshold: Int = 3):(Matrix[Int], List[String]) = {
    val flatFacts = facts.flatten(a => a)

    val users = flatFacts.map(f => f.commiters).toList.distinct

    val fileCountThresholdFilter = (p:(String, Int)) => p._2 >= threshold
    val emptyMap = Map.empty[String, Int]
    val fileCounts = flatFacts.foldLeft(emptyMap)((map,f) => map |+| f.fileChangeType)
                               .filter(fileCountThresholdFilter)

    val files = fileCounts.keys.toList;

    val numsimiHrs = 24*60/30
    def n =
      1 + //Y
      7 + // days
      numsimiHrs + // num 1/2 hours in day
      1 + // timespan since last
      1 + // number of users
      users.size +
      1 + // number of files
      files.size

    val m:Int = facts.size
    val X = DenseMatrix.zeros[Int](m, n)

    def f2v(fs: List[BuildChangeFact]): Vector[Int] = {
      val row = DenseVector.zeros[Int](n)
      val topFact = fs(0)
      val userCounts = fs.map(f=>f.commiters).foldLeft(Map[String, Int]()){
        (m, c) => m.updated(c, m.getOrElse(c, 0) + 1)
      }

      row(0) = (if (topFact.success) 1 else 0) // Y

      row(1 + topFact.runDay-1) = 1; // dayOfWeek is 1-7

      row(8 + topFact.startMin/30) = 1 // semiHr
      
      row(9 + numsimiHrs) = userCounts.size
      var ind = 10 + numsimiHrs
      userCounts.foreach(item => {
          val (user, count) = item
          row(ind + users.indexOf(user)) = count
        })
      ind += users.size
      var filesInGroup = fs.foldLeft(emptyMap)((map,f) => map |+| f.fileChangeType).filter(fileCountThresholdFilter)
      row(ind) = filesInGroup.size
      ind += 1
      filesInGroup.foreach((item) => {
        val (fileName, count) = item
        val fileInd = ind + files.indexOf(fileName)
        row(fileInd) = count
      })
      row
    }
    val flst = facts.toList.sortBy(a => new DateTime(a.head.date).getMillis)
    for (ii <- 0 until m) {
      X(ii,::) := f2v(flst(ii))
    }

    if (facts.size > 1) {
      val dates = flst.map(fg => new DateTime(fg(0).date))
      val runningTimeSpans = Iterable(0) ++ dates.zip(dates.tail).map(pair => new Duration(pair._1, pair._2).getStandardSeconds.toInt)
      //      println(flst.map(fg => fg(0).date))
      //      println(dates)
      //      println(runningTimeSpans)
      X(::,56) := runningTimeSpans.toArray.asVector
    }

    val colNames:List[String] = List("Y") ++
                                Range(1,8).map(d => "Day" + d) ++
                                Range(0,numsimiHrs).map(d => "SemHr" + d) ++
                                List("TimeSinceLast") ++
                                List("UserCount") ++
                                users ++
                                List("FileCount") ++
                                files
    return (X, colNames)
  }
}
