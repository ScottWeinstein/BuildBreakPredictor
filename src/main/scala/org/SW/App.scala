package org.SW

import scala.io.Source
import net.liftweb.json.Serialization.{read, write}
import Serialization._
import scalax.io._
import net.liftweb.json.NoTypeHints


object App {

  def Creds = {
    val text = Source.fromFile("password.txt", "utf-8").getLines()
    new RunParams(text.next(), text.next(), text.next())
  }

  def main(args: Array[String]) {
    implicit val formats = net.liftweb.json.Serialization.formats(NoTypeHints)
    implicit val codec = scalax.io.Codec.UTF8

    val tcdp = new TeamCityDataProvider(Creds)
    var fileName = "facts"
    if (args.size > 0) {
      fileName = args.head
      val facts = tcdp.Facts(fileName) //"bt43"
      val output:Output = Resource.fromFile("./target/" + fileName + ".json")
      val factsJson = write(facts)
      output.write(factsJson)
    }

    val input:Input = Resource.fromFile("./target/" + fileName + ".json")
    val facts = read[List[BuildChangeFact]](input.slurpString)

    val Xinfo = tcdp.Fact2Matrix(facts)
    LinAlg.Matrix2Csv(Xinfo._1,Xinfo._2,"./target/" + fileName + ".csv")

    //val (X_norm, factors) = tcdp.FeatureNormalize(X)
    //println(X)

  }
}

// http://jesseeichar.github.com/scala-io-doc/0.2.0/index.html#!/overview
// https://github.com/scalala/Scalala
// http://dispatch.databinder.net/Dispatch.html
