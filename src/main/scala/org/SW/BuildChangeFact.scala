package org.SW

case class BuildChangeFact(id: Int,
                           changeItemId: String,
                           changeId: Int,
                           date: String,
                           success: Boolean,
                           startMin: Int,
                           runDay: Int,
                           commiters: String,
                           fileChangeType: Map[String, Int])

