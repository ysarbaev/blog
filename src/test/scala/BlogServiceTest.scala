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

  "BlogService" should "pass full cycle" in {
    service.createPost(PostReq("post 1", "We don need no education"))  
    service.createPost(PostReq("post 2", "We don need no thought control"))  
    service.createPost(PostReq("post 2", "No dark sarcasm in the classroom"))  

    def posts = service.getTopPosts(0, 10)

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

}