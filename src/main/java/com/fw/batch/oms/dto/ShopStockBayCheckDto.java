package com.fw.batch.oms.dto;

import lombok.Data;

@Data
public class ShopStockBayCheckDto {
  private int xpsrRdr; // 노출순서
  private int tagRdr; // 태그노출순서
  private int wrkrPreQntt; // 작업자사전체크수량

  public ShopStockBayCheckDto(ShopBayTagDto m) {
    this.xpsrRdr = m.getXpsrRdr();
    this.tagRdr = m.getTagRdr();
    this.wrkrPreQntt = m.getWrkrPreQntt();
  }
}
