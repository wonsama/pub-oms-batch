package com.fw.batch.oms.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.LabelDto;
import com.fw.batch.oms.mappers.LabelMapper;
import com.fw.batch.oms.service.LabelSerivce;

@Service
public class LabelServiceImpl implements LabelSerivce {

  @Autowired
  private LabelMapper labelMapper;

  public int insertLabelList(List<LabelDto> list) {

    return labelMapper.insertLabelList(list);
  }

  public void deleteAllLabelList() {
    labelMapper.deleteAllLabelList();
  }

  public void deleteLabelListByMart(String shopCode) {
    labelMapper.deleteLabelListByMart(shopCode);
  }

  public List<LabelDto> selectLabelList(String shopCode) {
    return labelMapper.selectLabelList(shopCode);
  }
}
