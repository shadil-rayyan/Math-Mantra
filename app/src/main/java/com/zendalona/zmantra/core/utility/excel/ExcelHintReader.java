package com.zendalona.zmantra.core.utility.excel;

import android.content.Context;
import android.util.Log;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

public class ExcelHintReader {

    private static final String TAG = "ExcelHintReader";

    public static String getHintFromExcel(Context context, String language, String mode) {
        String excelFilePath = "hint/" + language + ".xlsx";

        try (InputStream inputStream = context.getAssets().open(excelFilePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                Log.e(TAG, "No sheet found in Excel file.");
                return null;
            }

            for (Row row : sheet) {
                if (row == null) continue;

                Cell modeCell = row.getCell(0);
                Cell hintCell = row.getCell(1);

                if (modeCell != null && hintCell != null &&
                        mode.equalsIgnoreCase(modeCell.getStringCellValue().trim())) {
                    return hintCell.getStringCellValue().trim();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error reading Excel file: " + excelFilePath, e);
        }

        return null; // Return null for fallback handling
    }
}
