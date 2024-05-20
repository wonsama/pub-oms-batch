package com.fw.batch.oms.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ExcelService {
  public ByteArrayOutputStream generateExcelSheet(String id) throws IOException, IllegalArgumentException;
}
