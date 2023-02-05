package mail
import Utils.ConfigReader

import java.util.Properties
import javax.mail.Flags.Flag
import javax.mail.internet.InternetAddress
import javax.mail.search.FlagTerm
import javax.mail.{ Authenticator, Flags, Folder, Message, PasswordAuthentication, Session, Store }

class InboxDownloader {
  val config = new ConfigReader("citrix2rocket.mail")
  val snrAddress: String  = config.getVariableString("snr_mail")
  val session:    Session = createSession
  val store:      Store   = session.getStore("imap")
  store.connect()

  def createSession: Session = {
    val properties = new Properties()

    properties.setProperty("mail.store.protocol", "imap")
    //  properties.setProperty("mail.debug", "true");
    properties.put("mail.imap.host", config.getVariableString("host"))
    properties.put("mail.imap.port", config.getVariableString("port"))
    properties.put("mail.imap.ssl.enable", "true")
    properties.put("mail.imap.auth", true)

    val username = config.getVariableString("username")
    val password = config.getVariableString("password")

    val session: Session = Session.getInstance(
      properties,
      new Authenticator {
        override def getPasswordAuthentication: PasswordAuthentication = {
          new PasswordAuthentication(username, password);
        }
      }
    )
    session
  }

  def filterMessages(messages: Array[Message]): Array[Message] = {
    messages.filter(_.getFrom.contains(new InternetAddress(snrAddress)))
  }

  def getMessagesFromFolder(folder: String): Array[Message] = {
    val inbox: Folder = store.getFolder(folder)
    inbox.open(Folder.READ_WRITE)
    val inboxMessages: Array[Message] = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false))
    val filteredInboxMessages = filterMessages(inboxMessages)
    inbox.setFlags(filteredInboxMessages, new Flags(Flag.SEEN), true)
    filteredInboxMessages
  }

  def getCitrixMessages: List[Message] = {

    val filteredInboxMessages  = getMessagesFromFolder("Inbox")
    val filteredCitrixMessages = getMessagesFromFolder("Citrix")

    val emails: List[Message] = (filteredInboxMessages ++ filteredCitrixMessages).toList
    emails.sortWith((a, b) => a.getReceivedDate.after(b.getReceivedDate))
  }

  def getLatestCitrixMessage: Option[Message] = getCitrixMessages.headOption
}
