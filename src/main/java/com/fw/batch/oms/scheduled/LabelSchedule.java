package com.fw.batch.oms.scheduled;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.fw.batch.oms.dto.LabelDto;
import com.fw.batch.oms.service.LabelSerivce;
import com.fw.batch.oms.util.HttpHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LabelSchedule implements SchedulingConfigurer {

  /*
   * Cron 표현식
   * <초:0-59> <분:0-59> <시:0-23> <일:1-31> <월:1-12:JAN-DEC> <요일:0-6:SUN-SAT>
   * <년:1970-2099>
   */

  @Value("${api.url.get_label_list}")
  private String API_URL_GET_LABEL_LIST;

  @Value("${api.cron.get_label_list}")
  private String API_CRON_GET_LABEL_LIST;

  @Autowired
  private LabelSerivce labelService;

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

    taskRegistrar.addTriggerTask(new Runnable() {
      @Override
      public void run() {
        log.info("triggered get label list");
        String res = HttpHelper.requestGet(API_URL_GET_LABEL_LIST);
        List<LabelDto> list = parseLabelInfo(res);

        labelService.deleteAllLabelList();
        // labelService.deleteLabelListByMart("FFFF");
        labelService.insertLabelList(list);

        List<LabelDto> results = labelService.selectLabelList("FFFF");
        if (results.size() > 0) {
          log.info("results[0] : {}", results.get(0));
        } else {
          log.info("results[0] : {}", "empty");
        }

      }
    }, new Trigger() {
      @Override
      public Instant nextExecution(TriggerContext triggerContext) {
        CronTrigger trigger = new CronTrigger(API_CRON_GET_LABEL_LIST);
        Instant nextExec = trigger.nextExecution(triggerContext);
        return nextExec;
      }
    });
  }

  private List<LabelDto> parseLabelInfo(String res) {
    return parseLabelInfo(res, "FFFF", "TEST SHOP");
  }

  private List<LabelDto> parseLabelInfo(String res, String shopCode, String shopName) {
    JsonElement json = JsonParser.parseString(res);
    JsonElement labelList = json.getAsJsonObject().get("labelList");
    JsonArray labelArray = labelList.getAsJsonArray();

    List<LabelDto> list = new ArrayList<LabelDto>();

    for (int i = 0; i < labelArray.size(); i++) {
      JsonObject label = labelArray.get(i).getAsJsonObject();

      LabelDto dto = new LabelDto();

      dto.setBattery(label.get("battery").toString());
      dto.setLabelCode(label.get("labelCode").toString()); // labelCode

      dto.setShopCode(shopCode);
      dto.setShopName(shopName);
      dto.setSignals(label.get("signal").toString()); // signal 은 예약어여서 signals 로 변경

      dto.setType(label.get("type").toString());
      dto.setUpdateStatus(label.get("updateStatus").toString());

      // timestamp
      // Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]
      String lastResponseTime = label.get("lastResponseTime").toString();
      if (lastResponseTime != null && lastResponseTime.length() > 0) {
        lastResponseTime = lastResponseTime.split("\\+")[0].replace("T", " ").replace("\"", "");
        try {
          final Timestamp _lastResponseTime = Timestamp.valueOf(lastResponseTime);
          dto.setLastResponseTime(_lastResponseTime);
        } catch (Exception e) {
          log.error("lastResponseTime parse error : {}", lastResponseTime); // 시간 부분 값이 - 로 되어 있는 경우 존재
        }

      } else {
        dto.setLastResponseTime(null);
      }

      // articleList
      JsonArray articleList = label.getAsJsonArray("articleList");
      if (articleList.size() > 0) {
        JsonObject article = articleList.get(0).getAsJsonObject();
        dto.setArticleId(article.get("articleId").toString());
        dto.setArticleName(article.get("articleName").toString());
      } else {
        dto.setArticleId("");
        dto.setArticleName("");
      }

      // gateway
      JsonObject gateway = label.getAsJsonObject("gateway");
      dto.setName(gateway.get("name").toString());
      String status = gateway.get("status").toString();
      if (status.equalsIgnoreCase("CONNECTED")) {
        dto.setStatus("ONLINE");
      } else {
        dto.setStatus("OFFLINE");
      }

      // templateType
      JsonArray templateType = label.getAsJsonArray("templateType");
      if (templateType.size() > 0) {
        String templateTypeStr = templateType.get(0).toString();
        dto.setTemplateType(templateTypeStr);
        dto.setTemplateManual("TRUE");
      } else {
        dto.setTemplateType("");
        dto.setTemplateManual("FALSE");
      }

      // regDate
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      dto.setRegDate(sdf.format(new Date()));

      // log.info("dto : {}", dto);

      list.add(dto);
    }

    return list;
  }

}
