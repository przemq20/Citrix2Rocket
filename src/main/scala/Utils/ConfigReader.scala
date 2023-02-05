package Utils

import com.typesafe.config.{ Config, ConfigFactory }

import scala.util.Properties

class ConfigReader(configPath: String) {
  private val env = Properties.envOrElse("env", "local")
  private final val config: Config = ConfigFactory
    .load(s"application-$env.conf")
    .getConfig(configPath)

  def getVariableString(variable: String): String = {
    Properties.envOrElse(variable, config.getString(variable))
  }

  def getVariableInt(variable: String): Int = {
    Properties.envOrElse(variable, config.getString(variable)).toInt
  }

  def getVariableList[T](variable: String): List[T] = config.getList(variable).toArray().toList.map(_.asInstanceOf[T])
}
