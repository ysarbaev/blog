package me.pages

import spray.json._
import spray.json.DefaultJsonProtocol
import scalikejdbc._

case class CommentReq(title: String, body: String, author: String)
case class PostReq(title: String, body: String)
case class Comment(title: String, body: String, author: String)
case class Blog(name: String, author: String)
case class ShortPost(
  id: Long,
  title: String,
  body: String, 
  created: Long,
  updated: Option[Long],
  comments: Int)
object ShortPost extends SQLSyntaxSupport[ShortPost] {
  override val tableName = "post"
  def apply(rs: WrappedResultSet) = new ShortPost(
    id = rs.long("id"),
    title = rs.string("title"),
    body = rs.string("body"),
    created = rs.jodaDateTime("created").getMillis(),
    updated = rs.jodaDateTimeOpt("updated").map(_.getMillis),
    comments = rs.int("comments")
  )
}


case class FullPost(
  id: Long,
  blog: String,
  author: String,
  title: String,
  comments: Seq[Comment]
)
object JsonProtocol extends DefaultJsonProtocol {
  implicit val commentFormatReq = jsonFormat3(CommentReq)
  implicit val postFormatReq = jsonFormat2(PostReq) 
  implicit val commentFormat = jsonFormat3(Comment)
  // implicit val shortPostFormat = jsonFormat6(ShortPost)
  implicit val fullPostFormat = jsonFormat5(FullPost)
  implicit val blogFormat = jsonFormat2(Blog)
}
