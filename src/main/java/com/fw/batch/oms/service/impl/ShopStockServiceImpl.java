package com.fw.batch.oms.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fw.batch.oms.dto.ShopStockDto;
import com.fw.batch.oms.mappers.ShopStockMapper;
import com.fw.batch.oms.service.ShopStockService;

@Service
public class ShopStockServiceImpl implements ShopStockService {

  @Autowired
  private ShopStockMapper shopStockMapper;

  public List<ShopStockDto> selectStocktakingList(String strId) {
    return shopStockMapper.selectStocktakingList(strId);
  }
}
