package Utils

import com.typesafe.config.{ Config, ConfigFactory }

import scala.util.Properties

class ConfigReader(configPath: String) {
  private val env = Properties.envOrElse("env", "local")
  private val resourceBasename = s"application-$env.conf"
  scribe.info(s"Reading config: $resourceBasename")
  private final val config: Config = ConfigFactory
    .load(resourceBasename)
    .getConfig(configPath)

  def getVariableString(variable: String): String = {
    scribe.info(s"Reading variable $variable")
    Properties.envOrElse(variable, config.getString(variable))
  }

  def getVariableInt(variable: String): Int = getVariableString(variable).toInt

  def getVariableList[T](variable: String): List[T] = config.getList(variable).toArray().toList.map(_.asInstanceOf[T])
}
