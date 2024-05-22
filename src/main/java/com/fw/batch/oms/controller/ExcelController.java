package com.fw.batch.oms.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

  /**
   * 재고실사결과 엑셀 다운로드
   * 매장 재고실사 요약, 매대별 상세정보, 태그 IF결과 비교로 구성됨
   *
   * @param id
   * @return
   * @throws IOException
   * @throws IllegalArgumentException
   */
  @GetMapping("/download/stock/{id}")
  public ResponseEntity<byte[]> downloadStock(@PathVariable String id)
      throws IOException, IllegalArgumentException {

    // http://localhost:8080/download/stock/15c29dfa-111c-11ef-9b32-506b8dfdca46

    ByteArrayOutputStream outputStream = excelService.generateExcelSheet(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "example.xlsx");

    return ResponseEntity.ok()
        .headers(headers)
        .body(outputStream.toByteArray());
  }

  /**
   * 매장 IF결과 라벨 리스트 다운로드
   * 매일 일 배치로 DB에 저장된 데이터를 엑셀로 만들어 다운로드
   * 수량이 많은 관계로 DB에는 매장별 최신 정보만 저장되어 있음
   *
   * @param strId 매장아이디 (ex. ST2404030141)
   * @return
   * @throws IOException
   * @throws IllegalArgumentException
   */
  @GetMapping("/download/labellist/{strId}")
  public ResponseEntity<byte[]> downloadLabelList(@PathVariable String strId)
      throws IOException, IllegalArgumentException {

    // http://localhost:8080/download/labellist/ST2404030141

    ByteArrayOutputStream outputStream = excelService.generateLabelList(strId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "OYLabelList.xlsx");

    return ResponseEntity.ok()
        .headers(headers)
        .body(outputStream.toByteArray());
  }
}
