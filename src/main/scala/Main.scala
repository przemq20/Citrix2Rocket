import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Sink, Source }
import mail.{ InboxDownloader, MessageParser }
import rocket.Sender

import javax.mail.Message
import scala.concurrent.Future

class Main() {
  implicit val system:       ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "Citrix2Rocket")
  implicit val materializer: Materializer.type    = Materializer
  val inboxDownloader = new InboxDownloader
  val messageParser   = new MessageParser
  val rocketSender    = new Sender

  def constructGraph: Future[Option[String]] = {
    val downloadMessageFlow: Flow[String, Option[Message], NotUsed] = Flow[String]
      .map(_ => inboxDownloader.getLatestCitrixMessage)

    val parseTokenFlow: Flow[Option[Message], Option[String], NotUsed] = Flow[Option[Message]].map(messageParser.getToken)
    val sendToTheRocketFlow: Flow[Option[String], Option[String], NotUsed] = Flow[Option[String]].map { token =>
      token.foreach(tok => rocketSender.sendToTheRocket(tok))
      token
    }
    val sink: Sink[Option[String], Future[Option[String]]] = Sink.head[Option[String]]

    val source: Source[String, NotUsed] = Source
      .single("Tick")

    val graph = source
      .via(downloadMessageFlow)
      .via(parseTokenFlow)
      .via(sendToTheRocketFlow)

    graph.runWith(sink)
  }
}

object Main extends App {
  val app = new Main()

  val route = get {
    onSuccess(app.constructGraph) {
      case Some(_) => complete("Token has been send to rocket chat.")
      case None    => complete("There are no unread tokens.")
    }
  }
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "Server")

  val host = "0.0.0.0"
  val port: Int = sys.env.getOrElse("PORT", "8088").toInt

  Http().newServerAt(host, port).bindFlow(route)

}
