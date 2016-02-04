package me.pages

import org.scalatest._
import org.scalatest.Assertions._

class BlogServiceTest extends FlatSpec with BeforeAndAfter{

  var service: BlogService = _

  before {
    service = new BlogService()
  }

  after {
    service.close()
  }

  def posts = service.getTopPosts(0, 10)

  "BlogService" should "pass full cycle" in {
    service.createPost(PostReq("post 1", "We don need no education"))  
    service.createPost(PostReq("post 2", "We don need no thought control"))  
    service.createPost(PostReq("post 3", "No dark sarcasm in the classroom"))  

    posts.foreach { p =>
      assert(p.comments == 0)
    }
    posts.foreach { p =>
      service.createComment(p.id, CommentReq("c for "+p.id, "nice", "Alice"))
    }
    posts.foreach { p =>
      assert(p.comments == 1)
    }

    posts.foreach { p =>
      service.deletePost(p.id)
    }

    assert(posts.size == 0)
  }

  "BlogService" should "update post and sort by last mod" in {
    service.createPost(PostReq("post 1", "We don need no education"))
    service.createPost(PostReq("post 2", "We don need no thought control"))

    println(posts.map(_.title))

    assert(posts.head.title == "post 2")

    service.updatePost(posts.head.id, PostReq("post 1 upd", "updated"))

    assert(posts.head.title == "post 1 upd")
  }

  "BlogService" should "remove comment" in {
    service.createPost(PostReq("post 1", "qwertyuio"))
    service.createComment(posts.head.id, CommentReq("title", "body", "author"))

    val fullPost = service.getPostWithComments(posts.head.id, 0, 10).get

    assert(fullPost.title == "post 1")
    assert(fullPost.comments.head.title == "title")
    assert(fullPost.comments.head.body == "body")
    assert(fullPost.comments.head.author == "author")
  }

}