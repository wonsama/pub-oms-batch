package com.fw.batch.oms.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.dto.ShopStockInfoDto;

import lombok.extern.slf4j.Slf4j;

import static com.fw.batch.oms.util.XlsUtil.drawCell;
import static com.fw.batch.oms.util.XlsUtil.getStyleBgColor;
import static com.fw.batch.oms.util.XlsUtil.getStyleTitle;
import static com.fw.batch.oms.util.XlsUtil.getStyleFont;
import static com.fw.batch.oms.util.XlsUtil.getCell;
import static com.fw.batch.oms.util.XlsUtil.getCellStyle;
import static com.fw.batch.oms.util.XlsUtil.getIntValue;
import static com.fw.batch.oms.util.XlsUtil.setRegionStyle;

@Slf4j
public class ExcelStockSheet2 {

  // BAY는 있으나 해당 BAY에 TAG가 없을 수도 있음
  private static final int CNT_PER_TAG = 3;
  private static final int CNT_PER_BAY = 4;
  private static final int CNT_HIDDEN_COL = 1;

  public static void createSheet(XSSFWorkbook workbook, List<ShopBayTagDto> bays, List<ShopStockDto> labels,
      List<ShopStockInfoDto> infos) {

    // 시트 초기화
    String strNm = bays.get(0).getStrNm();
    XSSFSheet sheet = workbook.createSheet(String.format("%s Scan 결과 값", strNm));

    // ! ROW 단위로 작업 수행
    // `sheet.createRow` 시트 내에 새 행을 만들고 상위 수준 표현을 반환합니다.
    // 참고: 이 위치에 행이 이미 존재하는 경우 해당 행은 제거/덮어쓰기되며 기존 셀이 모두 제거됩니다 !
    // Merge 는 Row만 Create 되어 있음 Cell 이 없어도 병합이 가능
    // 단 Cell 자체에 Style 이 없기 때문에 병합된 Cell에 Style을 적용하려면 병합된 Cell에 Style을 적용해야 함

    // 1 제목
    drawTitle(sheet, workbook, bays);
    log.info("1/13 draw title done");

    // 2 베이명 목록
    drawBayHeader(sheet, workbook, bays);
    log.info("2/13 draw bay header done");

    // 3 TAG 구분, Showcase Code
    drawTagCategory(sheet, workbook, bays);
    log.info("3/13 draw tag category done");

    // 4 사전점검수량
    drawEyeCheck(sheet, workbook, bays);
    log.info("4/13 draw eye check done");

    // 5 합계수량(소계)
    drawSumList(sheet, workbook, bays, labels);
    log.info("5/13 draw sum list done");

    // 6 오류체크
    drawErrCheck(sheet, workbook, bays, labels);
    log.info("6/13 draw err check done");

    // 7 Tags ROW 생성하기
    drawTagBoxes(sheet, workbook, bays, labels);
    log.info("7/13 draw tag boxes done");

    // 8 Tags ROW 값 업데이트
    updateTagBoxes(sheet, workbook, bays, labels);
    log.info("8/13 update tag boxes done");

    // 9 Tag 구분
    drawTagCategory2(sheet, workbook, bays, labels);
    log.info("9/13 draw tag sum done");

    // 10 Tag 소계
    drawTagSum(sheet, workbook, bays, labels);
    log.info("10/13 draw tag sum done");

    // 11 Error 현황
    drawErrCount(sheet, workbook, bays, labels);
    log.info("11/13 draw err count done");

    // 12 Tag별 합계 정보
    drawTagSumList(sheet, workbook, bays, labels, infos);
    log.info("12/13 draw tag sum list done");

    // 13 숨김 컬럼
    sheet.setColumnHidden(0, true);
    log.info("13/13 set column hidden done");

  }

  private static List<ShopStockDto> getLabels(List<ShopStockDto> labels, int xpsrRdr, int tagRdr) {

    List<ShopStockDto> filtered = labels.stream().filter(m -> m.getXpsrRdr() == xpsrRdr && m.getTagRdr() == tagRdr)
        .collect(Collectors.toList());

    return filtered;
  }

