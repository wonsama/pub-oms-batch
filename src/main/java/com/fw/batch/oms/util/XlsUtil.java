package com.fw.batch.oms.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

public class XlsUtil {

  public static final String DEFAULT_FONT = "맑은 고딕";

  public static int getIntValue(XSSFCell cell) {
    return getIntValue(cell, 0);
  }

  public static int getIntValue(XSSFCell cell, int defaultValue) {
    try {
      int v = (int) cell.getNumericCellValue();
      return v;
    } catch (Exception e) {
      return defaultValue;
    }

  }

  public static XSSFCell drawCell(XSSFRow row, int col, int value) {
    XSSFCell cell = row.createCell(col);
    cell.setCellValue(value);

    return cell;
  }

  public static XSSFCell drawCell(XSSFRow row, int col, String value) {
    XSSFCell cell = row.createCell(col);
    cell.setCellValue(value);

    return cell;
  }

  public static XSSFCell drawCell(XSSFRow row, int col, int value, XSSFCellStyle style) {
    XSSFCell cell = row.createCell(col);
    cell.setCellValue(value);
    cell.setCellStyle(style);

    return cell;
  }

  public static XSSFCell drawCell(XSSFRow row, int col, String value, XSSFCellStyle style) {
    XSSFCell cell = row.createCell(col);
    cell.setCellValue(value);
    cell.setCellStyle(style);

    return cell;
  }

  public static XSSFColor getColor(int red, int green, int blue) {
    byte[] rgb = new byte[] { (byte) red, (byte) green, (byte) blue };
    return new XSSFColor(rgb, null);
  }

  public static XSSFCellStyle getStyleTitle(XSSFWorkbook workbook) {
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    XSSFFont font = workbook.createFont();
    font.setFontHeightInPoints((short) 20);
    font.setFontName(DEFAULT_FONT);
    font.setBold(true);
    cellStyle.setFont(font);

    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);

    cellStyle.setAlignment(HorizontalAlignment.LEFT);

    return cellStyle;
  }

  public static void updateFontColor(XSSFWorkbook workbook, XSSFCellStyle style, int red, int green, int blue,
      boolean hasBold) {
    XSSFFont font = style.getFont();
    font.setColor(getColor(red, green, blue));
    style.setFont(font);
    font.setBold(hasBold);
  }

  public static XSSFCellStyle getStyleBgColor(XSSFWorkbook workbook, int red, int green, int blue) {
    return getStyleBgColor(workbook, red, green, blue, true);
  }

  public static XSSFCellStyle getStyleBgColor(XSSFWorkbook workbook, int red, int green, int blue, boolean hasBorder) {
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    XSSFFont font = workbook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName(DEFAULT_FONT);
    cellStyle.setFont(font);

    if (hasBorder) {
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }

    // Set background color
    cellStyle.setFillForegroundColor(getColor(red, green, blue));
    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

    return cellStyle;
  }

  public static enum PAINT_TYPE {
    INNER, BORDER, BOTH
  }

  public static XSSFCellStyle getStyleGrayBorder(XSSFWorkbook workbook, PAINT_TYPE type, int innerGray,
      int borderGray) {
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    XSSFFont font = workbook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName(DEFAULT_FONT);
    cellStyle.setFont(font);

    final XSSFColor cgray = getColor(innerGray, innerGray, innerGray); // GRAY COLOR (217, 242)
    final XSSFColor cgrayMore = getColor(borderGray, borderGray, borderGray); // GRAY
    // MORE COLOR ( 198 )

    if (type == PAINT_TYPE.BORDER || type == PAINT_TYPE.BOTH) {
      cellStyle.setBorderColor(BorderSide.BOTTOM, cgrayMore);
      cellStyle.setBorderColor(BorderSide.LEFT, cgrayMore);
      cellStyle.setBorderColor(BorderSide.RIGHT, cgrayMore);
      cellStyle.setBorderColor(BorderSide.TOP, cgrayMore);
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }

    // Set background color
    if (type == PAINT_TYPE.INNER || type == PAINT_TYPE.BOTH) {
      cellStyle.setFillForegroundColor(cgray);
      cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

    return cellStyle;
  }

  public static XSSFCellStyle getStyleFont(XSSFWorkbook workbook) {
    return getStyleFont(workbook, true);
  }

  public static XSSFCellStyle getStyleFont(XSSFWorkbook workbook, boolean hasBorder) {
    return getStyleFont(workbook, hasBorder, (short) 10, false, 0, 0, 0);
  }

  public static XSSFCellStyle getStyleFont(XSSFWorkbook workbook, boolean hasBorder, short fontsize, boolean hasBold,
      int red, int green, int blue) {
    XSSFCellStyle cellStyle = workbook.createCellStyle();
    XSSFFont font = workbook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setFontName(DEFAULT_FONT);
    font.setBold(hasBold);
    font.setColor(getColor(red, green, blue));
    cellStyle.setFont(font);

    if (hasBorder) {
      cellStyle.setBorderBottom(BorderStyle.THIN);
      cellStyle.setBorderTop(BorderStyle.THIN);
      cellStyle.setBorderLeft(BorderStyle.THIN);
      cellStyle.setBorderRight(BorderStyle.THIN);
    }

    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

    return cellStyle;
  }

  public static XSSFCell getCell(XSSFSheet sheet, int rowNum, int colNum) {
    XSSFRow row = sheet.getRow(rowNum);
    if (row == null) {
      throw new RuntimeException("Row is null");
      // row = sheet.createRow(rowNum);
    }
    XSSFCell cell = row.getCell(colNum);
    if (cell == null) {
      cell = row.createCell(colNum);
    }
    return cell;
  }

  public static XSSFCellStyle getCellStyle(XSSFSheet sheet, int rowNum, int colNum) {
    XSSFCell cell = getCell(sheet, rowNum, colNum);
    return cell.getCellStyle();
  }

  public static void setRegionStyle(XSSFSheet sheet, int startRow, int endRow, int startCol,
      int endCol, XSSFCellStyle style) {
    for (int i = startRow; i <= endRow; i++) {
      XSSFRow row = sheet.getRow(i);
      if (row == null) {
        row = sheet.createRow(i);
      }
      for (int j = startCol; j <= endCol; j++) {
        XSSFCell cell = row.getCell(j);
        if (cell == null) {
          cell = row.createCell(j);
        }
        cell.setCellStyle(style);
      }
    }
  }

}
