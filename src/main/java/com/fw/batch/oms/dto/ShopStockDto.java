package com.fw.batch.oms.dto;

import java.sql.Timestamp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class ShopStockDto extends ShopBayTagDto {

  private String lblId; // 라벨아이디
  private Timestamp rgstDate; // 등록일
  private int tagRdr; // 태그노출순서
  private String scanYn; // 스캔여부
  private String mtchYn; // 매치여부
}
