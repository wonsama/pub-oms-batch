package com.fw.batch.oms.service;

import java.util.List;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.dto.ShopStockInfoDto;

public interface ShopStockService {
  public List<ShopStockDto> selectStocktakingList(String id);

  public List<ShopBayTagDto> selectBayTagList(String id);

  public List<ShopStockInfoDto> selectStockInfo(String id);
}
