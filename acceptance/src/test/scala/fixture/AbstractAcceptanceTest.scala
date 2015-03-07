package fixture

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import event.DomainEvent
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import org.scalatest._
import slackres.playcontext.application.PlayApplication
import spray.client.pipelining._
import spray.http.{FormData, HttpRequest, HttpResponse}
import spray.httpx.marshalling.BasicMarshallers.FormDataMarshaller

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object AbstractAcceptanceTest {
  val port = new AtomicInteger(3000)
}

abstract class AbstractAcceptanceTest extends TestKit(ActorSystem("acceptance"))
with UUIDGenerator
with ImplicitSender
with FeatureSpecLike with Matchers with GivenWhenThen
with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val executionContext = system.dispatcher

  implicit def json4sFormats: Formats = Serialization.formats(NoTypeHints)

  override def afterAll {
    playApplication.close()
    slackStubApplication.close()
    TestKit.shutdownActorSystem(system)
  }

  override def beforeAll {
    system.eventStream.subscribe(testActor, classOf[DomainEvent[_]])
    slackStubApplication.start()
    playApplication.start()
  }

  val playApplicationPort = AbstractAcceptanceTest.port.incrementAndGet()
  val slackStubPort = AbstractAcceptanceTest.port.incrementAndGet()

  val slackStubApplication = new SlackStubApplication(system, port = slackStubPort, host = "localhost")
  val playApplication = new PlayApplication(system, port = playApplicationPort, host = "localhost",
    slackUrl = s"http://localhost:$slackStubPort/services")

  def post(url: String, payload: FormData, timeout: Duration = 1.second): HttpResponse = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    Await.result(pipeline(Post(url, payload)), timeout)
  }
/*
  def postWithResponse[T: FromResponseUnmarshaller](url: String, payload: AnyRef, timeout: Duration = 1.second) = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val result = Await.result(pipeline(Post(url, payload)), timeout)
    val un = unmarshal[T]
    (result, un(result))
  }
  */

  def get[T](url: String, t: (HttpResponse) => T, timeout: Duration = 1.second) = {
    val pipeline: HttpRequest => Future[T] = sendReceive ~> t
    Await.result(pipeline(Get(url)), timeout)
  }

  def getJson(url: String, timeout: Duration = 1.second) = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val s = Await.result(pipeline(Get(url)), timeout).entity.asString
    parse(s)
  }

  def host: String = "http://localhost"

  def playCommandUrl: String = s"$host:$playApplicationPort/service"

  def playCommandUrl(s: String): String = s"$playCommandUrl/$s"

  def slackCommand(text: String,
            token: String = "token",
            team_id: String = "team_id",
            team_domain: String = "team_domain",
            channel_id: String = "channel_id",
            channel_name: String = "channel_name",
            user_id: String = "user_id",
            user_name: String = "user_name",
            command: String = "/res") = {
    post(playCommandUrl("form"), FormData(Seq(
      "token" -> token,
      "team_id" -> team_id,
      "team_domain" -> team_domain,
      "channel_id" -> channel_id,
      "channel_name" -> channel_name,
      "user_id" -> user_id,
      "user_name" -> user_name,
      "command" -> command,
      "text" -> text)))
  }

  def playQueryUrl: String = s"$host:$playApplicationPort/query"



}

