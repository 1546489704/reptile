package com.reptile.wuthering.waves.schedule;

import com.reptile.wuthering.waves.domain.PxDO;
import com.reptile.wuthering.waves.request.PXProductService;
import com.reptile.wuthering.waves.service.PxService;
import com.reptile.wuthering.waves.util.RequestUtil;
import com.reptile.wuthering.waves.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class PxTask {

    private final PXProductService pxProductService;

    private final PxService pxService;

    // 钉钉机器人的 Webhook URL
    private static final String WEBHOOK_URL = "https://oapi.dingtalk.com/robot/send?access_token=14a207f9552a699bd62bf333c1de54f9a09bc4626132b5d69a37ff7ac108cfe1";

    @Scheduled(fixedRate = 20000)
    public void runTask() throws InterruptedException {
        log.info("拉取螃蟹交易数据---------------->>> start");
        ApiResponse<List<PxDO>> response = pxProductService.selectSearchPageList();
        if (!response.getSuccess()) {
            log.error("拉取螃蟹交易数据失败！！！错误码：{}, 原因：{}", response.getErrCode(), response.getErrMessage());
            return;
        }
        List<PxDO> list = response.getData();
        if (CollectionUtils.isEmpty(list)) {
            log.error("没有拉取到螃蟹交易数据失败！！！");
            return;
        }
        String template = "https://www.pxb7.com/product/%s/1?gameId=%s&price=%s&rcToken=%s";
        String pay="https://www.pxb7.com/confirm/%s";
        String mTemplate="https://m1.pxb7.com/pages-buy/ProductDetail/index?id=%s&gameId=10302&productType=1&rcToken=%s";
        String mPay="https://m1.pxb7.com/pages-buy/OrderConfirm/index?id=%s&gameId=10302&rcToken=%s&productType=1";
        for (PxDO pxDO : list) {
//            Thread.sleep(30000);
            //假如有重复就去掉
//            boolean b = pxService.removeById(pxDO.getProductId());
//            System.out.println(b);
            PxDO byId = pxService.getById(pxDO.getProductId()); //有重复就不拉去详情了
            if (byId!=null){
                System.out.println("旧数据"+byId.getProductName());
                continue;
            }

            ApiResponse<PxDO> detailResp = pxProductService.detail(pxDO.getProductId(), pxDO.getRcToken());
            if (!detailResp.getSuccess()) {
                log.error("拉取螃蟹商品详情数据失败！！！错误码：{}, 错误原因：{}", detailResp.getErrCode(), detailResp.getErrMessage());
            }

            PxDO detail = detailResp.getData();
            System.out.println("新数据"+detail.getProductName());
            // 提取五星角色部分
//            String[] parts = detail.getProductName().split("五星角色：|；");
//            String charactersPart = parts.length > 1 ? parts[1] : "";
//            int roleCount= pxProductService.countCharacters(charactersPart);
             //提取五星武器部分
//            parts = detail.getProductName().split("五星武器：");
//            String weaponsPart = parts.length > 1 ? parts[1].split("；")[0] : "";
//            int armsCount=pxProductService.countWeapons(weaponsPart);
            //统计限定角色，武器数量
            int roleCount= pxProductService.countValidFiveStarCharacters(detail.getProductName());
            int armsCount=pxProductService.countValidFiveStarWeapons(detail.getProductName());

            String detailsUrl = String.format(template, pxDO.getProductId(), pxDO.getGameId(), detail.getPrice(), pxDO.getRcToken());
            String mDetailsUrl=String.format(mTemplate, pxDO.getProductId(), pxDO.getRcToken());
            String payUrl=String.format(pay, pxDO.getProductId());
            String mpayUrl=String.format(mPay, pxDO.getProductId(),pxDO.getRcToken());
            pxDO.setPrice(detail.getPrice()/100); //实际价格除以100
            pxDO.setCreateTime(detail.getCreateTime());
            pxDO.setSellerId(detail.getSellerId());
            pxDO.setShelveUpTime(detail.getShelveUpTime());
            pxDO.setProductName(detail.getProductName());
            pxDO.setProductType(detail.getProductType());
//            pxDO.setArmsCount(detail.getArmsCount());
//            pxDO.setRoleCount(detail.getRoleCount());
            pxDO.setArmsCount(armsCount);
            pxDO.setRoleCount(roleCount);
            pxDO.setLotteryCount(detail.getLotteryCount());
            pxDO.setDetailsLink(detailsUrl);
            pxDO.setPayLink(payUrl);
            pxService.save(pxDO);
            sendCustomMarkdown(pxDO.getMainImageUrl(),pxDO.getShelveUpTime(),pxDO.getArmsCount(),pxDO.getRoleCount(),pxDO.getLotteryCount(),pxDO.getPrice(),pxDO.getProductName(),"查看详情",mDetailsUrl,"点击购买",mpayUrl);


        }

//        log.info("Array contents: {}", list.toArray());

        log.info("拉取螃蟹交易数据---------------->>> end");
    }

    public static void sendCustomMarkdown(
            String imageUrl,
            LocalDateTime time,
            Integer armsCount,
            Integer roleCount,
            Integer lotteryCount,
            long price,
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
        markdown.put("title", "螃蟹通知");  // 消息标题（显示在通知栏）
        markdown.put("text", ""+markdownText);    // Markdown 内容
        message.put("markdown", markdown);

        // 发送请求
        String response = RequestUtil.postRequest(WEBHOOK_URL, message, String.class);
        System.out.println("钉钉推送结果: " + response);
    }
}
