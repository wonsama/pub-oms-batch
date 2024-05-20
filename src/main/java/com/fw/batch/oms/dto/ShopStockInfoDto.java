package com.fw.batch.oms.dto;

import lombok.Data;

@Data
public class ShopStockInfoDto {
  private String id; // 아이디
  private String stckId; // 재고조사 아이디
  private int preDlvrQntt; // 조사전 납품수량
  private int preDtnlDlvrQntt; // 조사전 추가납품수량
  private int preEtcQntt; // 조사전 기타수량
  private int strQntt; // 진열대 수량
  private int brQntt; // 백룸 수량
  private int brDfctQntt; // 백룸불량 수량
  private String stckInchCode; // 재고인치코드
  private String stckClrCode; // 재고색상코드
  private String stckInchCodeNm; // 재고인치
  private String stckClrCodeNm; // 재고색상
  private int tagRdr; // 태그노출순서
}
