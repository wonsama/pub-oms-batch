package com.fw.batch.oms.service.impl;

import java.util.List;

import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fw.batch.oms.dto.ShopStockInfoDto;
import com.fw.batch.oms.util.XlsUtil.PAINT_TYPE;

import lombok.extern.slf4j.Slf4j;

import static com.fw.batch.oms.util.XlsUtil.drawCell;
import static com.fw.batch.oms.util.XlsUtil.getStyleFont;
import static com.fw.batch.oms.util.XlsUtil.getCell;
import static com.fw.batch.oms.util.XlsUtil.getColor;
import static com.fw.batch.oms.util.XlsUtil.getStyleGrayBorder;

@Slf4j
public class ExcelStockSheet1 {

  private static final int END_COL = 11;

  public static void createSheet(XSSFWorkbook workbook, List<ShopStockInfoDto> infos) {

    // 시트 초기화
    XSSFSheet sheet = workbook.createSheet("CJ올리브영 판교유스페이스"); // 임시

    // ! ROW 단위로 작업 수행
    // `sheet.createRow` 시트 내에 새 행을 만들고 상위 수준 표현을 반환합니다.
    // 참고: 이 위치에 행이 이미 존재하는 경우 해당 행은 제거/덮어쓰기되며 기존 셀이 모두 제거됩니다 !
    // Merge 는 Row만 Create 되어 있음 Cell 이 없어도 병합이 가능
    // 단 Cell 자체에 Style 이 없기 때문에 병합된 Cell에 Style을 적용하려면 병합된 Cell에 Style을 적용해야 함
    // Merge 를 하면 첫번째 Cell 의 값을 제외한 나머지 셀의 값은 제거된다

    // 1 제목 : 재고실사확인서, 작업담당자명
    drawTitle(sheet, workbook, infos);
    log.info("1 draw title done");

    // 2 재고실사현황
    drawSummerize(sheet, workbook, infos);

    // 3 재고실사결과표
    drawResultHeader(sheet, workbook, infos);

  }

  private static void drawResultHeader(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockInfoDto> infos) {

    // SET STYLE
    XSSFCellStyle s1 = getStyleFont(workbook, false);
    s1.setAlignment(HorizontalAlignment.LEFT);
    XSSFCellStyle s2 = getStyleGrayBorder(workbook, PAINT_TYPE.BOTH, 217, 150);
    XSSFCellStyle s3 = getStyleGrayBorder(workbook, PAINT_TYPE.BOTH, 242, 150);
    XSSFCellStyle s4 = getStyleGrayBorder(workbook, PAINT_TYPE.BOTH, 242, 150); // for red color
    s4.getFont().setColor(getColor(255, 0, 0));

    // SET ROWS
    final int START_ROW_1 = 7;
    XSSFRow row1 = sheet.createRow(START_ROW_1);
    drawCell(row1, 0, "■ 재고실사결과표", s1);

    final int START_ROW_2 = START_ROW_1 + 1;
    XSSFRow row2 = sheet.createRow(START_ROW_2);

    drawCell(row2, 0, "분류", s2);
    drawCell(row2, 1, "Tag 정보", s2);
    drawCell(row2, 2, "", s2);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 1, 2));
    drawCell(row2, 3, "조사 전", s2);
    drawCell(row2, 4, "", s2);
    drawCell(row2, 5, "", s2);
    drawCell(row2, 6, "", s2);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 3, 6));
    drawCell(row2, 7, "조사 결과", s2);
    drawCell(row2, 8, "", s2);
    drawCell(row2, 9, "", s2);
    drawCell(row2, 10, "", s2);
    drawCell(row2, 11, "", s2);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 7, 11));

    final int START_ROW_3 = START_ROW_1 + 2;
    XSSFRow row3 = sheet.createRow(START_ROW_3);
    drawCell(row3, 0, "", s2);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_3, 0, 0));
    drawCell(row3, 1, "Inch", s3);
    drawCell(row3, 2, "Color", s3);
    drawCell(row3, 3, "납품수량", s3);
    drawCell(row3, 4, "추가납품수량", s3);
    drawCell(row3, 5, "* 기타", s3);
    drawCell(row3, 6, "소계", s4);
    drawCell(row3, 7, "진열대(SC)", s3);
    drawCell(row3, 8, "백룸(BR)", s3);
    drawCell(row3, 9, "백룸-불량(BRC)", s3);
    drawCell(row3, 10, "소계", s4);
    drawCell(row3, 11, "오차수량", s4);

  }

  private static void drawSummerize(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockInfoDto> infos) {

    XSSFCellStyle s1 = getStyleFont(workbook, false);
    XSSFCellStyle s2 = getStyleGrayBorder(workbook, PAINT_TYPE.BOTH, 217, 198);
    XSSFCellStyle s3 = getStyleGrayBorder(workbook, PAINT_TYPE.BORDER, 198, 198);

    final int START_ROW_1 = 4;
    XSSFRow row1 = sheet.createRow(START_ROW_1);

    s1.setAlignment(HorizontalAlignment.LEFT);
    drawCell(row1, 0, "■ 재고실사현황", s1);

    final int START_ROW_2 = START_ROW_1 + 1;
    XSSFRow row2 = sheet.createRow(START_ROW_2);

    drawCell(row2, 0, "총 실사수량", s2);
    drawCell(row2, 1, "", s2);
    drawCell(row2, 2, "", s2);
    drawCell(row2, 3, 100, s3);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 0, 2));

    drawCell(row2, 4, "Barcode 스캔 에러", s2);
    drawCell(row2, 5, "", s2);
    drawCell(row2, 6, "", s2);
    drawCell(row2, 7, "", s3);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 4, 6));

    drawCell(row2, 8, "G/W 정보매칭 에러", s2);
    drawCell(row2, 9, "", s2);
    drawCell(row2, 10, "", s2);
    drawCell(row2, 11, "", s3); // sheet2 정보를 setup
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 8, 10));
  }

  private static void drawTitle(XSSFSheet sheet, XSSFWorkbook workbook, List<ShopStockInfoDto> infos) {

    final int START_ROW_1 = 0;

    XSSFRow row1 = sheet.createRow(START_ROW_1);
    XSSFCellStyle s1 = getStyleFont(workbook, false);
    s1.setFillBackgroundColor(getColor(255, 255, 255));
    XSSFFont font = s1.getFont();
    font.setBold(true);
    font.setFontHeight(28);
    font.setUnderline(FontUnderline.SINGLE);
    XSSFCellStyle s2 = getStyleFont(workbook, false);
    s2.setAlignment(HorizontalAlignment.RIGHT);
    XSSFCellStyle s3 = getStyleFont(workbook, false);
    s3.setAlignment(HorizontalAlignment.LEFT);
    XSSFCellStyle s4 = getStyleGrayBorder(workbook, PAINT_TYPE.INNER, 242, 242);

    // 재고실사확인서
    drawCell(row1, 0, "재고실사확인서");
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_1, START_ROW_1, 0, END_COL));
    getCell(sheet, START_ROW_1, 0).setCellStyle(s1);

    // 작업 담당자명
    final int START_ROW_2 = START_ROW_1 + 2;
    XSSFRow row2 = sheet.createRow(START_ROW_2);
    drawCell(row2, 8, "작업 담당자명:", s2);
    drawCell(row2, 9, "", s4);
    drawCell(row2, 10, "", s4);
    drawCell(row2, 11, "(인)", s3);
    sheet.addMergedRegion(new CellRangeAddress(START_ROW_2, START_ROW_2, 9, 10));
  }
}