  private static int getMaxLabels(List<ShopStockDto> labels) {
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

  private static List<String> getBayNmList(List<ShopBayTagDto> bays) {
    List<String> bayNmList = bays.stream().map(m -> m.getBayNm()).distinct().collect(Collectors.toList());
    return bayNmList;
  }

  private static int getMaxXpsrRdr(List<ShopBayTagDto> bays) {
    return bays
        .stream()
        .max(Comparator.comparingInt(ShopBayTagDto::getXpsrRdr))
        .orElseThrow(NoSuchElementException::new).getXpsrRdr();
  }

  private static int getEndColumn(List<ShopBayTagDto> bays) {
    final int MAX_XPSR_RDR = getMaxXpsrRdr(bays);
    return CNT_HIDDEN_COL + (MAX_XPSR_RDR * CNT_PER_TAG * CNT_PER_BAY);
  }

  private static void updateTagBoxes(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
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

  private static void drawTagSumList(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels, List<ShopStockInfoDto> infos) {
    // 고정값 설정
    final int MAX_TAG_ROWS = (int) Math.floor(getMaxLabels(labels) / 10) * 10 + 10;

    XSSFCellStyle s1 = getStyleFont(workbook, false);
    XSSFCellStyle s2 = getStyleBgColor(workbook, 242, 242, 242);
    XSSFCellStyle s3 = getStyleFont(workbook);
    XSSFCellStyle s4 = getStyleBgColor(workbook, 218, 242, 208);
    XSSFCellStyle s5 = getStyleBgColor(workbook, 251, 226, 213);

    // SET ROW VALUES
    final int START_ROW_1 = 7 + MAX_TAG_ROWS + 9;
    XSSFRow row1 = sheet.createRow(START_ROW_1);
    drawCell(row1, 1, "#. TAG별 합계 정보", s1);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_1, 1, 6));
    getCellStyle(sheet, START_ROW_1, 1).setAlignment(HorizontalAlignment.LEFT);

    // SET ROW VALUES
    final int START_ROW_2 = 7 + MAX_TAG_ROWS + 10;
    XSSFRow row2 = sheet.createRow(START_ROW_2);
    drawCell(row2, 0, "입력X", s1);
    drawCell(row2, 1, "Tag 정보");
    drawCell(row2, 3, "매장(SC)", s2);
    drawCell(row2, 4, "백룸(BR)", s2);
    drawCell(row2, 5, "백룸-불량(BRC)", s2);
    drawCell(row2, 6, "소계", s2);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 1, 2));
    setRegionStyle(sheet, START_ROW_2, START_ROW_2, 1, 2, s2);

    // 포맷 변경
    DataFormat format = workbook.createDataFormat();
    short formatIndex = format.getFormat("#,##0");
    s3.setDataFormat(formatIndex);
    s4.setDataFormat(formatIndex);
    s5.setDataFormat(formatIndex);

    final int START_ROW_3 = 7 + MAX_TAG_ROWS + 11; // 1.6 white
    drawTagSumItem(sheet, START_ROW_3, infos, 1, s1, s3);

    final int START_ROW_4 = START_ROW_3 + 1; // 1.6 black
    drawTagSumItem(sheet, START_ROW_4, infos, 2, s1, s3);

    final int START_ROW_5 = START_ROW_3 + 2; // 1.6 sum
    drawTagSumInch(sheet, START_ROW_5, infos, "TAG_16", s1, s4);

    final int START_ROW_6 = START_ROW_3 + 3; // 2.2 white
    drawTagSumItem(sheet, START_ROW_6, infos, 3, s1, s3);

    final int START_ROW_7 = START_ROW_3 + 4; // 2.2 black
    drawTagSumItem(sheet, START_ROW_7, infos, 4, s1, s3);

    final int START_ROW_8 = START_ROW_3 + 5; // 2.2 sum
    drawTagSumInch(sheet, START_ROW_8, infos, "TAG_22", s1, s4);

    final int START_ROW_9 = START_ROW_3 + 6; // total
    drawTagSumTotal(sheet, START_ROW_9, infos, s1, s5);
  }

  private static void drawTagSumTotal(XSSFSheet sheet, int startRow, List<ShopStockInfoDto> infos,
      XSSFCellStyle s1, XSSFCellStyle s5) {

    XSSFRow row = sheet.createRow(startRow);
    drawCell(row, 0, "입력X", s1);
    drawCell(row, 1, "합계", s5); // 합계
    drawCell(row, 2, "", s5); // 합계

    int c1 = infos.stream().mapToInt(x -> x.getStrQntt()).sum();
    drawCell(row, 3, c1, s5); // 진열대
    int c2 = infos.stream().mapToInt(x -> x.getBrQntt()).sum();
    drawCell(row, 4, c2, s5); // 백룸
    int c3 = infos.stream().mapToInt(x -> x.getBrDfctQntt()).sum();
    drawCell(row, 5, c3, s5); // 백룸불량
    int c4 = c1 + c2 + c3;
    drawCell(row, 6, c4, s5); // 소계

    sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 1, 2));
  }

  private static void drawTagSumInch(XSSFSheet sheet, int startRow, List<ShopStockInfoDto> infos, String inch,
      XSSFCellStyle s1, XSSFCellStyle s4) {
    List<ShopStockInfoDto> items = infos.stream().filter(x -> x.getStckInchCode().equalsIgnoreCase(inch))
        .collect(Collectors.toList());
    ShopStockInfoDto wht = items.stream().filter(x -> x.getStckClrCode().equalsIgnoreCase("WHT")).findFirst()
        .orElseThrow();
    ShopStockInfoDto blck = items.stream().filter(x -> x.getStckClrCode().equalsIgnoreCase("BLCK")).findFirst()
        .orElseThrow();

    XSSFRow row = sheet.createRow(startRow);
    drawCell(row, 0, "입력X", s1);
    drawCell(row, 1, "소계", s4); // 소계
    drawCell(row, 2, "", s4); // 소계
    drawCell(row, 3, wht.getStrQntt() + blck.getStrQntt(), s4); // 진열대
    drawCell(row, 4, wht.getBrQntt() + blck.getBrQntt(), s4); // 백룸
    drawCell(row, 5, wht.getBrDfctQntt() + blck.getBrDfctQntt(), s4); // 백룸불량
    drawCell(row, 6, wht.getStrQntt() + blck.getStrQntt() + wht.getBrQntt() + blck.getBrQntt() + wht.getBrDfctQntt()
        + blck.getBrDfctQntt(), s4); // 소계

    sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 1, 2));
  }

  private static void drawTagSumItem(XSSFSheet sheet, int startRow, List<ShopStockInfoDto> infos, int tagRdr,
      XSSFCellStyle s1,
      XSSFCellStyle s3) {
    ShopStockInfoDto info = infos.stream().filter(x -> x.getTagRdr() == tagRdr).findFirst().orElseThrow();

    XSSFRow row = sheet.createRow(startRow);
    drawCell(row, 0, "입력X", s1);
    drawCell(row, 1, info.getStckInchCodeNm(), s3); // 인치
    drawCell(row, 2, info.getStckClrCodeNm(), s3); // 색상
    drawCell(row, 3, info.getStrQntt(), s3); // 진열대
    drawCell(row, 4, info.getBrQntt(), s3); // 백룸
    drawCell(row, 5, info.getBrDfctQntt(), s3); // 백룸불량
    drawCell(row, 6, info.getStrQntt() + info.getBrQntt() + info.getBrDfctQntt(), s3); // 소계
  }

  private static void drawErrCount(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {
    // 고정값 설정
    final int MAX_TAG_ROWS = (int) Math.floor(getMaxLabels(labels) / 10) * 10 + 10;

    XSSFCellStyle s1 = getStyleFont(workbook, false);
    XSSFCellStyle s2 = getStyleBgColor(workbook, 242, 242, 242);
    XSSFCellStyle s3 = getStyleFont(workbook);

    // SET ROW VALUES
    final int START_ROW_1 = 7 + MAX_TAG_ROWS + 6;
    XSSFRow row1 = sheet.createRow(START_ROW_1);
    drawCell(row1, 1, "#. Error 현황", s1);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_1, 1, 6));
    getCellStyle(sheet, START_ROW_1, 1).setAlignment(HorizontalAlignment.LEFT);

    final int START_ROW_2 = 7 + MAX_TAG_ROWS + 7;
    XSSFRow row2 = sheet.createRow(START_ROW_2);

    drawCell(row2, 1, "Scan Error");
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 1, 2));
    setRegionStyle(sheet, START_ROW_2, START_ROW_2, 1, 2, s2);

    drawCell(row2, 4, "Matching Error");
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 4, 5));
    setRegionStyle(sheet, START_ROW_2, START_ROW_2, 4, 5, s2);

    // scan, matching 오류 count
    final int COUNT_START_ROW = 7;

    int scanSum = 0;
    int mtchSum = 0;
    List<String> bayNmList = getBayNmList(bays);
    for (int i = 0; i < bayNmList.size(); i++) {
      for (int j = 0; j < CNT_PER_BAY; j++) {
        final int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG;

        // 기본적으로 Clear 아니라면 숫자를 반환함.
        // parse 가 가능한 상태면 숫자로 반환, 아니면 Integer.MIN_VALUE 반환 후 연산 무시
        int scan = getIntValue(getCell(sheet, COUNT_START_ROW, startCol + 1), Integer.MIN_VALUE);
        if (scan != Integer.MIN_VALUE) {
          scanSum += scan;
        }
        int mtch = getIntValue(getCell(sheet, COUNT_START_ROW, startCol + 2), Integer.MIN_VALUE);
        if (mtch != Integer.MIN_VALUE) {
          mtchSum += mtch;
        }
      }
    }
    drawCell(row2, 3, scanSum, s3);
    drawCell(row2, 6, mtchSum, s3);
  }

  private static void drawTagSum(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {

    // 고정값 설정
    final int MAX_TAG_ROWS = (int) Math.floor(getMaxLabels(labels) / 10) * 10 + 10;

    XSSFCellStyle s1 = getStyleBgColor(workbook, 218, 242, 208);

    // SET ROW VALUES
    final int START_ROW = 7 + MAX_TAG_ROWS + 4;
    XSSFRow row = sheet.createRow(START_ROW);
    drawCell(row, 1, "소계", s1);

    // LOOP FOR BAY SIZE
    List<String> bayNmList = getBayNmList(bays);
    for (int i = 0; i < bayNmList.size(); i++) {
      // 3 size
      for (int j = 0; j < CNT_PER_BAY; j++) {
        final int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG;

        int sumValue = (int) getCell(sheet, 6, startCol).getNumericCellValue();
        drawCell(row, startCol, sumValue);

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW, START_ROW, startCol, startCol + 2));
        setRegionStyle(sheet, START_ROW, START_ROW, startCol, startCol + 2, s1);
      }
    }
  }

  private static void drawTagCategory2(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
      List<ShopStockDto> labels) {

    // 고정값 설정
    // 고정값 설정
    final int MAX_TAG_ROWS = (int) Math.floor(getMaxLabels(labels) / 10) * 10 + 10;

    XSSFCellStyle s2 = getStyleFont(workbook);

    // SET ROW VALUES
    final int START_ROW_1 = 7 + MAX_TAG_ROWS + 2;
    XSSFRow row1 = sheet.createRow(START_ROW_1);
    drawCell(row1, 1, "Tag 구분", s2);

    // SET ROW VALUES
    final int START_ROW_2 = 7 + MAX_TAG_ROWS + 3;
    XSSFRow row2 = sheet.createRow(START_ROW_2);
    drawCell(row2, 1, "", s2);

    // LOOP FOR BAY SIZE
    List<String> bayNmList = getBayNmList(bays);
    for (int i = 0; i < bayNmList.size(); i++) {
      // 3 size
      for (int j = 0; j < CNT_PER_BAY; j++) {
        final int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG;

        drawCell(row1, startCol, j < 2 ? "1.6\"" : "2.2\"");

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_1, startCol, startCol + 2));
        setRegionStyle(sheet, START_ROW_1, START_ROW_1, startCol, startCol + 2, s2);

        drawCell(row2, startCol, j % 2 == 0 ? "White" : "Black");

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, startCol, startCol + 2));
        setRegionStyle(sheet, START_ROW_2, START_ROW_2, startCol, startCol + 2, s2);
      }
    }
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_2, 1, 1));

  }

  private static void drawTagBoxes(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
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

  private static void drawErrCheck(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
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
          drawCell(row, startCol + 1, scanCount, s3);
        } else {
          drawCell(row, startCol + 1, "Clear", s1);
        }
        if (mtchCount > 0) {
          drawCell(row, startCol + 2, mtchCount, s3);
        } else {
          drawCell(row, startCol + 2, "Clear", s1);
        }
      }
    }
  }

  private static void drawSumList(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays,
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

  private static void drawEyeCheck(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {
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

  private static void drawTagCategory(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {
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
    List<String> bayNmList = getBayNmList(bays);
    for (int i = 0; i < bayNmList.size(); i++) {
      // 3 size
      for (int j = 0; j < CNT_PER_BAY; j++) {
        final int startCol = 2 + CNT_PER_TAG * CNT_PER_BAY * i + j * CNT_PER_TAG;

        drawCell(row1, startCol, j < 2 ? "1.6\"" : "2.2\"");

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_1, startCol, startCol + 2));
        setRegionStyle(sheet, START_ROW_1, START_ROW_1, startCol, startCol + 2, s2);

        drawCell(row2, startCol, j % 2 == 0 ? "White" : "Black");

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, startCol, startCol + 2));
        setRegionStyle(sheet, START_ROW_2, START_ROW_2, startCol, startCol + 2, s2);

        drawCell(row3, startCol, "SC");

        // MERGE ROWS
        sheet.addMergedRegion(new CellRangeAddress(START_ROW_3, START_ROW_3, startCol, startCol + 2));
        setRegionStyle(sheet, START_ROW_3, START_ROW_3, startCol, startCol + 2, s2);
      }
    }

    // MERGE COLS
    sheet.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));

  }

  private static void drawBayHeader(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {

    List<String> bayNmList = getBayNmList(bays);

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

  private static void drawTitle(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopBayTagDto> bays) {

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
