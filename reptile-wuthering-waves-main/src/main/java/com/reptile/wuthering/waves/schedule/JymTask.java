package com.reptile.wuthering.waves.schedule;

import com.reptile.wuthering.waves.domain.JymDO;
import com.reptile.wuthering.waves.domain.PxDO;
import com.reptile.wuthering.waves.request.JYMProductService;
import com.reptile.wuthering.waves.request.PXProductService;
import com.reptile.wuthering.waves.service.JymService;
import com.reptile.wuthering.waves.service.PxService;
import com.reptile.wuthering.waves.util.RequestUtil;
import com.reptile.wuthering.waves.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @since 2025/2/11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JymTask {

    private final PXProductService pxProductService;

    private final PxService pxService;

    private final JYMProductService jymProductService;

    private final JymService jymService;

    // 钉钉机器人的 Webhook URL
    private static final String WEBHOOK_URL = "https://oapi.dingtalk.com/robot/send?access_token=14a207f9552a699bd62bf333c1de54f9a09bc4626132b5d69a37ff7ac108cfe1";

    @Scheduled(fixedRate = 10000)
    public void runTask() throws InterruptedException {
        log.info("拉取交易猫交易数据---------------->>> start");
        ApiResponse<List<JymDO>> response = jymProductService.selectJymSearchPageList();
        if (!response.getSuccess()) {
            log.error("拉取交易猫交易数据失败！！！错误码：{}, 原因：{}", response.getErrCode(), response.getErrMessage());
            return;
        }
        List<JymDO> list = response.getData();
        if (CollectionUtils.isEmpty(list)) {
            log.error("没有拉取到拉取交易猫交易数据失败！！！错误码：{}, 原因：{}",response.getErrCode(), response.getErrMessage());
            return;
        }
        for (JymDO jymDO : list) {

            JymDO byId = jymService.getById(jymDO.getProductId());
           // System.out.println("byId"+byId.getPrice());
            if (byId != null) {
                System.out.println("旧数据"+byId.getProductName());
                continue;
            }
            ApiResponse<JymDO> jymDOApiResponse = jymProductService.jymDetail(jymDO.getDetailsLink());
            if (!jymDOApiResponse.getSuccess()) {
                log.error("拉取交易猫商品详情数据失败！！！错误码：{}, 错误原因：{}", jymDOApiResponse.getErrCode(), jymDOApiResponse.getErrMessage());
            }
            System.out.println("id"+jymDO.getPrice());
            JymDO data = jymDOApiResponse.getData();
            System.out.println("新数据"+data.getProductName());
            jymDO.setRoleCount(data.getRoleCount());
            jymDO.setArmsCount(data.getArmsCount());
            jymDO.setLotteryCount(data.getLotteryCount());
            jymDO.setPayLink(jymDO.getDetailsLink());
            // 构建 HTML 消息
            String htmlContent = "<div style='font-family: Arial, sans-serif;'>" +
                    "<img src='" + jymDO.getMainImageUrl() + "' style='width: 100%; max-width: 600px;margin-top: -33%;' alt='大图'>" +
                    "<p><strong>价格</strong>: " + jymDO.getPrice() + "</p>" +
                    "<p><strong>限定角色</strong>: " + jymDO.getRoleCount() + "</p>" +
                    "<p><strong>限定武器</strong>: " + jymDO.getArmsCount() + "</p>" +
                    "<p><strong>抽卡总量</strong>: " + jymDO.getLotteryCount() + "</p>" +
                    "<p><strong>上架时间</strong>: " + jymDO.getShelveUpTime() + "</p>" +
                    "<hr>" +  // 分隔线
                    "<p><a href='" + jymDO.getDetailsLink() + "'>👉 查看详情</a></p>" +
                    "<p><a href='" + jymDO.getPayLink() + "'>👉 点击购买</a></p>" +
                    "</div>";
//            sendDingTalkMessage(htmlContent);
            sendCustomMarkdown(jymDO.getMainImageUrl(),jymDO.getShelveUpTime(),jymDO.getArmsCount(),jymDO.getRoleCount(),jymDO.getLotteryCount(),jymDO.getPrice(),jymDO.getProductName(),"查看详情",jymDO.getDetailsLink(),"点击购买",jymDO.getPayLink());
            jymService.save(jymDO);
        }



        log.info("拉取交易猫交易数据---------------->>> end");
    }

    public static void sendCustomMarkdown(
            String imageUrl,
            String time,
            Integer armsCount,
            Integer roleCount,
            Integer lotteryCount,
            String price,
            String description,
            String link1Title,
            String link1Url,
            String link2Title,
            String link2Url) {

        // 构建 Markdown 内容
        String markdownText = "![大图](" + imageUrl + ")\n\n" +
                "**价格**: " + price + "\n\n" +
                "**限定角色**: " + roleCount + "\n\n" +
                "**限定武器**: " + armsCount + "\n\n" +
                "**抽卡总量**: " + lotteryCount + "\n\n" +
                "**上架时间**: " + time + "\n\n" +
                "---\n" +  // 分隔线
//                description + "\n\n" +
                "[👉 " + link1Title + "](" + link1Url + ")\n" +
                "[👉 " + link2Title + "](" + link2Url + ")";


        // 构建请求体
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");

        Map<String, String> markdown = new HashMap<>();
        markdown.put("title", "交易猫通知");  // 消息标题（显示在通知栏）
        markdown.put("text", markdownText);    // Markdown 内容
        message.put("markdown", markdown);

        // 发送请求
        String response = RequestUtil.postRequest(WEBHOOK_URL, message, String.class);
        System.out.println("钉钉推送结果: " + response);
    }


    public static void sendDingTalkMessage(String htmlContent) {

        // 构建请求体
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");

        Map<String, String> markdown = new HashMap<>();
        markdown.put("title", "交易猫通知");  // 消息标题（显示在通知栏）
        markdown.put("text", htmlContent);    // Markdown 内容
        message.put("markdown", markdown);
        System.out.println("钉钉推送结果html: " + htmlContent);
        String s = RequestUtil.postRequest(WEBHOOK_URL, message, String.class);
        System.out.println("钉钉推送结果: " + s);
            // 发送请求并获取响应

    }

}
