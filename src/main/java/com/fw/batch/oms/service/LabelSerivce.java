package com.fw.batch.oms.service;

import java.util.List;

import com.fw.batch.oms.dto.LabelDto;

public interface LabelSerivce {
  public int insertLabelList(List<LabelDto> list);

  public void deleteAllLabelList();

  public void deleteLabelListByMart(String shopCode);

  public List<LabelDto> selectLabelList(String shopCode);
}
