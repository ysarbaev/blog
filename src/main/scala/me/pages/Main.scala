package me.pages

import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import me.pages._
import me.pages.JsonProtocol._

object Main extends App {

	val config = ConfigFactory.load()
	val httpConfig = config.getConfig("http")
	val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val blogService = new BlogService()

	implicit val system = ActorSystem()

	implicit val actorMaterializer = ActorMaterializer()

	Http().bindAndHandle(routesLogged, httpInterface, httpPort)

  def routesLogged = logRequestResult("akka-http")(routes)
	def routes = {
		path("") {
			get {
				complete {
					Blog("my blog", "yuri")
				}
			}
		} ~
		pathPrefix("post" / IntNumber) { postId =>
			get {
				complete {
					s"""get: $postId is requested"""
				}
			}
		} ~
		path("post") {
			put {
				entity(as[PostReq]) { post =>
					complete {
						s"""patch: $post"""
					}
				}
			}
		} ~
		pathPrefix("post" / IntNumber) { postId =>
			patch {
				entity(as[PostReq]) { post =>
					complete {
						s"""patch: $post """
					}
				}
			}
		} ~
		pathPrefix("post" / IntNumber) { postId =>
			delete {
				complete {
					s"""delete: $postId"""
				}
			}
		} ~
		pathPrefix("post" / IntNumber / "comment") { postId =>
			put {
				entity(as[CommentReq]) { comment =>
					complete {
						s"""put: comment = $comment for $postId """
					}
				}
			}
		} ~
		pathPrefix("post" / IntNumber / "comment" / IntNumber) { (postId, commentId) =>
			delete {
				complete {
					s"""delete: $postId / $commentId"""
				}
			}
		}

	}

}