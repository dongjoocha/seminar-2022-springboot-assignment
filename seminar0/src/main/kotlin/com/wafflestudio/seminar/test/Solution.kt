package com.wafflestudio.seminar.test

/**
 * TODO
 *   3번을 코틀린으로 다시 한번 풀어봐요.
 *   객체를 통한 구조화를 시도해보면 좋아요 :)
 */

object Command {
    const val MOVE = "move"
    const val DELETE = "delete"
    const val RESTORE = "restore"
    const val LIST = "list"
    const val QUIT = "q"
}

class StudentChart(
    private val chart: MutableList<String>,
    private val deletedList: MutableList<Pair<String, Int>>,
    private var selectedRow: Int = 0
) {
    fun process(input: List<String>) {
        val cmd = input[0]
        when (cmd) {
            Command.MOVE -> {
                val direction = input[1]
                val num = input[2].toInt()
                val nextRow = if (direction == "-u") selectedRow - num else selectedRow + num

                if (nextRow in chart.indices) {
                    selectedRow = nextRow
                } else {
                    println("Error 100")
                }
            }

            Command.DELETE -> {
                deletedList.add(Pair(chart.removeAt(selectedRow), selectedRow))
                if (selectedRow == chart.size) selectedRow -= 1
            }

            Command.RESTORE -> {
                if (deletedList.isEmpty()) {
                    println("Error 200")
                } else {
                    val student = deletedList.removeLast()
                    val name = student.first
                    val idx = student.second
                    chart.add(idx, name)
                    if (idx <= selectedRow) selectedRow += 1
                }
            }

            Command.LIST -> {
                for (student in chart) {
                    println(student)
                }
            }
        }
    }
}

fun main() {
    val chartRegex = Regex("(?<=\")\\w{1,6}(?=\")")
    val chart = chartRegex.findAll(readLine().toString()).map { it.value }.toMutableList()
    val deletedList = mutableListOf<Pair<String, Int>>()
    var input: List<String>

    val studentChart = StudentChart(chart, deletedList)

    while (true) {
        input = readLine()!!.split(" ")

        if (input[0] == Command.QUIT) {
            break
        } else {
            studentChart.process(input)
        }
    }
}