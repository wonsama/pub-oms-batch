package com.fw.batch.oms.service;

import java.util.List;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockDto;

public interface ShopStockService {
  public List<ShopStockDto> selectStocktakingList(String strId);

  public List<ShopBayTagDto> selectBayTagList(String strId);
}
