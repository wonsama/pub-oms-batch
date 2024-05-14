package com.fw.batch.oms.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ShopStockDto {
  private String strId; // 매장아이디
  private String strNm; // 매장명
  private String workSttsCode; // 작업상태코드
  private String workSttsCodeNm; // 작업상태
  private int gtwyQntt; // 게이트웨이수량
  private String workCmplDate; // 작업완료일
  private String bayTypeCode; // 매대유형코드
  private String bayTypeCodeNm; // 매대유형
  private String bayNm; // 매대명
  private int xpsrRdr; // 노출순서
  private String stckInchCode; // 재고인치코드
  private String stckInchCodeNm; // 재고인치
  private String stckClrCode; // 재고색상코드
  private String stckClrCodeNm; // 재고색상
  private int wrkrPreQntt; // 작업자사전체크수량
  private String lblId; // 라벨아이디
  private Timestamp rgstDate; // 등록일
  private String tagRdr; // 태그노출순서
}
