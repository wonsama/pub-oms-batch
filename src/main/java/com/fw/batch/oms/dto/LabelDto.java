package com.fw.batch.oms.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class LabelDto {

  // LabelList.xlsx 를 참조하여 작성

  private int id; // ID ( - ) ::: int 11 ::: not null
  private String shopCode;// SHOP CODE ( - ) ::: varchar 32 ::: not null
  private String shopName;// SHOP NAME ( - ) ::: varchar 64 ::: not null
  private String labelCode; // LABEL ID ( labelCode ) ::: varchar 32 ::: not null
  private String articleId; // PRODUCT ID ( articleList[0].articleId ) ::: varchar 32
  private String articleName; // PRODUCT DESCRIPTION ( articleList[0].articleName ) ::: varchar 256
  private String name; // LINKED GATEWAY ( gateway.name ) ::: varchar 32
  private String battery; // BATTERY ( battery ) ::: varchar 32
  private String signals; // SIGNAL STRENGTH ( signal ) ::: varchar 32 / signal 은 예약어
  private String type; // TYPE ( type ) ::: varchar 32
  private String templateType; // TEMPLATE ( templateType[0]) ::: varchar 32
  private String templateManual; // TEMPLATE MANUAL ( - / templateType[0] => empty : FALSE / exist : TRUE ) :::
                                 // varchar 32
  private String status; // NETWORK ( gateway.status : CONNECTED => ONLINE else OFFLINE ) ::: varchar 32
  private String updateStatus; // STATUS ( updateStatus ) ::: varchar 32
  private Timestamp lastResponseTime; // LATEST RESPONSE TIME ( lastResponseTime ) ::: datetime

  private String regDate; // yyyymmdd 8 ::: not null
}
