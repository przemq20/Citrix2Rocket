package rocket

import Utils.ConfigReader

object RCEnvironment {
  private final val config = new ConfigReader("citrix2rocket.rocket")

  final val HOST:     String = config.getVariableString("rc_host")
  final val ROOM_ID:  String = config.getVariableString("rc_room_id")
  final val API_PATH: String = "/api/v1"
  final val TOKEN:    String = config.getVariableString("rc_token")
  final val USER_ID:  String = config.getVariableString("rc_user_id")

  final val CORE_URL: String = s"$HOST$API_PATH"
  final val SEND_MESSAGE = s"$CORE_URL/chat.sendMessage"
  final val ROOMS_INFO   = s"$CORE_URL/rooms.info?roomId=$ROOM_ID"
}
