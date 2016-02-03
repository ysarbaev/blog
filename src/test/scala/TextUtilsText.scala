import org.scalatest.FlatSpec
import org.scalatest.Assertions._
import me.pages.TextUtils._

class TextUtilsTest extends FlatSpec {

  val textSample = "Hi. My name is Y. How a y?"
  behavior of "prologEnd"

  textSample should "10" in {
    assert(
      prologEnd(textSample, 10, 2, '.') == 10
    )
  }

  textSample should "16" in {
    assert(
      prologEnd(textSample, 16, 2, '.') == 16
    )
  }

  textSample should "26" in {
    assert(
      prologEnd(textSample, 100, 3, '.') == 26
    )
  }

  textSample should "2" in {
    assert(
      prologEnd(textSample, 2, 10, '.') == 2
    )
  }

  "empty string" should "0" in {
    assert(
      prologEnd("", 10, 10, '.') == 0
    )
  }

  "aaa" should "0" in {
    assert(
      prologEnd("aaa", 0, 10, '.') == 0
    )
  }

}