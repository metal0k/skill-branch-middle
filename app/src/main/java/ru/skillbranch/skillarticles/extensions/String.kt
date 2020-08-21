package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {
  if (this.isNullOrBlank() || substr.isNullOrBlank())
    return listOf()
  var pos = -1
  val resultList = mutableListOf<Int>()
  do {
    pos = this.indexOf(substr, pos.inc(), ignoreCase)
    if (pos != -1)
      resultList.add(pos)
  } while (pos != -1)
  return resultList
}