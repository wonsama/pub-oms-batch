package com.fw.batch.oms.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.fw.batch.oms.dto.ShopStockDto;

public interface ExcelService {
  public ByteArrayOutputStream generateExcelSheet(List<ShopStockDto> list) throws IOException, IllegalArgumentException;
}
