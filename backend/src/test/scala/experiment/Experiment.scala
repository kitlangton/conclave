//package experiment
//
//import quacro._
//import zio.blocking.Blocking
//import zio.query.ZQuery
//import zio.{ExitCode, Has, UIO, URIO}
//import zymposium.model._
//
//import java.sql.{Connection, SQLException}
//import java.util.UUID
//
//object Experiment extends zio.App {
//  import QuillContext._
//
//  val AccountDS: GenericDataSource[Account] = Quacros.gen[Account]
//  val CommentDS: GenericDataSource[Comment] = Quacros.gen[Comment]
//
//  def getAccount(email: String) =
//    AccountDS.get(_.email)(email).map(_.head)
//
//  def getComments(accountId: UUID): ZQuery[Has[Connection] with Blocking, Nothing, List[Comment]] =
//    CommentDS.get(_.accountId)(accountId)
//
//  def getAccountAndComments(email: String): ZQuery[Has[Connection] with Blocking, Nothing, (Account, List[Comment])] =
//    for {
//      account  <- getAccount(email)
//      comments <- getComments(account.id)
//    } yield account -> comments
//
//  val program: ZQuery[Has[Connection] with Blocking, SQLException, List[(Account, List[Comment])]] =
//    ZQuery.foreachPar(List("kit.langton@gmail.com", "adam.fraser@zmail.com"))(getAccountAndComments)
//
//  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
//    program.run
//      .tap { r =>
//        UIO {
//          r.foreach { case (account, value) =>
//            println(s"account = $account")
//            println(s"comments = \n${value.mkString("\n")}")
//            println("")
//          }
//        }
//      }
//      .debug("RESULT")
//      .provideCustomLayer(QuillContext.live)
//      .exitCode
//
//}
