package com.fw.batch.oms.service.impl;

import java.util.List;

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
import static com.fw.batch.oms.util.XlsUtil.getColor;

@Slf4j
public class ExcelLabelList {

  private static final String[] headers = new String[] { "NO", "LABEL ID", "PRODUCT ID", "PRODUCT DESCRIPTION",
      "LINKED GATEWAY", "BATTERY", "SIGNAL STRENGTH", "TYPE", "TEMPLATE", "TEMPLATE MANUAL", "NETWORK", "STATUS",
      "LATEST RESPONSE TIME" };

  public static void createSheet(XSSFWorkbook workbook, List<ShopStockIf> ifs) {
    XSSFSheet sheet = workbook.createSheet("LabelList");
    // 1 제목
    drawHeaders(sheet, workbook, ifs);
    log.info("1/5 draw headers done");

    // 2 내용
    drawDatas(sheet, workbook, ifs);
    log.info("2/5 draw datas done");

    // 3 셀 크기 조정
    for (int i = 0; i < headers.length; i++) {
      sheet.autoSizeColumn(i);
    }
    log.info("3/5 auto size column done"); // 5초걸림 제거 필요

    // 4 틀 고정
    sheet.createFreezePane(0, 1);
    log.info("4/5 freeze pane done");

    // 5 자동필터
    sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, headers.length - 1));
    log.info("5/5 auto filter done");
  }

  private static void drawHeaders(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockIf> ifs) {
    // SET STYLE
    XSSFCellStyle s1 = getStyleBgColor(workbook, 51, 204, 204); // 라임 배경
    s1.getFont().setColor(getColor(255, 255, 255)); // 흰색 글씨
    s1.getFont().setBold(true);

    // SET ROW
    final int rowIdx = 0;
    XSSFRow row = sheet.createRow(rowIdx);
    for (int i = 0; i < headers.length; i++) {
      drawCell(row, i, headers[i], s1);
    }
  }

  private static void drawDatas(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockIf> ifs) {
    XSSFCellStyle s1 = getStyleFont(workbook); // 중앙정렬 텍스트
    s1.getFont().setBold(true);

    final int totalCnt = (int) ifs.stream().count();
    final int startRow = 1;

    for (int i = 0; i < totalCnt; i++) {
      ShopStockIf ifData = ifs.get(i);
      XSSFRow row = sheet.createRow(i + startRow);

      drawCell(row, 0, i + 1, s1);
      drawCell(row, 1, ifData.getLblId(), s1);
      drawCell(row, 2, ifData.getPrdcId(), s1);
      drawCell(row, 3, ifData.getPrdcNm(), s1);
      drawCell(row, 4, ifData.getGtwyNm(), s1);
      drawCell(row, 5, ifData.getBtryStts(), s1);
      drawCell(row, 6, ifData.getSgnlStts(), s1);
      drawCell(row, 7, ifData.getType(), s1);
      drawCell(row, 8, ifData.getTmplType(), s1);
      drawCell(row, 9, ifData.getTmplMnl(), s1);
      drawCell(row, 10, ifData.getNtwrStts(), s1);
      drawCell(row, 11, ifData.getLblStts(), s1);
      drawCell(row, 12, ifData.getLastRspnTime(), s1);
    }

  }
}
