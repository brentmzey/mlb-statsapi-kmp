package com.sabermetrics.statsapi.formatters

import com.sabermetrics.statsapi.models.MlbBoxscoreData
import com.sabermetrics.statsapi.models.MlbLinescoreData

/**
 * Formats structured MLB models into clean terminal ASCII tables matching Python `statsapi.boxscore` and `statsapi.linescore`.
 */
object MlbTableFormatter {

    fun formatBoxscore(box: MlbBoxscoreData): String {
        val rowLen = 79
        val sb = StringBuilder()
        val divLine = "-".repeat(rowLen) + " | " + "-".repeat(rowLen) + "\n"

        // Headers
        sb.append(box.awayTeamName.padEnd(rowLen)).append(" | ").append(box.homeTeamName.padEnd(rowLen)).append("\n")
        sb.append(divLine)

        // Batting Box
        sb.append(
            "${"Batters".padEnd(40)} ${"AB".padStart(3)} ${"R".padStart(3)} ${"H".padStart(3)} ${"RBI".padStart(3)} ${"BB".padStart(3)} ${"K".padStart(3)} ${"LOB".padStart(3)} ${"AVG".padStart(4)} ${"OPS".padStart(5)} | "
        )
        sb.append(
            "${"Batters".padEnd(40)} ${"AB".padStart(3)} ${"R".padStart(3)} ${"H".padStart(3)} ${"RBI".padStart(3)} ${"BB".padStart(3)} ${"K".padStart(3)} ${"LOB".padStart(3)} ${"AVG".padStart(4)} ${"OPS".padStart(5)}\n"
        )
        sb.append(divLine)

        val maxBatters = maxOf(box.awayBatters.size, box.homeBatters.size)
        for (i in 0 until maxBatters) {
            val a = box.awayBatters.getOrNull(i)
            val h = box.homeBatters.getOrNull(i)

            val aStr = if (a != null) {
                "${a.nameField.padEnd(40)} ${a.ab.padStart(3)} ${a.r.padStart(3)} ${a.h.padStart(3)} ${a.rbi.padStart(3)} ${a.bb.padStart(3)} ${a.k.padStart(3)} ${a.lob.padStart(3)} ${a.avg.padStart(4)} ${a.ops.padStart(5)}"
            } else {
                " ".repeat(rowLen)
            }

            val hStr = if (h != null) {
                "${h.nameField.padEnd(40)} ${h.ab.padStart(3)} ${h.r.padStart(3)} ${h.h.padStart(3)} ${h.rbi.padStart(3)} ${h.bb.padStart(3)} ${h.k.padStart(3)} ${h.lob.padStart(3)} ${h.avg.padStart(4)} ${h.ops.padStart(5)}"
            } else {
                " ".repeat(rowLen)
            }

            sb.append(aStr).append(" | ").append(hStr).append("\n")
        }

        sb.append(divLine)

        // Pitching Box
        sb.append(
            "${"Pitchers".padEnd(43)} ${"IP".padStart(4)} ${"H".padStart(3)} ${"R".padStart(3)} ${"ER".padStart(3)} ${"BB".padStart(3)} ${"K".padStart(3)} ${"HR".padStart(3)} ${"ERA".padStart(6)} | "
        )
        sb.append(
            "${"Pitchers".padEnd(43)} ${"IP".padStart(4)} ${"H".padStart(3)} ${"R".padStart(3)} ${"ER".padStart(3)} ${"BB".padStart(3)} ${"K".padStart(3)} ${"HR".padStart(3)} ${"ERA".padStart(6)}\n"
        )
        sb.append(divLine)

        val maxPitchers = maxOf(box.awayPitchers.size, box.homePitchers.size)
        for (i in 0 until maxPitchers) {
            val a = box.awayPitchers.getOrNull(i)
            val h = box.homePitchers.getOrNull(i)

            val aStr = if (a != null) {
                "${a.nameField.padEnd(43)} ${a.ip.padStart(4)} ${a.h.padStart(3)} ${a.r.padStart(3)} ${a.er.padStart(3)} ${a.bb.padStart(3)} ${a.k.padStart(3)} ${a.hr.padStart(3)} ${a.era.padStart(6)}"
            } else {
                " ".repeat(rowLen)
            }

            val hStr = if (h != null) {
                "${h.nameField.padEnd(43)} ${h.ip.padStart(4)} ${h.h.padStart(3)} ${h.r.padStart(3)} ${h.er.padStart(3)} ${h.bb.padStart(3)} ${h.k.padStart(3)} ${h.hr.padStart(3)} ${h.era.padStart(6)}"
            } else {
                " ".repeat(rowLen)
            }

            sb.append(aStr).append(" | ").append(hStr).append("\n")
        }

        sb.append(divLine)
        return sb.toString()
    }

    fun formatLinescore(line: MlbLinescoreData): String {
        val sb = StringBuilder()
        val inningCount = line.innings.size
        val teamColWidth = maxOf(line.awayTeamName.length, line.homeTeamName.length, 12) + 2

        // Header: Team | 1 2 3 4 5 6 7 8 9 | R H E
        sb.append(" ".repeat(teamColWidth))
        for (i in 1..inningCount) {
            sb.append(" ${i.toString().padStart(2)}")
        }
        sb.append(" |  R  H  E\n")

        val totalWidth = teamColWidth + (inningCount * 3) + 11
        sb.append("-".repeat(totalWidth)).append("\n")

        // Away line
        sb.append(line.awayTeamName.padEnd(teamColWidth))
        for (inn in line.innings) {
            val runs = inn.awayRuns?.toString() ?: "-"
            sb.append(" ${runs.padStart(2)}")
        }
        sb.append(" | ${line.awayRuns.toString().padStart(2)} ${line.awayHits.toString().padStart(2)} ${line.awayErrors.toString().padStart(2)}\n")

        // Home line
        sb.append(line.homeTeamName.padEnd(teamColWidth))
        for (inn in line.innings) {
            val runs = inn.homeRuns?.toString() ?: "-"
            sb.append(" ${runs.padStart(2)}")
        }
        sb.append(" | ${line.homeRuns.toString().padStart(2)} ${line.homeHits.toString().padStart(2)} ${line.homeErrors.toString().padStart(2)}\n")

        return sb.toString()
    }
}
