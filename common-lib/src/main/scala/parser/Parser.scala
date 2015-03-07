package parser

import spray.http.StatusCode

trait Parser {

  def parse(input: SlackCommand): PartialFunction[List[String], ParseResponse]

}

sealed trait ParseResponse

case class StatusCodeResponse(code: StatusCode) extends ParseResponse
case class StatusCodeAndMessageResponse(code: StatusCode, message: AnyRef) extends ParseResponse