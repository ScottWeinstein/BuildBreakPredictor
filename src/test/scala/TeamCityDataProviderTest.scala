package org.SW

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import Serialization.RunParams
import org.joda.time.{Period, DateTime}


@RunWith(classOf[JUnitRunner])
class TeamCityDataProviderTest extends Spec with MustMatchers {

  describe("Conversion of facts into matrix") {

    val single =  List(List(BuildChangeFact(1, "1", 1, "2011-11-11T16:27:10.000-05:00", true, 1, 2, "user1", Map("File1" -> 1))))
    val tcdp = new TeamCityDataProvider(RunParams("http://localhost:8080" ,"b", "c"))

    it("must convert a single change") {
      val (matrix, cols) = tcdp.Fact2Matrix(single, 0)
      println(cols)
      matrix.numRows must equal (1)
      matrix.numCols must equal (61)
      cols.size must equal (61)
    }
    
    it("must scan dates") {
      val a = new DateTime()
      val b = new DateTime()
      val c = new Period(b,a)
      c.getSeconds


    }
    
  }
}