package com.fw.batch.oms.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fw.batch.oms.service.ExcelService;

@RestController
public class ExcelController {

  @Autowired
  private ExcelService excelService;

  // @Autowired
  // private ShopStockService shopStockService;

  @GetMapping("/download/stock/{strId}")
  public ResponseEntity<byte[]> downloadStock(@PathVariable String strId)
      throws IOException, IllegalArgumentException {

    // List<ShopStockDto> list = shopStockService.selectStocktakingList(strId); //
    // http://localhost:8080/download/stock/ST2404030141

    // if (list.size() > 0) {
    // log.info("list last item : " + list.get(list.size() - 1));
    // }
    // log.info("list size : " + list.size());

    ByteArrayOutputStream outputStream = excelService.generateExcelSheet();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "example.xlsx");

    return ResponseEntity.ok()
        .headers(headers)
        .body(outputStream.toByteArray());
  }
}
