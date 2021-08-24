//package quacro
//
//import io.getquill.context.ZioJdbc.{DataSourceLayer, QDataSource}
//import io.getquill.{PostgresZioJdbcContext, SnakeCase}
//import zio.blocking.Blocking
//import zio.query._
//import zio.{Chunk, Has, ZLayer}
//
//import java.sql.Connection
//import scala.language.experimental.macros
//import scala.reflect.macros.blackbox
//
//object QuillContext extends PostgresZioJdbcContext(SnakeCase) {
//  val live: ZLayer[Blocking, Nothing, Has[Connection]] =
//    (QDataSource.fromPrefix("postgresDB") >>> DataSourceLayer.live).orDie
//}
//
//case class GenericRequest[A](field: String, value: Any) extends Request[Nothing, List[A]]
//
//object GenericRequest {
//
//  def extractRequests[V](label: String, requests: Chunk[GenericRequest[_]]): Set[V] =
//    requests
//      .collect { case GenericRequest(field, values) if field == label => values }
//      .toSet
//      .asInstanceOf[Set[V]]
//}
//
//case class GenericDataSource[A](dataSource: DataSource[Has[Connection], GenericRequest[A]]) {
//  def get[V](select: A => V)(value: V): ZQuery[Has[Connection], Nothing, List[A]] =
//    macro Quacros.get_impl[A, V]
//
//  def makeRequest(request: GenericRequest[A]): ZQuery[Has[Connection], Nothing, List[A]] =
//    ZQuery.fromRequest(request)(dataSource)
//}
//
//private class Quacros(val c: blackbox.Context) {
//  import c.universe._
//
//  case class Param(label: String, tpe: Type)
//
//  def get_impl[S: WeakTypeTag, A: WeakTypeTag](select: c.Tree)(value: c.Tree): c.Tree =
//    select match {
//      case q"{ ($_) => $_.${name} }" =>
//        val request = q"GenericRequest[${c.weakTypeOf[S]}](${name.toString}, $value)"
//        q"${c.prefix}.makeRequest($request)"
//    }
//
//  def debug_impl[T: c.WeakTypeTag]: c.Tree = {
//    val dataSource = createDataSource_impl[T]
//
//    val block =
//      q"""
//val ds = $dataSource
//GenericDataSource(ds)
//         """
//
////    println("RESULT")
////    println(block)
//    block
//  }
//
//  def createDataSource_impl[T: c.WeakTypeTag]: c.Tree = {
//    val tpe   = c.weakTypeOf[T]
//    val block = q"""
//zio.query.DataSource.fromFunctionBatchedM("DerivedPersonDataSource") { (requests: zio.Chunk[GenericRequest[$tpe]]) =>
//  zio.ZIO.debug(${s"RUNNING QUERY ${tpe.toString} — "} + requests.toString) *>
//  QuillContext.run(${query_impl[T]}).map { results =>
//    ..${createGroupBys[T]}
//    ${createMatch[T]}
//  }
//}
//       """
//
////    println("RESULT")
////    println(block)
//    block
//  }
//
//  def createMatch[T: WeakTypeTag] = {
//    val params = getParams[T]
//    val cases: List[CaseDef] = params.map { param =>
//      val mapName = TermName(s"${param.label}Map")
//      cq"GenericRequest(${param.label}, v) => $mapName.getOrElse(v.asInstanceOf[${param.tpe}], List.empty)"
//    }.toList
//
//    q"""
//requests.map { request =>
//  ${Match(q"request", cases :+ cq"_ => throw new Error(${"NOPE"})")}
//}
//         """
//  }
//
//  def createGroupBys[T: WeakTypeTag] =
//    getParams[T].map(createGroupBy)
//
//  def createGroupBy(param: Param) = {
//    val selector = q"""_.${TermName(param.label)}"""
//    val mapName  = TermName(s"${param.label}Map")
//    q"""val $mapName = results.groupBy($selector)"""
//  }
//
//  private def getParams[T: c.WeakTypeTag] =
//    c.weakTypeOf[T]
//      .decls
//      .filter(d => d.toString.contains("value") && d.isPublic)
//      .map(_.asMethod)
//      .map { method =>
//        Param(method.name.toString, method.typeSignature.resultType)
//      }
//
//  def query_impl[T: c.WeakTypeTag] = {
//    val tpe    = c.weakTypeOf[T]
//    val params = getParams[T]
//
//    val filters = params.map { param =>
//      q"""
//liftQuery(${extractRequest(param)}).contains(v.${TermName(param.label)})
//         """
//    }
//
//    val result = q"""
//query[$tpe].filter { v => ${filters.reduce((a, b) => q"$a || $b")} }
//       """
//
//    println(result)
//    result
//  }
//
//  def extractRequest(param: Param): c.Tree =
//    q"""GenericRequest.extractRequests[${param.tpe}](${param.label}, requests)"""
//
//}
//
//object Quacros {
//  def query[T]: CompletedRequestMap = macro Quacros.query_impl[T]
//  def debug[T]: DataSource[Has[Connection] with Blocking, GenericRequest[T]] = macro Quacros.debug_impl[T]
//  def gen[T]: GenericDataSource[T] = macro Quacros.debug_impl[T]
//}
