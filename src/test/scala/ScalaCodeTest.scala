package org.SW

import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

case class foo(id: Int, desc: String)



@RunWith(classOf[JUnitRunner])
class ScalaCodeTest extends Spec with MustMatchers {

  describe("why does this compile") {

    def getIterableOfList: Iterable[List[Int]] = {
      Seq(List(1))
    }

    def methodTakesList(arg: List[Int]) = {
    }

    def getIterableOfList2: Iterable[List[foo]] = {
      Seq(List(foo(1,"a")))
    }

    def methodTakesList2(arg: List[foo]) = {
    }


    it("should not compile") {
      //methodTakesList(getIterableOfList)
      //methodTakesList2(getIterableOfList2)
    }
  }
}