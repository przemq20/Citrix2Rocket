import Utils.ConfigReader
import akka.NotUsed
import akka.actor.Cancellable
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, RunnableGraph, Sink, Source }
import mail.{ InboxDownloader, MessageParser }

import javax.mail.Message
import scala.concurrent.duration._

class Main(interval: Int) {
  implicit val system:       ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "Citrix2Rocket")
  implicit val materializer: Materializer.type    = Materializer
  val inboxDownloader = new InboxDownloader
  val messageParser   = new MessageParser

  def constructGraph: RunnableGraph[Cancellable] = {
    val downloadMessageFlow: Flow[String, Option[Message], NotUsed] = Flow[String]
      .map(_ => inboxDownloader.getLatestCitrixMessage)

    val parseTokenFlow: Flow[Option[Message], Option[String], NotUsed] = Flow[Option[Message]].map(messageParser.getToken)

    val sink: Sink[Option[String], NotUsed] = Flow[Option[String]].to(Sink.foreach {
      case Some(value) => println(value)
      case None        =>
    })

    val source: Source[String, Cancellable] = Source
      .tick(0.second, interval.second, "tick")

    val graph: RunnableGraph[Cancellable] = source
      .via(downloadMessageFlow)
      .via(parseTokenFlow)
      .to(sink)

    graph
  }

  def run(): Cancellable = constructGraph.run()

}

object Main extends App {
  val interval = new ConfigReader("citrix2rocket.mail").getVariableInt("interval")
  val app      = new Main(interval)
  app.run()
}
