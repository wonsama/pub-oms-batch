package com.fw.batch.oms.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.fw.batch.oms.dto.LabelDto;

@Mapper
public interface LabelMapper {
  public int insertLabelList(List<LabelDto> list);

  public void deleteAllLabelList();

  public void deleteLabelListByMart(String shopCode);

  public List<LabelDto> selectLabelList(String shopCode);
}
