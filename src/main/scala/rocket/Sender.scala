package rocket

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class Sender {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "Http")

  def sendToTheRocket(message: String): Unit = {
    val data = makeMessageString(message)
    scribe.info(s"Sending $data")
    try {
      val headers: Seq[HttpHeader] = Seq(
        RawHeader("X-Auth-Token", RCEnvironment.TOKEN),
        RawHeader("X-User-Id", RCEnvironment.USER_ID),
        RawHeader("Content-type", "application/json"),
        RawHeader("Charset", "UTF-8")
      )

      val entity: RequestEntity = HttpEntity(
        ContentTypes.`application/json`,
        data
      )

      val request        = HttpRequest(method = POST, uri = RCEnvironment.SEND_MESSAGE, headers = headers, entity = entity)
      val responseFuture = Http().singleRequest(request)

      scribe.info(s"$request")

      responseFuture.onComplete {
        case Success(value)     => scribe.info(s"Response: $value")
        case Failure(exception) => scribe.error(s"Failure: $exception")
      }
    } catch {
      case e: Throwable => scribe.error(s"Error: ${e.getLocalizedMessage}")
    }
  }

  def makeMessageString(content: String): String = {
    val newlineChar    = "\\n"
    val whitespaceChar = "\u2001"
    val rawContent = content
      .replace("\n", newlineChar)
      .replace("\t", whitespaceChar * 3)
      .replace("\"", "")
      .replace("{", "")
      .replace("}", "")

    s"""{"message": {"rid": "${RCEnvironment.ROOM_ID}", "msg": "$rawContent "}}"""
  }

}
