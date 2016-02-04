package me.pages

import spray.json._
import spray.json.DefaultJsonProtocol
import scalikejdbc._

case class CommentReq(title: String, body: String, author: String)
case class PostReq(title: String, body: String)
case class Comment(id: Long, title: String, body: String, author: String)
case class Blog(name: String, author: String)
case class ShortPost(
  id: Long,
  title: String,
  body: String, 
  created: Long,
  updated: Option[Long],
  comments: Int)

case class FullPost(
  id: Long,
  title: String,
  body: String,
  author: String,
  comments: Seq[Comment] = Nil
)
object JsonProtocol extends DefaultJsonProtocol {
  implicit val commentFormatReq = jsonFormat3(CommentReq)
  implicit val postFormatReq = jsonFormat2(PostReq) 
  implicit val commentFormat = jsonFormat4(Comment)
  implicit val shortPostFormat = jsonFormat6(ShortPost)
  implicit val fullPostFormat = jsonFormat5(FullPost)
  implicit val blogFormat = jsonFormat2(Blog)
}
