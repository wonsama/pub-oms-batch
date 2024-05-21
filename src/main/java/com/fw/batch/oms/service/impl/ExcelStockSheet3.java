package com.fw.batch.oms.service.impl;

import java.util.List;

import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fw.batch.oms.dto.ShopStockIf;

import lombok.extern.slf4j.Slf4j;

import static com.fw.batch.oms.util.XlsUtil.drawCell;
import static com.fw.batch.oms.util.XlsUtil.getStyleBgColor;
import static com.fw.batch.oms.util.XlsUtil.getStyleFont;
import static com.fw.batch.oms.util.XlsUtil.getCell;
import static com.fw.batch.oms.util.XlsUtil.updateFontColor;

@Slf4j
public class ExcelStockSheet3 {

  public static void createSheet(XSSFWorkbook workbook, List<ShopStockIf> ifs) {

    // 시트 초기화
    XSSFSheet sheet = workbook.createSheet("LabelList");

    // ! ROW 단위로 작업 수행
    // `sheet.createRow` 시트 내에 새 행을 만들고 상위 수준 표현을 반환합니다.
    // 참고: 이 위치에 행이 이미 존재하는 경우 해당 행은 제거/덮어쓰기되며 기존 셀이 모두 제거됩니다 !
    // Merge 는 Row만 Create 되어 있음 Cell 이 없어도 병합이 가능
    // 단 Cell 자체에 Style 이 없기 때문에 병합된 Cell에 Style을 적용하려면 병합된 Cell에 Style을 적용해야 함
    // Merge 를 하면 첫번째 Cell 의 값을 제외한 나머지 셀의 값은 제거된다

    // 1 합계
    drawSum(sheet, workbook, ifs);
    log.info("1 draw title done");

    // 2 헤더
    drawHeaders(sheet, workbook, ifs);
    log.info("2 draw headers done");

    // 3 데이터
    drawDatas(sheet, workbook, ifs);
    log.info("3 draw datas done");
  }

  private static void drawDatas(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockIf> ifs) {
    XSSFCellStyle s1 = getStyleFont(workbook); // 중앙정렬 텍스트
    XSSFCellStyle s2 = getStyleBgColor(workbook, 255, 0, 0); // 빨강

    final int totalCnt = (int) ifs.stream().count();
    final int startRow = 5;

    for (int i = 0; i < totalCnt; i++) {
      ShopStockIf ifData = ifs.get(i);
      XSSFRow row = sheet.createRow(i + startRow);

      drawCell(row, 0, i + 1, s1);
      drawCell(row, 1, ifData.getLblId(), s1);
      if (ifData.getMtchYn() != null && ifData.getMtchYn().equalsIgnoreCase("Y")) {
        drawCell(row, 2, "O", s1);
      } else {
        drawCell(row, 2, "X", s2);
      }
      drawCell(row, 3, ifData.getPrdcId(), s1);
      drawCell(row, 4, ifData.getPrdcNm(), s1);
      drawCell(row, 5, ifData.getGtwyNm(), s1);
      drawCell(row, 6, ifData.getBtryStts(), s1);
      drawCell(row, 7, ifData.getSgnlStts(), s1);
      drawCell(row, 8, ifData.getType(), s1);
      drawCell(row, 9, ifData.getTmplType(), s1);
      drawCell(row, 10, ifData.getTmplMnl(), s1);
      drawCell(row, 11, ifData.getNtwrStts(), s1);
      drawCell(row, 12, ifData.getLblStts(), s1);
      drawCell(row, 13, ifData.getLastRspnTime(), s1);
    }

  }

  private static void drawHeaders(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockIf> ifs) {
    final String[] headers = new String[] { "NO", "LABEL ID", "Mathing Check", "PRODUCT ID", "PRODUCT DESCRIPTION",
        "LINKED GATEWAY", "BATTERY", "SIGNAL STRENGTH", "TYPE", "TEMPLATE", "TEMPLATE MANUAL", "NETWORK", "STATUS",
        "LATEST RESPONSE TIME" };

    // SET STYLE
    XSSFCellStyle s1 = getStyleBgColor(workbook, 51, 204, 204); // 라임
    updateFontColor(workbook, s1, 255, 255, 255, true); // 흰색

    // SET ROW
    final int rowIdx = 4;
    XSSFRow row = sheet.createRow(rowIdx);
    for (int i = 0; i < headers.length; i++) {
      drawCell(row, i, headers[i], s1);
    }
  }

  private static void drawSum(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockIf> ifs) {
    // SET STYLE
    XSSFCellStyle s1 = getStyleBgColor(workbook, 232, 232, 232); // 회색
    XSSFCellStyle s2 = getStyleBgColor(workbook, 71, 211, 89); // 녹색
    XSSFCellStyle s3 = getStyleBgColor(workbook, 255, 0, 0); // 빨강
    XSSFCellStyle s4 = getStyleFont(workbook); // 중앙정렬 텍스트

    DataFormat format = workbook.createDataFormat();
    short formatIndex = format.getFormat("#,##0");
    s4.setDataFormat(formatIndex);
    s4.setAlignment(HorizontalAlignment.RIGHT);

    // DRAW ROWS
    int totalCnt = (int) ifs.stream().count();
    _drawSum(sheet, ifs, 0, "Total Label Cnt.", totalCnt, s4, s1);

    int matchCnt = (int) ifs.stream().filter(x -> x.getMtchYn().equalsIgnoreCase("Y")).count();
    _drawSum(sheet, ifs, 1, "Matching Label Cnt.", matchCnt, s4, s2);

    int notMatchCnt = (int) ifs.stream().filter(x -> x.getMtchYn().equalsIgnoreCase("N")).count();
    _drawSum(sheet, ifs, 2, "Total Label Cnt.", notMatchCnt, s4, s3);
  }

  private static void _drawSum(XSSFSheet sheet, List<ShopStockIf> ifs, int rowIdx, String title, int count,
      XSSFCellStyle s1, XSSFCellStyle s2) {
    // DRAW ROWS 3
    XSSFRow row3 = sheet.createRow(rowIdx);
    drawCell(row3, 0, title, s2);
    drawCell(row3, 1, "", s2);
    drawCell(row3, 2, count, s1);

    sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
    getCell(sheet, rowIdx, 0).getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
  }
}
