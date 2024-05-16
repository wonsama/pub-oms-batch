package com.fw.batch.oms.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.ShopStockBayCheckDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.service.ExcelService;

import lombok.extern.slf4j.Slf4j;

import static com.fw.batch.oms.util.XlsUtil.drawCell;
import static com.fw.batch.oms.util.XlsUtil.getStyleBgColor;
import static com.fw.batch.oms.util.XlsUtil.getStyleTitle;
import static com.fw.batch.oms.util.XlsUtil.getStyleFont;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

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

    // ! ROW 단위로 작업 수행
    // Create a new row within the sheet and return the high level representation
    // Note: If a row already exists at this position, it is removed/overwritten and
    // any existing cell is removed!

    // 제목
    drawTitle(sheet, workbook, list);

    // 베이명 목록
    drawBayHeader(sheet, workbook, list);

    // TAG 구분, Showcase Code
    drawTagCategory(sheet, workbook, list);

    // 사전점검수량
    drawEyeCheck(sheet, workbook, list);

    // 합계수량(소계)
    drawSumList(sheet, workbook, list);

    // 오류체크
    drawErrCheck(sheet, workbook, list);

    // 숨김 컬럼
    final int HIDDEN_COL = 0;
    sheet.setColumnHidden(HIDDEN_COL, true);

  }

  private void drawErrCheck(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {
    // SET ROW VALUES
    final int START_ROW = 7;
    XSSFRow row = sheet.createRow(START_ROW);

    // getStyleFont(XSSFWorkbook workbook, boolean hasBorder, short fontsize,
    // boolean hadBold,
    // int red, int green, int blue) {

    XSSFCellStyle s1 = getStyleFont(workbook, false);
    XSSFCellStyle s2 = getStyleFont(workbook, true, (short) 10, true, 255, 0, 0);

    drawCell(row, 0, "입력X", s1);
    drawCell(row, 1, "오류체크", s2);

  }

  private void drawSumList(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {

    // SET ROW VALUES
    final int START_ROW = 6;
    XSSFRow row = sheet.createRow(START_ROW);

    XSSFCellStyle s1 = getStyleBgColor(workbook, 210, 251, 199); // lime
    XSSFCellStyle s2 = getStyleFont(workbook);
    XSSFCellStyle s3 = getStyleFont(workbook, false);

    drawCell(row, 1, "소계", s1);
    drawCell(row, 0, "입력X", s3);

    List<ShopStockBayCheckDto> checkList = list.stream()
        .map(m -> new ShopStockBayCheckDto(m.getXpsrRdr(), m.getTagRdr(),
            m.getWrkrPreQntt())) // 정렬순서, 태그노출순서,
        // 작업자사전체크수량
        .distinct().collect(Collectors.toList());

    List<Integer> totalList = list.stream()
        .map(m -> m.getXpsrRdr()) // 정렬순서
        .distinct().collect(Collectors.toList());

    for (int i = 0; i < totalList.size(); i++) {

      int xpsrRdr = totalList.get(i);
      for (int j = 0; j < CNT_PER_BAY; j++) { // x4 = 12
        // final int startCol = 2 + CNT_PER_TAG * i + j * CNT_PER_BAY * CNT_PER_TAG;
        final int startCol = 2 + i * CNT_PER_TAG * CNT_PER_BAY + j * CNT_PER_TAG;

        // 사전 점검수량 정보 반환
        ShopStockBayCheckDto eye = getEyeCheck(checkList, xpsrRdr, j);

        // x3
        if (eye == null) {
          drawCell(row, startCol, "-", s1);
        } else {
          drawCell(row, startCol, eye.getWrkrPreQntt(), s1);
        }

        drawCell(row, startCol + 1, "Scan", s2);
        drawCell(row, startCol + 2, "Matching", s2);

      }
    }
  }

  private void drawEyeCheck(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {
    // SET ROW VALUES
    final int START_ROW = 5;
    XSSFRow row = sheet.createRow(START_ROW);

    XSSFCellStyle s1 = getStyleBgColor(workbook, 242, 207, 237); // pink
    XSSFCellStyle s2 = getStyleFont(workbook);

    drawCell(row, 1, "사전 eye\n체크 수량", s1);

    List<ShopStockBayCheckDto> checkList = list.stream()
        .map(m -> new ShopStockBayCheckDto(m.getXpsrRdr(), m.getTagRdr(),
            m.getWrkrPreQntt())) // 정렬순서, 태그노출순서,
        // 작업자사전체크수량
        .distinct().collect(Collectors.toList());

    List<Integer> totalList = list.stream()
        .map(m -> m.getXpsrRdr()) // 정렬순서
        .distinct().collect(Collectors.toList());

    for (int i = 0; i < totalList.size(); i++) {

      int xpsrRdr = totalList.get(i);
      for (int j = 0; j < CNT_PER_BAY; j++) { // x4 = 12
        // final int startCol = 2 + CNT_PER_TAG * i + j * CNT_PER_BAY * CNT_PER_TAG;
        final int startCol = 2 + i * CNT_PER_TAG * CNT_PER_BAY + j * CNT_PER_TAG;

        // 사전 점검수량 정보 반환
        ShopStockBayCheckDto eye = getEyeCheck(checkList, xpsrRdr, j);

        // x3
        if (eye == null) {
          drawCell(row, startCol, "-", s1);
        } else {
          drawCell(row, startCol, eye.getWrkrPreQntt(), s1);
        }
        drawCell(row, startCol + 1, "검증", s2);
        drawCell(row, startCol + 2, "", s2);

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW, START_ROW, startCol + 1, startCol + 2));
      }

    }

  }

  private ShopStockBayCheckDto getEyeCheck(List<ShopStockBayCheckDto> checkList, int xpsrRdr, int idx) {
    List<ShopStockBayCheckDto> item = checkList.stream()
        .filter(dto -> (dto.getXpsrRdr() == xpsrRdr && dto.getTagRdr() == idx + 1))
        .collect(Collectors.toList());
    if (item.isEmpty()) {
      return null;
    } else {
      return item.get(0);
    }
  }

  private void drawTagCategory(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {
    // SET ROW VALUES
    final int START_ROW_1 = 2;
    XSSFRow row1 = sheet.createRow(START_ROW_1);

    XSSFCellStyle s1 = getStyleFont(workbook, false);
    XSSFCellStyle s2 = getStyleFont(workbook);

    drawCell(row1, 0, "입력X", s1);
    drawCell(row1, 1, "Tag 구분", s2);

    // SET ROW VALUES
    final int START_ROW_2 = 3;
    XSSFRow row2 = sheet.createRow(START_ROW_2);

    drawCell(row2, 0, "입력X", s1);
    drawCell(row2, 1, "", s2);

    // SET ROW VALUES
    final int START_ROW_3 = 4;
    XSSFRow row3 = sheet.createRow(START_ROW_3);
    drawCell(row3, 0, "입력X", s1);
    drawCell(row3, 1, "Showcase Code", s2);

    // LOOP FOR BAY SIZE
    List<String> bayNmList = list.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());
    for (int i = 0; i < bayNmList.size(); i++) {
      // 3 size
      for (int j = 0; j < CNT_PER_BAY; j++) {
        final int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG;

        drawCell(row1, startCol, j < 2 ? "1.6\"" : "2.2\"", s2);
        drawCell(row1, startCol + 1, "", s2);
        drawCell(row1, startCol + 2, "", s2);

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_1, startCol, startCol + 2));

        drawCell(row2, startCol, j % 2 == 0 ? "White" : "Black", s2);
        drawCell(row2, startCol + 1, "", s2);
        drawCell(row2, startCol + 2, "", s2);

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, startCol, startCol + 2));

        drawCell(row3, startCol, "SC", s2);
        drawCell(row3, startCol + 1, "", s2);
        drawCell(row3, startCol + 2, "", s2);

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_3, START_ROW_3, startCol, startCol + 2));
      }
    }

    // MERGE COLS
    sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

  }

  private void drawBayHeader(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {

    List<String> bayNmList = list.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());

    // SET ROW VALUES
    final int START_ROW = 1;
    XSSFRow row = sheet.createRow(START_ROW);
    XSSFCellStyle s1 = getStyleFont(workbook);

    drawCell(row, 1, "매대", s1);

    for (int i = 0; i < bayNmList.size(); i++) {
      int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i;

      final String bayNm = bayNmList.get(i);
      drawCell(row, startCol, bayNm, s1);

      for (int j = 0; j < CNT_PER_TAG * CNT_PER_BAY - 1; j++) {
        drawCell(row, startCol + j + 1, "", s1);
      }

      // MERGE ROWS
      sheet.addMergedRegion(new CellRangeAddress(1, 1, startCol, startCol +
          CNT_PER_TAG * CNT_PER_BAY - 1));
    }
  }

  private void drawTitle(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockDto> list) {

    // 고정값 설정
    final int MAX_XPSR_RDR = list
        .stream()
        .max(Comparator.comparingInt(ShopStockDto::getXpsrRdr))
        .orElseThrow(NoSuchElementException::new).getXpsrRdr();
    final int CNT_HIDDEN_COL = 1;
    final int maxColumn = CNT_HIDDEN_COL + ((MAX_XPSR_RDR - 1) * CNT_PER_TAG * CNT_PER_BAY);

    // SET ROW VALUES
    final int START_ROW = 0;
    XSSFRow row = sheet.createRow(START_ROW);
    XSSFCellStyle s1 = getStyleTitle(workbook);

    String strNm = list.get(0).getStrNm();
    drawCell(row, 1, String.format("%s ESL Tag ID 조사 결과 현황", strNm), s1);

    // MERGE ROWS
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, maxColumn));
  }

}
