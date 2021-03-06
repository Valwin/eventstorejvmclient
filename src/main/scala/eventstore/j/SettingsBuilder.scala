package eventstore
package j

import Settings.Default
import java.net.InetSocketAddress
import Builder.RequireMasterSnippet
import scala.concurrent.duration._

/**
 * @author Yaroslav Klymko
 */
class SettingsBuilder extends Builder[Settings] with RequireMasterSnippet[SettingsBuilder] {
  var _address = Default.address
  var _maxReconnections = Default.maxReconnections
  var _reconnectionDelay = Default.reconnectionDelay
  var _defaultCredentials = Default.defaultCredentials
  var _heartbeatInterval = Default.heartbeatInterval
  var _heartbeatTimeout = Default.heartbeatTimeout
  var _connectionTimeout = Default.connectionTimeout
  var _backpressureSettings = Default.backpressureSettings

  def address(x: InetSocketAddress): SettingsBuilder = set {
    _address = x
  }

  def address(host: String): SettingsBuilder = address(new InetSocketAddress(host, Default.address.getPort))

  def maxReconnections(x: Int) = set {
    _maxReconnections = x
  }

  def requireMaster(x: Boolean) = RequireMasterSnippet.requireMaster(x)

  def reconnectionDelay(x: FiniteDuration): SettingsBuilder = set {
    _reconnectionDelay = x
  }

  def reconnectionDelay(length: Long, unit: TimeUnit): SettingsBuilder = reconnectionDelay(FiniteDuration(length, unit))

  def reconnectionDelay(length: Long): SettingsBuilder = reconnectionDelay(length, SECONDS)

  def defaultCredentials(x: Option[UserCredentials]): SettingsBuilder = set {
    _defaultCredentials = x
  }

  def defaultCredentials(x: UserCredentials): SettingsBuilder = defaultCredentials(Option(x))

  def defaultCredentials(login: String, password: String): SettingsBuilder =
    defaultCredentials(UserCredentials(login = login, password = password))

  def noDefaultCredentials = defaultCredentials(None)

  def heartbeatInterval(x: FiniteDuration): SettingsBuilder = set {
    _heartbeatInterval = x
  }

  def heartbeatInterval(length: Long, unit: TimeUnit): SettingsBuilder = heartbeatInterval(FiniteDuration(length, unit))

  def heartbeatInterval(length: Long): SettingsBuilder = heartbeatInterval(length, SECONDS)

  def heartbeatTimeout(x: FiniteDuration): SettingsBuilder = set {
    _heartbeatTimeout = x
  }

  def heartbeatTimeout(length: Long, unit: TimeUnit): SettingsBuilder = heartbeatTimeout(FiniteDuration(length, unit))

  def heartbeatTimeout(length: Long): SettingsBuilder = heartbeatTimeout(length, SECONDS)

  def connectionTimeout(x: FiniteDuration): SettingsBuilder = set {
    _connectionTimeout = x
  }

  def connectionTimeout(length: Long, unit: TimeUnit): SettingsBuilder = connectionTimeout(FiniteDuration(length, unit))

  def connectionTimeout(length: Long): SettingsBuilder = connectionTimeout(length, SECONDS)

  def backpressureSettings(x: BackpressureSettings): SettingsBuilder = set {
    _backpressureSettings = x
  }

  def build = Settings(
    address = _address,
    maxReconnections = _maxReconnections,
    requireMaster = RequireMasterSnippet.value,
    reconnectionDelay = _reconnectionDelay,
    defaultCredentials = _defaultCredentials,
    heartbeatInterval = _heartbeatInterval,
    heartbeatTimeout = _heartbeatTimeout,
    connectionTimeout = _connectionTimeout,
    backpressureSettings = _backpressureSettings)
}