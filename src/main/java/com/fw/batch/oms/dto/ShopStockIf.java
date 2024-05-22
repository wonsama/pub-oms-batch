package com.fw.batch.oms.dto;

import lombok.Data;

@Data
public class ShopStockIf {
  private String strId; // 매장 아이디
  private String stckId; // 재고조사 아이디
  private String lblId; // 라벨아이디
  private String mtchYn; // 매치여부
  private String prdcId; // 제품아이디
  private String prdcNm; // 제품명
  private String gtwyNm; // 게이트웨이 이름
  private String btryStts; // 배터리상태
  private String sgnlStts; // 신호상태
  private String type; // 유형
  private String tmplType; // 템플릿 타입
  private String tmplMnl; // 템플릿 매뉴얼
  private String ntwrStts; // 네트워크상태
  private String lblStts; // 라벨상태
  private String lastRspnTime; // 마지막 응답 시간 - 20-:00.000+0900 또는 null 있을 수 있음 그래서 Timestamp 대신 String으로 변경
}
