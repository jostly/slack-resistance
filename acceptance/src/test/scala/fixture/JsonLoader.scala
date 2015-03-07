package fixture

import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read

import scala.io.Source

object JsonLoader {
  implicit private[this] val formats = Serialization.formats(NoTypeHints)

  def load[T](filename: String)(implicit manifest: Manifest[T]) = read[T](Source.fromURL(getClass.getClassLoader.getResource(filename)).mkString)

}
