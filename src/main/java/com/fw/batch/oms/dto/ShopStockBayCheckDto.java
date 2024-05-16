package com.fw.batch.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopStockBayCheckDto {
  private int xpsrRdr; // 노출순서
  private int tagRdr; // 태그노출순서
  private int wrkrPreQntt; // 작업자사전체크수량
}
