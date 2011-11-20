package org.SW

import scalala.tensor.{Matrix, ::}
import scalax.io.{Seekable, Resource}

class LinAlg {
  def Matrix2Csv(mat: Matrix[Int], cols:List[String], fileName: String): Unit = {
    implicit val codec = scalax.io.Codec.UTF8

    val output:Seekable = Resource.fromFile(fileName)
    output.truncate(0)
    output.open(os => {

      os.append(cols.mkString(","))
      os.append("\n")

      for (ii <- 0 to mat.numRows-1) {
       val row = mat(ii,::)
        val sbrow = row.toList.foldLeft(new StringBuilder())((sb,v) => {
          sb.append(v)
          sb.append(",")
        })
        sbrow.length = sbrow.length - 1
        sbrow.append("\n")
        os.append(sbrow.toString())
      }
    })
  }
}

object LinAlg extends LinAlg

  //        val n = mat.numCols
  //      row.foreachPair( (j,v) => {
  //        output.write(v)
  //        if (j != n - 1)
  //          output.write(",")
  //        else
  //          output.write("\n")
  //      })
