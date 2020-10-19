package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int, Int>>.groupByBounds(bounds: List<Pair<Int, Int>>): MutableList<List<Pair<Int, Int>>> {
    val resultMap = this.groupBy(
       keySelector = { listItem ->
          bounds.indexOfFirst {
             listItem.first in it.first..it.second && listItem.second in it.first..it.second
          } }
    )

    val resultList = mutableListOf<List<Pair<Int,Int>>>()
    for (i in 0..bounds.size.dec())
        resultList.add(resultMap.getOrElse(i){listOf()})

    return resultList
}
