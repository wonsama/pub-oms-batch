package com.fw.batch.oms.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.dto.ShopStockIf;
import com.fw.batch.oms.dto.ShopStockInfoDto;

@Mapper
public interface ShopStockMapper {
  public List<ShopStockDto> selectStocktakingList(String id);

  public List<ShopBayTagDto> selectBayTagList(String id);

  public List<ShopStockInfoDto> selectStockInfo(String id);

  public List<ShopStockIf> selectStockIf(String id);

  public List<ShopStockIf> selectIfLabelList(String strId);
}
