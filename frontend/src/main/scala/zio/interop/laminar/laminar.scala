package zio.interop

import com.raquo.laminar.api.L._
import zio._
import zio.internal.{Executor, Platform}
import zio.stream.ZStream

import scala.concurrent.ExecutionContext
import scala.scalajs.js

package object laminar {

  def runtimeFromLayerJS[R <: Has[_]](layer: ULayer[R]): Runtime.Managed[R] = {
    val scalaJSExecutor =
      Executor.fromExecutionContext(Int.MaxValue) {
        new ExecutionContext {
          def execute(runnable: Runnable): Unit =
            runnable.run()
          def reportFailure(cause: Throwable): Unit =
            cause.printStackTrace()
        }
      }

    Runtime
      .unsafeFromLayer(layer, Platform.fromExecutor(scalaJSExecutor))
      .withExecutor(Runtime.default.platform.executor)
  }

  implicit final class ZioEventStreamOps[A](val self: EventStream[A]) extends AnyVal {
    def ++[A1 >: A](that: EventStream[A1]): EventStream[A1] =
      EventStream.merge(self, that)
  }

  implicit final class ZioOps[E, A](val self: ZIO[ZEnv, E, A]) extends AnyVal {
    def runAsync(): Unit =
      Runtime.default.unsafeRunAsync_(self)

    def toSignal(initial: => A): Signal[A] =
      toEventStream.toSignal(initial)

    def toEventStream: EventStream[A] = {
      val promise = new js.Promise[A]((success, fail) =>
        Runtime.default.unsafeRunAsync_(
          self
            .tapBoth(
              e => UIO(fail(e)),
              { a =>
                UIO(success(a))
              }
            )
        )
      )
      EventStream.fromJsPromise(promise)
    }
  }

  implicit final class ZStreamOps[E <: Throwable, A](val self: ZStream[ZEnv, E, A]) extends AnyVal {
    def toSignal(initial: => A): Signal[A] =
      toEventStream.toSignal(initial)

    def toEventStream: EventStream[A] =
      EventStream.fromCustomSource[A](
        start = { (next, error, index, isStarted) =>
          Runtime.default.unsafeRunAsync_(
            self.tap { value =>
              UIO(next(value))
            }.runDrain
          )
        },
        stop = index => ()
      )

  }

}
