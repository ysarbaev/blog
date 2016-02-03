package me.pages

object TextUtils {
  /*
    Finds the last position of the 
  */
  def prologEnd(
    text: String, 
    maxPrologLen: Int, 
    maxPrologSentences: Int, 
    sentenceSeparator: Char): Int = {

    val maxLen = if (text.length > maxPrologLen) maxPrologLen else text.length

    def end(searchFrom: Int, dotCounter: Int, endPos: Int): Int = {
      if (dotCounter == maxPrologSentences) {
        endPos
      } else if (searchFrom > maxLen) {
        maxLen
      } else {
        text.indexOf(sentenceSeparator, searchFrom) match {
          case -1 => maxLen
          case p if p > maxLen => maxLen
          case p => end(p + 1, dotCounter + 1, p)
        }
      }
    } 

    end(0, 0, 0)
  }
}