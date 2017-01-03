package org.hanuna

import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess


fun File.createOutputFile(postfix: String): File {
    val absolutePath = parent + File.separator + nameWithoutExtension + "-$postfix." + extension
    return File(absolutePath)
}

val charset = Charset.forName("IBM866")
var debug = false

enum class Districts(val rusName: String) {
    adm("Адмиралтейский"),
    vas("Василеостровский"),
    vyb("Выборгский"),
    kal("Калининский"),
    kir("Кировский"),
    kol("Колпинский"),
    krag("Красногвардейский"),
    krasn("Красносельский"),
    kronsh("Кронштадтский"),
    kur("Курортный"),
    mos("Московский"),
    nev("Невский"),
    petrogv("Петроградский"),
    petrdv("Петродворцовый"),
    pri("Приморский"),
    push("Пушкинский"),
    fr("Фрунзенский"),
    chentr("Центральный"),
    other("--------------------")
}

enum class DistrictLinePrefix() {
    six,
    ml,
    st,
    euler;
    val prefix get() = "\\${name}"
}

sealed class TypedLine {
    abstract val text: String
    data class Other(override val text: String): TypedLine()

    data class DistrictLine(override val text: String, val district: Districts): TypedLine()
}

fun String.toTypedLine(): TypedLine {
    if (DistrictLinePrefix.values().none { startsWith(it.prefix) }) return TypedLine.Other(this)
    val district = Districts.values().firstOrNull {
        contains(it.rusName)
    } ?: Districts.other
    return TypedLine.DistrictLine(this, district)
}

fun Districts.writeForIt(inputFile: File, typedLines: List<TypedLine>) {
    val outputFile = inputFile.createOutputFile(name)
    if (debug) println("Output file: ${outputFile.absolutePath}")
    val writer = outputFile.printWriter(charset)

    for (line in typedLines) {
        when (line) {
            is TypedLine.Other -> writer.println(line.text)
            is TypedLine.DistrictLine -> if (line.district == this) writer.println(line.text)
        }
    }
    writer.close()
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Please specify input file")
        return
    }
    debug = args.size > 1

    val fileName = args[0]
    val inputFile = File(fileName)
    if (debug) println("Input file: ${inputFile.absolutePath}")

    val inputLines = inputFile.readLines(charset)
    val typedInputLines = inputLines.map(String::toTypedLine)

    Districts.values().forEach { district ->
        if (typedInputLines.any { it is TypedLine.DistrictLine && it.district == district }) {
            district.writeForIt(inputFile, typedInputLines)
        }
    }
}