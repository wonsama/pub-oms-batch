package com.fw.batch.oms.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockBayCheckDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.service.ExcelService;
import com.fw.batch.oms.service.ShopStockService;

import lombok.extern.slf4j.Slf4j;

import static com.fw.batch.oms.util.XlsUtil.drawCell;
import static com.fw.batch.oms.util.XlsUtil.getStyleBgColor;
import static com.fw.batch.oms.util.XlsUtil.getStyleTitle;
import static com.fw.batch.oms.util.XlsUtil.getStyleFont;
import static com.fw.batch.oms.util.XlsUtil.getCell;
import static com.fw.batch.oms.util.XlsUtil.getIntValue;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

  // BAY는 있으나 해당 BAY에 TAG가 없을 수도 있음

  private final int CNT_PER_TAG = 3;
  private final int CNT_PER_BAY = 4;
  private final int CNT_HIDDEN_COL = 1;

  @Autowired
  ShopStockService shopStockService;

  public ByteArrayOutputStream generateExcelSheet()
      throws IOException, IllegalArgumentException {

    log.info("1/4 start generateExcelSheet");

    List<ShopBayTagDto> bays = shopStockService.selectBayTagList("ST2404030141");
    List<ShopStockDto> labels = shopStockService.selectStocktakingList("ST2404030141");

    log.info("2/4 query result: bays={}, labels={}", bays.size(), labels.size());

    if (labels == null || labels.size() == 0) {
      // 데이터가 없는 경우 별도의 엑셀 시트를 생성하지 않는다. 오류 발생
      throw new IllegalArgumentException("labels is empty");
    }

    // Create a new workbook
    XSSFWorkbook workbook = new XSSFWorkbook();
    createScanResultSheet(workbook, bays, labels);
    log.info("3/4 createScanResultSheet done");

    // Write the workbook to a ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    log.info("4/4 workbook write done");

    return outputStream;
  }

  /**
   * 스캔 결과 시트 생성
   *
   * @param list 스캔 결과 리스트
   */
  private void createScanResultSheet(XSSFWorkbook workbook, List<ShopBayTagDto> bays, List<ShopStockDto> labels) {

    // 시트 초기화
    String strNm = bays.get(0).getStrNm();
    XSSFSheet sheet = workbook.createSheet(String.format("%s Scan 결과 값", strNm));

    // ! ROW 단위로 작업 수행
    // Create a new row within the sheet and return the high level representation
    // Note: If a row already exists at this position, it is removed/overwritten and
    // any existing cell is removed!

    // 제목
    drawTitle(sheet, workbook, bays);
    log.info("draw title done");

    // 베이명 목록
    drawBayHeader(sheet, workbook, bays);
    log.info("draw bay header done");

    // TAG 구분, Showcase Code
    drawTagCategory(sheet, workbook, bays);
    log.info("draw tag category done");

    // 사전점검수량
    drawEyeCheck(sheet, workbook, bays);
    log.info("draw eye check done");

    // 합계수량(소계)
    drawSumList(sheet, workbook, bays, labels);
    log.info("draw sum list done");

    // 오류체크
    drawErrCheck(sheet, workbook, bays, labels);
    log.info("draw err check done");

    // Tags ROW 생성하기
    drawTagBoxes(sheet, workbook, bays, labels);
    log.info("draw tag boxes done");

    // Tags ROW 값 업데이트
    updateTagBoxes(sheet, workbook, bays, labels);
    log.info("update tag boxes done");

    // Tags Sum
    drawTagSum(sheet, workbook, bays, labels);
    log.info("draw tag sum done");

    // 숨김 컬럼
    sheet.setColumnHidden(0, true);
    log.info("set column hidden done");

  }

  private void drawTagSum(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {

    // 고정값 설정
    // 고정값 설정
    final int MAX_TAG_ROWS = (int) Math.floor(getMaxLabels(labels) / 10) * 10 + 10;
    final int START_ROW = 7 + MAX_TAG_ROWS + 2;
    final int endColumn = getEndColumn(bays);

  }

  /**
   * 노출순서 중 가장 큰 값을 반환 ( 노출순서는 1부터 시작 )
   *
   * @param list
   * @return
   */
  private int getMaxXpsrRdr(List<ShopBayTagDto> bays) {
    return bays
        .stream()
        .max(Comparator.comparingInt(ShopBayTagDto::getXpsrRdr))
        .orElseThrow(NoSuchElementException::new).getXpsrRdr();
  }

  private int getEndColumn(List<ShopBayTagDto> bays) {
    final int MAX_XPSR_RDR = getMaxXpsrRdr(bays);
    return CNT_HIDDEN_COL + (MAX_XPSR_RDR * CNT_PER_TAG * CNT_PER_BAY);
  }

  private void updateTagBoxes(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {

    // 고정값 설정
    final int START_ROW = 8;
    final int MAX_XPSR_RDR = getMaxXpsrRdr(bays);

    for (int i = 0; i < MAX_XPSR_RDR; i++) {
      for (int j = 0; j < CNT_PER_BAY; j++) {
        List<ShopStockDto> filtered = getLabels(labels, i + 1, j + 1); // 노출순서, 태그노출순서 는 1부터 시작하므로 +1
        if (filtered.size() > 0) {
          for (int k = 0; k < filtered.size(); k++) {
            int startRow = START_ROW + k;
            int startCol = 2 + i * CNT_PER_TAG * CNT_PER_BAY + j * CNT_PER_TAG;
            String lblId = filtered.get(k).getLblId();
            String scanYn = filtered.get(k).getScanYn().equalsIgnoreCase("Y") ? "O" : "X";
            String mtchYn = filtered.get(k).getMtchYn().equalsIgnoreCase("Y") ? "O" : "X";

            getCell(sheet, startRow, startCol).setCellValue(lblId);
            getCell(sheet, startRow, startCol + 1).setCellValue(scanYn);
            if (scanYn.equals("X")) {
              getCell(sheet, startRow, startCol + 1).setCellStyle(getStyleBgColor(workbook, 255, 0, 0));
            }
            getCell(sheet, startRow, startCol + 2).setCellValue(mtchYn);
            if (mtchYn.equals("X")) {
              getCell(sheet, startRow, startCol + 2).setCellStyle(getStyleBgColor(workbook, 255, 0, 0));
            }
          }
        }

      }
    }

  }

  /**
   * 라벨 정보 반환
   *
   * @param labels  라벨 리스트
   * @param xpsrRdr 노출순서
   * @param tagRdr  태그노출순서
   * @return
   */
  private List<ShopStockDto> getLabels(List<ShopStockDto> labels, int xpsrRdr, int tagRdr) {

    List<ShopStockDto> filtered = labels.stream().filter(m -> m.getXpsrRdr() == xpsrRdr && m.getTagRdr() == tagRdr)
        .collect(Collectors.toList());

    return filtered;
  }

  private int getMaxLabels(List<ShopStockDto> labels) {
    Map<Integer, Map<Integer, Long>> maps = labels.stream()
        .collect(Collectors.groupingBy(
            ShopStockDto::getXpsrRdr,
            Collectors.groupingBy(ShopStockDto::getTagRdr, Collectors.counting())));

    int maxLabels = maps.values().stream()
        .map(m -> m.values().stream().mapToInt(Long::intValue).max()
            .orElse(0))
        .collect(Collectors.toList()).stream().mapToInt(Integer::intValue).max().orElse(0);

    return maxLabels;
  }

  private void drawTagBoxes(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {

    // 고정값 설정
    final int START_ROW = 8;
    final int endColumn = getEndColumn(bays);
    final int MAX_TAG_ROWS = (int) Math.floor(getMaxLabels(labels) / 10) * 10 + 10;

    for (int i = START_ROW; i < START_ROW + MAX_TAG_ROWS; i++) {
      XSSFRow row = sheet.createRow(i);
      XSSFCellStyle s1 = getStyleFont(workbook);
      XSSFCellStyle s2 = getStyleBgColor(workbook, 217, 233, 250);

      int no = i - START_ROW + 1;

      if (Math.floor((no - 1) / 10) % 2 == 0) {
        drawCell(row, 1, no, s1);
      } else {
        drawCell(row, 1, no, s2);
      }

      for (int j = 2; j < endColumn + CNT_HIDDEN_COL; j++) {
        if (Math.floor((no - 1) / 10) % 2 == 0) {
          drawCell(row, j, "", s1);
        } else {
          drawCell(row, j, "", s2);
        }

      }
    }
  }

  private void drawErrCheck(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {
    // SET ROW VALUES
    final int START_ROW = 7;
    XSSFRow row = sheet.createRow(START_ROW);
    final int MAX_XPSR_RDR = getMaxXpsrRdr(bays);

    XSSFCellStyle s1 = getStyleFont(workbook);
    XSSFCellStyle s2 = getStyleFont(workbook, true, (short) 10, true, 255, 0, 0);
    XSSFCellStyle s3 = getStyleBgColor(workbook, 255, 0, 0);

    drawCell(row, 0, "입력X", s1);
    drawCell(row, 1, "오류체크", s2);

    for (int i = 0; i < MAX_XPSR_RDR; i++) {

      for (int j = 0; j < CNT_PER_BAY; j++) { // x4 = 12
        final int startCol = 2 + i * CNT_PER_TAG * CNT_PER_BAY + j * CNT_PER_TAG;

        int eyeValue = getIntValue(getCell(sheet, START_ROW - 2, startCol));
        int sumValue = getIntValue(getCell(sheet, START_ROW - 1, startCol));

        if (eyeValue != sumValue) {
          drawCell(row, startCol, eyeValue - sumValue, s3);
        } else {
          drawCell(row, startCol, eyeValue - sumValue, s1);
        }

        List<ShopStockDto> filtered = getLabels(labels, i + 1, j + 1); // 노출순서, 태그노출순서 는 1부터 시작하므로 +1
        int scanCount = (int) filtered.stream().filter(x -> x.getScanYn().equalsIgnoreCase("N")).count();
        int mtchCount = (int) filtered.stream().filter(x -> x.getMtchYn().equalsIgnoreCase("N")).count();

        if (scanCount > 0) {
          drawCell(row, startCol + 1, "X", s3);
        } else {
          drawCell(row, startCol + 1, "Clear", s1);
        }
        if (mtchCount > 0) {
          drawCell(row, startCol + 2, "X", s3);
        } else {
          drawCell(row, startCol + 2, "Clear", s1);
        }
      }
    }
  }

  private void drawSumList(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {

    // SET ROW VALUES
    final int START_ROW = 6;
    final int MAX_XPSR_RDR = getMaxXpsrRdr(bays);

    XSSFRow row = sheet.createRow(START_ROW);

    XSSFCellStyle s1 = getStyleBgColor(workbook, 210, 251, 199); // lime
    XSSFCellStyle s2 = getStyleFont(workbook);
    XSSFCellStyle s3 = getStyleFont(workbook, false);

    drawCell(row, 0, "입력X", s3);
    drawCell(row, 1, "소계", s1);

    for (int i = 0; i < MAX_XPSR_RDR; i++) {

      for (int j = 0; j < CNT_PER_BAY; j++) { // x4 = 12
        final int startCol = 2 + i * CNT_PER_TAG * CNT_PER_BAY + j * CNT_PER_TAG;

        // 사전 점검수량 정보 반환
        // ShopStockBayCheckDto eye = getEyeCheck(checkList, i + 1, j);
        List<ShopStockDto> filtered = getLabels(labels, i + 1, j + 1); // 노출순서, 태그노출순서 는 1부터 시작하므로 +1

        // x3
        if (filtered.size() > 0) {
          drawCell(row, startCol, filtered.size(), s1);
        } else {
          drawCell(row, startCol, 0, s1);
        }

        drawCell(row, startCol + 1, "Scan", s2);
        drawCell(row, startCol + 2, "Matching", s2);

      }
    }
  }

  private void drawEyeCheck(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {
    // SET ROW VALUES
    final int START_ROW = 5;
    XSSFRow row = sheet.createRow(START_ROW);
    final int MAX_XPSR_RDR = getMaxXpsrRdr(bays);

    XSSFCellStyle s1 = getStyleBgColor(workbook, 242, 207, 237); // pink
    XSSFCellStyle s2 = getStyleFont(workbook);

    drawCell(row, 1, "사전 eye\n체크 수량", s1);

    for (int i = 0; i < MAX_XPSR_RDR; i++) {

      for (int j = 0; j < CNT_PER_BAY; j++) { // x4 = 12
        final int startCol = 2 + i * CNT_PER_TAG * CNT_PER_BAY + j * CNT_PER_TAG;

        int wrkrPreQntt = bays.get(i * CNT_PER_BAY + j).getWrkrPreQntt();
        drawCell(row, startCol, wrkrPreQntt, s1);
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

  private void drawTagCategory(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {
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
    List<String> bayNmList = bays.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());
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

  private void drawBayHeader(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {

    List<String> bayNmList = bays.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());

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

  private void drawTitle(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {

    // 고정값 설정
    final int endColumn = getEndColumn(bays);

    // SET ROW VALUES
    final int START_ROW = 0;
    XSSFRow row = sheet.createRow(START_ROW);
    XSSFCellStyle s1 = getStyleTitle(workbook);

    String strNm = bays.get(0).getStrNm();
    drawCell(row, 1, String.format("%s ESL Tag ID 조사 결과 현황", strNm), s1);

    // MERGE ROWS
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, endColumn));
  }

}
