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

  def prologEnd(text: String) = TextUtils.prologEnd(text, MAX_PROLOG_LENGTH, MAX_PROLOG_SENTENCES, SENTENCE_SEPARATOR)


  def getTopPosts(offset: Int, limit: Int): List[ShortPost] = {
    DB.readOnly { implicit session =>
      sql""" 
        select p.id, 
               p.title, 
               substring(p.body, 0, p.prolog_end_pos) as body, 
               cc.comments,
               p.created,
               p.updated
        from post p inner join comment_counter cc on p.id = cc.postId
        order by p.last_mod
        limit ${limit} offset ${offset};
      """.map(ShortPost(_)).list.apply()
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
        update post where id = ${postId} 
          set title = ${post.title}, body = ${post.body}, updated = now();
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