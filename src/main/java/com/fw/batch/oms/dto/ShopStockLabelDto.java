package com.fw.batch.oms.dto;

import lombok.Data;

@Data
public class ShopStockLabelDto {
  private int xpsrRdr; // 노출순서
  private int tagRdr; // 태그노출순서
  private String lblId; // 라벨아이디
  private String scanYn; // 스캔여부
  private String mtchYn; // 매치여부

  public ShopStockLabelDto(ShopStockDto m) {
    this.xpsrRdr = m.getXpsrRdr();
    this.tagRdr = m.getTagRdr();
    this.lblId = m.getLblId();
    this.scanYn = m.getScanYn();
    this.mtchYn = m.getMtchYn();
  }
}
