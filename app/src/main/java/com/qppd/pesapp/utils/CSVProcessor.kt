package com.qppd.pesapp.utils

import android.content.Context
import android.net.Uri
import com.qppd.pesapp.models.ExcelRow
import java.io.BufferedReader
import java.io.InputStreamReader

class CSVProcessor {
    companion object {
        fun readCSVFile(context: Context, uri: Uri): List<ExcelRow> {
            val rows = mutableListOf<ExcelRow>()
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var isHeader = true
                while (reader.readLine().also { line = it } != null) {
                    if (isHeader) {
                        isHeader = false
                        continue
                    }
                    val columns = line!!.split(",")
                    if (columns.size >= 3) {
                        val parentName = columns[0].trim()
                        val parentEmail = columns[1].trim()
                        val childrenNames = columns.subList(2, columns.size).joinToString(",").trim()
                        if (parentName.isNotBlank() && parentEmail.isNotBlank()) {
                            rows.add(ExcelRow(parentName, parentEmail, childrenNames))
                        }
                    }
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("Failed to read CSV file: ${e.message}")
            }
            return rows
        }

        fun generatePassword(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..8).map { chars.random() }.joinToString("")
        }
    }
} 