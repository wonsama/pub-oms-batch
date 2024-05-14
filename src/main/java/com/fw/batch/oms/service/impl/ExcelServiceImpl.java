package com.fw.batch.oms.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.service.ExcelService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

  private final String DEFAULT_FONT = "맑은 고딕";
  private final int CNT_PER_TAG = 3;
  private final int CNT_PER_BAY = 4;

  public ByteArrayOutputStream generateExcelSheet(List<ShopStockDto> list)
      throws IOException, IllegalArgumentException {

    if (list == null || list.size() == 0) {
      // 데이터가 없는 경우 별도의 엑셀 시트를 생성하지 않는다. 오류 발생
      throw new IllegalArgumentException("list is empty");
    }

    // Create a new workbook
    XSSFWorkbook workbook = new XSSFWorkbook();
    createScanResultSheet(workbook, list);

    // Write the workbook to a ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();

    return outputStream;
  }

  /**
   * 스캔 결과 시트 생성
   *
   * @param list 스캔 결과 리스트
   */
  private void createScanResultSheet(XSSFWorkbook workbook, List<ShopStockDto> list) {

    // 시트 초기화
    String strNm = list.get(0).getStrNm();
    XSSFSheet sheet = workbook.createSheet(String.format("%s Scan 결과 값", strNm));

    // 고정값 설정
    final int MAX_XPSR_RDR = list
        .stream()
        .max(Comparator.comparingInt(ShopStockDto::getXpsrRdr))
        .orElseThrow(NoSuchElementException::new).getXpsrRdr();

    // ! ROW 단위로 작업 수행
    // Create a new row within the sheet and return the high level representation
    // Note: If a row already exists at this position, it is removed/overwritten and
    // any existing cell is removed!

    // 제목
    drawTitle(sheet, workbook, strNm, MAX_XPSR_RDR);

    // 베이명 목록
    drawBayHeader(sheet, workbook, list);

    // TAG 구분
    drawTagCategory(sheet, workbook, list);

    // 숨김 컬럼
    final int HIDDEN_COL = 0;
    sheet.setColumnHidden(HIDDEN_COL, true);

    // filtered item
    // https://www.baeldung.com/java-filter-collection-by-list
    // List<ShopStockDto> filtered = list.stream().filter(shopStockDto ->
    // shopStockDto.getStrId().equals(strNm))
    // .collect(Collectors.toList());

    // get max value item

    // title merge

  }

  private void drawTagCategory(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {
    // SET ROW VALUES
    final int START_ROW_1 = 2;
    XSSFRow row1 = sheet.createRow(START_ROW_1);

    XSSFCell c1 = row1.createCell(0);
    XSSFCellStyle s1 = getStyleFont10(workbook, false);
    c1.setCellValue("입력X");
    c1.setCellStyle(s1);

    XSSFCell c2 = row1.createCell(1);
    XSSFCellStyle s2 = getStyleFont10(workbook);
    c2.setCellValue("Tag 구분");
    c2.setCellStyle(s2);

    // LOOP FOR BAY SIZE
    List<String> bayNmList = list.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());
    for (int i = 0; i < bayNmList.size(); i++) {
      // 3 size
      for (int j = 0; j < CNT_PER_BAY; j++) {
        XSSFCell c3 = row1.createCell(2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG);
        c3.setCellStyle(s2);
        c3.setCellValue(j < 2 ? "1.6\"" : "2.2\"");
        XSSFCell c4 = row1.createCell(2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG + 1);
        c4.setCellStyle(s2);
        XSSFCell c5 = row1.createCell(2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG + 2);
        c5.setCellStyle(s2);
        c5.setCellValue(3);
        // XSSFCell c6 = row1.createCell(2 + CNT_PER_TAG * CNT_PER_BAY * i + j *
        // CNT_PER_BAY + 3);
        // c6.setCellStyle(s2);
        // c6.setCellValue(3);
      }
    }
  }

  private void drawBayHeader(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {

    List<String> bayNmList = list.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());

    // SET ROW VALUES
    final int START_ROW = 1;
    XSSFRow row = sheet.createRow(START_ROW);

    XSSFCell c1 = row.createCell(1);
    XSSFCellStyle s1 = getStyleFont10(workbook);
    c1.setCellValue("매대");
    c1.setCellStyle(s1);

    log.info("bayNmList.size() : " + bayNmList.size());

    for (int i = 0; i < bayNmList.size(); i++) {
      int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i;
      log.info("startCol : " + startCol);
      XSSFCell c2 = row.createCell(startCol);
      c2.setCellValue(bayNmList.get(i));
      c2.setCellStyle(s1);

      for (int j = 0; j < CNT_PER_TAG * CNT_PER_BAY; j++) {
        XSSFCell c3 = row.createCell(startCol + j + 1);
        c3.setCellStyle(s1);
      }

      // MERGE ROWS
      sheet.addMergedRegion(new CellRangeAddress(1, 1, startCol, startCol +
          CNT_PER_TAG * CNT_PER_BAY - 1));
    }
  }

  private void drawTitle(XSSFSheet sheet, XSSFWorkbook workbook, String strNm, int MAX_XPSR_RDR) {

    final int CNT_HIDDEN_COL = 1;
    final int CNT_LABEL_COL = 1;

    int maxColumn = CNT_HIDDEN_COL + CNT_LABEL_COL + (MAX_XPSR_RDR * CNT_PER_TAG * CNT_PER_BAY);

    // SET ROW VALUES
    final int START_ROW = 0;
    XSSFRow row = sheet.createRow(START_ROW);
    XSSFCell c1 = row.createCell(1);
    c1.setCellValue(String.format("%s ESL Tag ID 조사 결과 현황", strNm));

    // SET CELL STYLE
    XSSFCellStyle s1 = getStyleTitle(workbook);
    c1.setCellStyle(s1);

    // MERGE ROWS
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, maxColumn));
  }

  private XSSFCellStyle getStyleTitle(XSSFWorkbook workbook) {
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

  private XSSFCellStyle getStyleFont10(XSSFWorkbook workbook) {
    return getStyleFont10(workbook, true);
  }

  private XSSFCellStyle getStyleFont10(XSSFWorkbook workbook, boolean hasBorder) {
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

    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

    return cellStyle;
  }

  // font.setFontName("Arial");
  // font.setBold(true);
  // font.setItalic(true);
  // font.setFontHeightInPoints((short) 14);

  // private XSSFCellStyle createCellStyle(XSSFWorkbook workbook) {

  // // CREATE CELL STYLE
  // XSSFCellStyle cellStyle = workbook.createCellStyle();

  // // SET FONT
  // XSSFFont font = workbook.createFont();
  // font.setFontHeightInPoints((short) 20);

  // // SET FONT COLOR
  // font.setColor(getColor(255, 0, 0));
  // cellStyle.setFont(font);

  // // Set border
  // // cellStyle.setBorderBottom(BorderStyle.THICK);
  // // cellStyle.setBorderTop(BorderStyle.THICK);
  // // cellStyle.setBorderLeft(BorderStyle.THICK);
  // // cellStyle.setBorderRight(BorderStyle.THICK);

  // // Set background color
  // // cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
  // // cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

  // // Set horizontal alignment
  // cellStyle.setAlignment(HorizontalAlignment.LEFT);

  // return cellStyle;
  // }

  private XSSFColor getColor(int red, int green, int blue) {
    byte[] rgb = new byte[] { (byte) red, (byte) green, (byte) blue };
    return new XSSFColor(rgb, null);
  }

}
