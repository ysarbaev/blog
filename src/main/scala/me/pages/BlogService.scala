package me.pages

import scalikejdbc._
import scalikejdbc.config._
import me.pages._

class BlogService() {

  val MAX_PROLOG_LENGTH = 500
  val MAX_PROLOG_SENTENCES = 3
  val SENTENCE_SEPARATOR = '.'

  DBs.setupAll()

  DB.localTx { implicit session =>
    sql"""
      create table if not exists post
      (
        id bigint auto_increment primary key,
        title varchar not null,
        prolog_end_pos int not null,
        body varchar not null,
        created datetime not null default now(),
        updated datetime null,
        last_mod datetime as coalesce(updated, created)
      ); 
      
      create index if not exists post_last_mod_idx on post(last_mod);
    """.execute.apply()
    sql"""    
      create table if not exists comment
      (
        id bigint auto_increment primary key,
        postId bigint,
        title varchar not null,
        author varchar not null,
        body varchar not null,
        created datetime not null default now(),
        foreign key (postId) references post(id)
      );
    """.execute.apply()
    sql"""  
      create table if not exists comment_counter
      (
        postId bigint primary key,
        comments int check comments >= 0
      );
    """.execute.apply()

  }

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

  object FullPost extends SQLSyntaxSupport[FullPost] {
    override val tableName = "post"
    def apply(rs: WrappedResultSet): FullPost = 
      new FullPost(
        id = rs.long("id"),
        title = rs.string("title"),
        body = rs.string("body"),
        author = "author"
      )
  }

  object Comment extends SQLSyntaxSupport[Comment] {
    override val tableName = "comment"
    def apply(rs: WrappedResultSet): Comment = 
      new Comment(
        id = rs.long("id"),
        title = rs.string("title"),
        body = rs.string("body"),
        author = rs.string("author")
      )
  }

  def prologEnd(text: String) = TextUtils.prologEnd(text, MAX_PROLOG_LENGTH, MAX_PROLOG_SENTENCES, SENTENCE_SEPARATOR)


  def getTopPosts(offset: Int, limit: Int): List[ShortPost] = {
    DB.readOnly { implicit session =>
      sql""" 
        select * from (select 
               p.id, 
               p.title, 
               substring(p.body, 0, p.prolog_end_pos) as body, 
               cc.comments,
               p.created,
               p.updated
        from post p inner join comment_counter cc on p.id = cc.postId
        order by p.last_mod desc) limit ${limit} offset ${offset}
      """.map(ShortPost(_)).list.apply()
    }
  }

  def getPostWithComments(postId: Long, offset: Int, limit: Int): Option[FullPost] = {
    DB.readOnly { implicit session =>
      val post = 
      sql"""
        select * from post where id = ${postId};
      """.map(FullPost(_)).single.apply()
      val comments = 
      sql"""
        select * from 
          (select * from comment where postId = ${postId} order by id)
        limit ${limit} offset ${offset};
      """.map(Comment(_)).list.apply()
      post.map(_.copy(comments = comments))
    }
  }

  def createPost(post: PostReq) {
    val prologEndPos = prologEnd(post.body)
    DB.localTx { implicit session =>
      sql"""
        insert into post(title, prolog_end_pos, body) 
          values(${post.title}, 
                 ${prologEndPos}, 
                 ${post.body});
      """.update.apply()

      sql"""insert into comment_counter values(identity(), 0)""".update.apply()
    }
  }

  def updatePost(postId: Long, post: PostReq) {
    DB.localTx { implicit session =>
      sql"""
        update post set title = ${post.title}, body = ${post.body}, updated = now()
          where id = ${postId};
      """.update.apply()
    }
  }

  def deletePost(postId: Long) {
    DB.localTx { implicit session =>
      sql"""delete from comment where postId = ${postId};""".update.apply()
      sql"""delete from post where id = ${postId};""".update.apply()
      sql"""delete from comment_counter where postId = ${postId};""".update.apply()
    }
  }

  def createComment(postId: Long, comment: CommentReq) {
    DB.localTx { implicit session =>
      sql"""
        insert into comment(postId, title, author, body) 
          values(${postId},
                 ${comment.title},
                 ${comment.author},
                 ${comment.body});
        """.update.apply()
      sql"""update comment_counter set comments = comments + 1 where postId = ${postId};""".update.apply()
    }
  }

  def deleteComment(postId: Long, commentId: Long) {
    DB.localTx { implicit session =>
      sql"""delete from comment where id = ${commentId} and postId = ${postId};""".update.apply()
      sql"""update comment_counter set comments = comments - 1 where postId = ${postId};""".update.apply() 
    }
  }

  def close() {
    DBs.closeAll()
  }

}