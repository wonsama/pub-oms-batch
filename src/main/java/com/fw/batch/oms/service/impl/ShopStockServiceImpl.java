package com.fw.batch.oms.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.ShopBayTagDto;
import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.dto.ShopStockIf;
import com.fw.batch.oms.dto.ShopStockInfoDto;
import com.fw.batch.oms.mappers.ShopStockMapper;
import com.fw.batch.oms.service.ShopStockService;

@Service
public class ShopStockServiceImpl implements ShopStockService {

  @Autowired
  private ShopStockMapper shopStockMapper;

  public List<ShopStockDto> selectStocktakingList(String id) {
    return shopStockMapper.selectStocktakingList(id);
  }

  public List<ShopBayTagDto> selectBayTagList(String id) {
    return shopStockMapper.selectBayTagList(id);
  }

  public List<ShopStockInfoDto> selectStockInfo(String id) {
    return shopStockMapper.selectStockInfo(id);
  }

  public List<ShopStockIf> selectStockIf(String id) {
    return shopStockMapper.selectStockIf(id);
  }

}
