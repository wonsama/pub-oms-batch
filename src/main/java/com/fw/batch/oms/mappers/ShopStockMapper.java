package com.fw.batch.oms.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.fw.batch.oms.dto.ShopStockDto;

@Mapper
public interface ShopStockMapper {
  public List<ShopStockDto> selectStocktakingList(String strId);
}
