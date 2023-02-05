package mail

import javax.mail.Message
import scala.util.matching.Regex

class MessageParser {
  val length = 8
  val pattern: Regex = (s"([0-9]{$length})").r

  def getToken(message: Option[Message]): Option[String] = {
    message.flatMap(a => pattern.findFirstIn(a.getContent.toString))
  }
}
