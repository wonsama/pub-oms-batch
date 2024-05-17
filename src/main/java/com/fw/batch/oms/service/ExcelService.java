package com.fw.batch.oms.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ExcelService {
  public ByteArrayOutputStream generateExcelSheet() throws IOException, IllegalArgumentException;
}
