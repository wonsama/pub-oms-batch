package com.fw.batch.oms.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.dto.ShopStockIf;
import com.fw.batch.oms.dto.ShopStockInfoDto;
import com.fw.batch.oms.service.ExcelService;
import com.fw.batch.oms.service.ShopStockService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

  @Autowired
  ShopStockService shopStockService;

  /**
   * 매장 IF결과 라벨 리스트 다운로드
   *
   * @param strId
   * @return
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public ByteArrayOutputStream generateLabelList(String strId) throws IOException, IllegalArgumentException {

    log.info("step 1-1 : query data from DB");

    List<ShopStockIf> ifs = shopStockService.selectIfLabelList(strId);

    log.info("step 1-2 : query result check");

    if (ifs == null || ifs.size() == 0) {
      // 데이터가 없는 경우 별도의 엑셀 시트를 생성하지 않는다. 오류 발생
      throw new IllegalArgumentException("labels is empty");
    }

    log.info("step 2 : create workbook & sheet");

    XSSFWorkbook workbook = new XSSFWorkbook();

    ExcelLabelList.createSheet(workbook, ifs);
    log.info("3/6 make sheet1 done");

    log.info("step 3 : write workbook to ByteArrayOutputStream");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();

    log.info("step 4 : workbook write done");

    return outputStream;
  }

  public ByteArrayOutputStream generateExcelSheet(String id) throws IOException, IllegalArgumentException {

    log.info("1/6 start generateExcelSheet");

    List<ShopBayTagDto> bays = shopStockService.selectBayTagList(id);
    List<ShopStockDto> labels = shopStockService.selectStocktakingList(id);
    List<ShopStockInfoDto> infos = shopStockService.selectStockInfo(id);
    List<ShopStockIf> ifs = shopStockService.selectStockIf(id);

    log.info("2/6 query result: bays={}, labels={}", bays.size(), labels.size());

    if (labels == null || labels.size() == 0) {
      // 데이터가 없는 경우 별도의 엑셀 시트를 생성하지 않는다. 오류 발생
      throw new IllegalArgumentException("labels is empty");
    }

    // Create a new workbook
    XSSFWorkbook workbook = new XSSFWorkbook();

    ExcelStockSheet1.createSheet(workbook, infos);
    log.info("3/6 make sheet1 done");

    ExcelStockSheet2.createSheet(workbook, bays, labels, infos);
    log.info("4/6 make sheet2 done");

    ExcelStockSheet3.createSheet(workbook, ifs);
    log.info("5/6 make sheet3 done");

    // Write the workbook to a ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    log.info("6/6 workbook write done");

    return outputStream;
  }
}
