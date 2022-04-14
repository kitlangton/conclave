//package zymposium
//
//import zio.{Chunk, ExitCode, Has, UIO, URIO, ZIO}
//import zymposium.model.{Account, AccountId, Comment, CommentId}
//import zio.query._
//
//import java.sql.{Connection, SQLException}
//import java.util.UUID
//import QuillContext._
//
//object Queries extends zio.App {
//
//  type GetAccounts = GetAccounts.type
//  final case object GetAccounts extends Request[SQLException, List[Account]]
//
//  sealed trait CommentRequest[+A] extends Request[SQLException, A] with Serializable {
//    def insert[A1 >: A](a: A1): Any = a
//  }
//
//  object Insert {
//    def apply[A](request: Request[_, A])(a: A): Any = a
//  }
//
//  final case class GetCommentsByAccountId(accountId: AccountId) extends CommentRequest[List[Comment]]
//  final case class GetCommentsById(id: CommentId)               extends CommentRequest[List[Comment]]
//
//  def getCommentById(id: CommentId): ZQuery[Has[Connection], SQLException, Option[Comment]] =
//    ZQuery.fromRequest(GetCommentsById(id))(CommentsDataSource).map(_.headOption)
//
//  implicit val encodeUUID: MappedEncoding[UUID, String] = MappedEncoding[UUID, String](_.toString)
//  implicit val decodeUUID: MappedEncoding[String, UUID] = MappedEncoding[String, UUID](UUID.fromString)
//
//  val getAccountsDataSource: DataSource[Has[Connection], GetAccounts] =
//    DataSource.fromFunctionM("GetAccounts")(_ => getAccountsZIO)
//
//  val getCommentsDataSource: DataSource[Has[Connection], GetCommentsByAccountId] =
//    DataSource.fromFunctionBatchedM("GetAccounts") { requests =>
//      for {
//        comments <- getCommentsZIO(requests.map(_.accountId))
//        map       = comments.groupBy(_.accountId)
//      } yield requests.map(r => map.getOrElse(r.accountId, List.empty))
//    }
//
//  val CommentsDataSource: DataSource[Has[Connection], CommentRequest[List[Comment]]] =
//    DataSource.fromFunctionBatchedM("CommentsDataSource") { requests =>
//      val byId: Chunk[GetCommentsById]               = requests.collect { case r @ GetCommentsById(_) => r }
//      val byAccountId: Chunk[GetCommentsByAccountId] = requests.collect { case r @ GetCommentsByAccountId(_) => r }
//
//      getCommentsComplexZIO(byId.map(_.id), byAccountId.map(_.accountId)).map { comments =>
//        val byIdMap        = comments.groupBy(_.id)
//        val byAccountIdMap = comments.groupBy(_.accountId)
//
//        requests.map {
//          case GetCommentsByAccountId(accountId) => byAccountIdMap.getOrElse(accountId, List.empty)
//          case GetCommentsById(id)               => byIdMap.getOrElse(id, List.empty)
//        }
//      }
//    }
//
////  val CommentsDataSource: DataSource[Has[Connection], CommentRequest[Any]] =
////    DataSource.Batched.make("CommentsDataSource") { requests =>
////      val byId: Chunk[GetCommentsById]               = requests.collect { case r @ GetCommentsById(_) => r }
////      val byAccountId: Chunk[GetCommentsByAccountId] = requests.collect { case r @ GetCommentsByAccountId(_) => r }
////
////      getCommentsComplexZIO(byId.map(_.id), byAccountId.map(_.accountId))
////        .map { comments =>
////          val byIdMap        = comments.groupBy(_.id)
////          val byAccountIdMap = comments.groupBy(_.accountId)
////
////          requests.foldLeft(CompletedRequestMap.empty) { (map, request) =>
////            request match {
////              case r @ GetCommentsByAccountId(accountId) =>
////                map.insert(r)(Right(byAccountIdMap.getOrElse(accountId, List.empty)))
////              case r @ GetCommentsById(id) =>
////                map.insert(r)(Right(byIdMap.getOrElse(id, List.empty)))
////            }
////          }
////        }
////        .catchAll { e =>
////          UIO(requests.foldLeft(CompletedRequestMap.empty)(_.insert(_)(Left(e))))
////        }
////    }
//
//  def getCommentsComplexZIO(
//      ids: Chunk[CommentId],
//      accountIds: Chunk[AccountId]
//  ): ZIO[Has[Connection], SQLException, List[Comment]] =
//    ZIO.debug(s"GETTING COMMENTS FOR accountIds $accountIds and ids $ids") *>
//      QuillContext.run {
//        query[Comment]
//          .filter(c => liftQuery(accountIds).contains(c.accountId) || liftQuery(ids).contains(c.id))
//      }
//
//  def getCommentsZIO(accountIds: Chunk[AccountId]): ZIO[Has[Connection], SQLException, List[Comment]] =
////    val list = accountIds.toList
//    // SELECT c.id, c.account_id, c.text FROM comment c WHERE c.account_id = ANY(?)
//    ZIO.debug(s"GETTING COMMENTS FOR $accountIds") *>
//      QuillContext.run {
//        query[Comment].filter(c => liftQuery(accountIds).contains(c.accountId))
//      }
//
//  def getCommentsZIO(accountId: AccountId): ZIO[Has[Connection], SQLException, List[Comment]] =
//    ZIO.debug(s"GETTING COMMENTS FOR $accountId") *>
//      QuillContext.run(query[Comment].filter(_.accountId == lift(accountId)))
//
//  def getAccountsZIO: ZIO[Has[Connection], SQLException, List[Account]] =
//    ZIO.debug(s"GETTING ACCOUNTS") *>
//      QuillContext.run(query[Account])
//
//  def getComments(accountId: AccountId): ZQuery[Has[Connection], SQLException, List[Comment]] =
//    ZQuery.fromRequest(GetCommentsByAccountId(accountId))(CommentsDataSource)
//
//  def getAccounts: ZQuery[Has[Connection], SQLException, List[Account]] =
//    ZQuery.fromRequest(GetAccounts)(getAccountsDataSource)
//
//  val programZIO: ZIO[Has[Connection], SQLException, List[(Account, List[Comment])]] =
//    for {
//      accounts <- getAccountsZIO
//      comments <- ZIO.foreachPar(accounts) { account =>
//                    getCommentsZIO(account.id).map(account -> _)
//                  }
//    } yield comments
//
//  val programZQuery: ZQuery[Has[Connection], SQLException, List[(Account, List[Comment])]] =
//    for {
//      accounts <- getAccounts
//      comments <- ZQuery.foreachPar(accounts) { account =>
//                    getComments(account.id).map(account -> _)
//                  }
//    } yield comments
//
//  val example =
//    getCommentsZIO(AccountId(UUID.fromString("de23733b-a2af-4dbd-8500-1be9637fdbd3")))
//
//  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
//    programZQuery.run
////    programZIO
//      .tap { accountsWithComments =>
//        ZIO.debug(accountsWithComments.mkString("\n"))
//      }
//      .debug("RESULTS")
//      .provideLayer(QuillContext.live)
//      .exitCode
//}
