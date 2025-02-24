package com.reptile.wuthering.waves.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.reptile.wuthering.waves.domain.IpDO;
import com.reptile.wuthering.waves.domain.JymDO;
import com.reptile.wuthering.waves.domain.PxDO;
import com.reptile.wuthering.waves.util.JacksonUtil;
import com.reptile.wuthering.waves.util.JsoupUtil;
import com.reptile.wuthering.waves.util.RequestUtil;
import com.reptile.wuthering.waves.vo.ApiResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class JYMProductService {
    // 需要排除的常驻五星角色
    private static final Set<String> EXCLUDED_CHARACTERS = new HashSet<>(Arrays.asList(
            "维里奈", "凌阳", "鉴心", "安可", "卡卡罗"
    ));

    // 需要排除的常驻五星角色
    private static final Set<String> EXCLUDED_WUQI = new HashSet<>(Arrays.asList(
            "千古洑流", "浩境粼光", "停驻之烟", "擎渊怒涛", "漪澜浮录"
    ));
    public ApiResponse<List<JymDO>> selectJymSearchPageList() {
        // 目标URL
        String url = "https://m.jiaoyimao.com/jg2007615-3/f7973043-c7973044/o110/?rId=107";

        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5524.83 Safari/537.36 Edg/110.0.1586.41");
        headers.put("Referer", "https://m.jiaoyimao.com/");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Connection", "keep-alive");
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("Sec-Fetch-User", "?1");
        headers.put("Upgrade-Insecure-Requests", "1");

        // 用于存储 JymDO 对象的列表
        List<JymDO> jymList = new ArrayList<>();
        ApiResponse<List<JymDO>> resp = new ApiResponse<>();
      //  String json = RequestUtil.getRequest("http://localhost:8093/get");
      //  IpDO ipDO = JacksonUtil.toObject(json, new IpDO().getClass());
        try {
            // 发送HTTP请求并获取HTML文档
            Connection.Response response = Jsoup.connect(url)
                    .headers(headers)
                   // .proxy(ipDO.getIpAddress(),Integer.parseInt(ipDO.getPort()))
                    .method(Connection.Method.GET)
                    .execute();

//            JsoupUtil.sendGetRequest(url,1000);
            // 获取 Cookies
            Map<String, String> cookies = response.cookies();

            // 解析 HTML 文档
            Document document = response.parse();

            // 获取所有goods-list-dom类的元素
            Elements goodsList = document.getElementsByClass("goods-list-dom");
            // 如果未获取到 goods-list-dom，说明触发了反爬
            if (goodsList.isEmpty()) {
                System.out.println("触发反爬机制，切换到 Selenium 处理...");
                goodsList = handleWithSelenium(url);
            }
            for (Element goods : goodsList) {
                // 获取goods下的所有子节点
                for (int i = 1; i <= 5; i++) { // 排除第一条，循环2-5条
                    Node childNode = goods.childNode(i);

                    // 确保子节点是Element类型
                    if (childNode instanceof Element) {
                        Element item = (Element) childNode;

                        // 获取详情链接
                        String detailUrl = item.attr("href");

                        // 获取商品ID
                        String goodsId = item.attr("data-goods-id");

                        // 获取商品名称
                        String goodsName = item.attr("data-goods_name");

                        // 获取scene_id
                        String sceneId = item.attr("data-scene_id");

                        // 获取账号类型
                        String publisher = item.attr("data-publisher");

                        // 获取价格
                        String price = item.attr("data-price");

                        // 获取主图
                        String mainImage = item.select("img.lxg-image-img").attr("src");

                        // 获取发布时间
                        String releaseTime = item.select("div.right-footer span.right-footer-tag-text").text();

                        // 创建 JymDO 对象
                        JymDO jymDO = new JymDO();
                        jymDO.setProductId(goodsId);
                        jymDO.setDetailsLink(detailUrl);
                        jymDO.setProductName(goodsName);
                        jymDO.setGameId(sceneId);
                        jymDO.setPrice(price);
                        jymDO.setMainImageUrl(mainImage);
                        jymDO.setShelveUpTime(releaseTime);

                        // 将 JymDO 对象添加到列表中
                        jymList.add(jymDO);
                    }
                }

                // 随机延迟，避免频繁请求
                Thread.sleep(1000 + new Random().nextInt(1000));
            }

            // 封装成功响应
            resp.setSuccess(true);
            resp.setData(jymList);
            resp.setTotal(jymList.size());  // 设置总条数

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("请求失败: " + e.getMessage());
            resp.setSuccess(false);
            resp.setErrMessage(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("线程中断: " + e.getMessage());
            resp.setSuccess(false);
            resp.setErrMessage(e.getMessage());
        }

        return resp;
    }


    public ApiResponse<JymDO> jymDetail(String url2) {
       // String url="https://m.jiaoyimao.com/jg2007615/1739940461649332.html";
        ApiResponse<JymDO> resp=new ApiResponse<>();
        JymDO jymDO = new JymDO();
        try {
            // 设置代理
//            String proxyHost = "114.232.109.194";
//            int proxyPort = 8089;
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.5524.83 Safari/537.36 Edg/110.0.1586.41");
            headers.put("Referer", "https://m.jiaoyimao.com/");
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            headers.put("Accept-Language", "zh-CN,zh;q=0.9");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Connection", "keep-alive");
            headers.put("Sec-Fetch-Dest", "document");
            headers.put("Sec-Fetch-Mode", "navigate");
            headers.put("Sec-Fetch-Site", "same-origin");
            headers.put("Sec-Fetch-User", "?1");
            headers.put("Upgrade-Insecure-Requests", "1");
         //   String json = RequestUtil.getRequest("http://localhost:8093/get");
          //  IpDO ipDO = JacksonUtil.toObject(json, new IpDO().getClass());
            // 发送HTTP请求并获取HTML文档
            Connection.Response response = Jsoup.connect(url2)
                    .headers(headers)
                  //  .proxy(ipDO.getIpAddress(),Integer.parseInt(ipDO.getPort()))
                    .method(Connection.Method.GET)
                    .execute();

            // 获取 Cookies
            Map<String, String> cookies = response.cookies();

            // 解析 HTML 文档
            Document document = response.parse();

            // 获取所有goods-list-dom类的元素
            Elements goodsDetail = document.getElementsByClass("official-report");


            //  goodsDetail.select("div.item-verify-account-report-imgs > div.item");
            // 获取所有 item 元素
            Elements items = goodsDetail.select("div.item-property-info-group > div.item");
            int count=0;
//            Integer role=0;
//            Integer arms=0;
            int role2=0;
            for (Element detail : items) {
                // 提取 keys 和 value
//                jymDO=new JymDO();
                String key = detail.select("div.keys").text().trim();
                String value = detail.select("div.value").text().trim();
                if (key.equals("联觉等级")||key.equals("5星角色数")||key.equals("5星武器数")||key.equals("铸潮波纹")||key.equals("性别"))
                {
                    continue;
                }
                if (key.equals("角色黄数")){
//                    jymDO.setRoleCount(Integer.parseInt(value));
                    int role=Integer.parseInt(value);
                    //大于0说明有5星角色
                    if (role>0){
                        // -------------------------------
                        // 1. 解析角色信息 (5星角色) 1716190954420754
                        // -------------------------------
                        Element roleSection = document.getElementById("item-official-report-key-1714371842046659");
                        List<String> fiveStarRoles = new ArrayList<>();
                        if (roleSection==null){
                            roleSection = document.getElementById("item-official-report-key-1716190954420754");
                        }
                        if (roleSection != null) {
                            // 找到所有 hot-game-list-item
                            Elements hotGameItems = roleSection.select("div.hot-game-list-item");
                            for (Element item : hotGameItems) {
                                // 检查 item-tab 是否包含 "5星"
                                Element tab = item.selectFirst("div.item-tab:contains(5星)");
                                if (tab != null) {
                                    // 获取所有 name 值
                                    Elements names = item.select("div.cdm-image-wrap + span.name");
                                    List<String> collect = names.stream().map(name -> name.text().trim()).filter(name -> !EXCLUDED_CHARACTERS.contains(name)).collect(Collectors.toList());
                                    System.out.println(collect.size());
                                    if (collect!=null){
                                        role2=collect.size();
                                    }
                                }
                            }
                        }
                    }
                }
                if (key.equals("武器黄数")){
                    int arms=Integer.parseInt(value);
                    if (arms>0){
                        // -------------------------------
                        // 2. 解析武器信息 (5星武器) 1716523577393204
                        // -------------------------------
                        Element weaponSection = document.getElementById("item-official-report-key-1716523311463611");
                        List<String> fiveStarWeapons = new ArrayList<>();
                        if (weaponSection==null){
                            weaponSection = document.getElementById("item-official-report-key-1716523577393204");
                        }
                        if (weaponSection != null) {
                            // 找到所有 hot-game-list-item
                            Elements hotGameItems = weaponSection.select("div.hot-game-list-item");
                            for (Element item : hotGameItems) {
                                // 检查 item-tab 是否包含 "5星"
                                Element tab = item.selectFirst("div.item-tab:contains(5星)");
                                if (tab != null) {
                                    // 获取所有 name 值
                                    Elements names = item.select("div.cdm-image-wrap + span.name");
                                    List<String> collect = names.stream().map(name -> name.text().trim()).filter(name -> !EXCLUDED_WUQI.contains(name)).collect(Collectors.toList());
                                    System.out.println(collect.size());
                                    if (collect==null){
                                        jymDO.setArmsCount(0);
                                    }else{
                                        jymDO.setArmsCount(collect.size());
                                    }

                                }
                            }
                        }
                    }
                }
                if (key.equals("浮金波纹")){
                    count+=Integer.parseInt(value);
                }
                if (key.equals("唤声涡纹")){
                    count+=Integer.parseInt(value);
                }
                if (key.equals("星声")){
                    String replace = value.replace(",", "");
                    if (Integer.parseInt(replace)>160){
                        count+=Integer.parseInt(replace)/160;
                    }

                }
            }
            System.out.println("jymDOjymDO--count: " + count);
//             System.out.println("jymDOjymDO--name: " + jymDO.getProductName());
            jymDO.setLotteryCount(count);
            jymDO.setRoleCount(role2);
            resp.setSuccess(true);
            resp.setData(jymDO);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("请求失败: " + e.getMessage());
            resp.setSuccess(false);
            resp.setErrMessage(e.getMessage());

        }

        return resp;
    }

    private Elements handleWithSelenium(String url) {
        WebDriver driver = null;
        // 自动下载并配置 ChromeDriver
        WebDriverManager.chromedriver().setup();

        // 配置浏览器选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");  // 隐藏自动化标记
        options.addArguments("--headless");  // 无头模式（不显示浏览器界面）
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        try {
            // 初始化 Selenium WebDriver（以 Chrome 为例）
//            System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");
            driver = new ChromeDriver(options);

            // 打开目标页面
            driver.get(url);

//            // 处理滑动验证码（假设验证码是一个滑块）
            WebElement slider = driver.findElement(By.id("nc_1_n1z"));
            Actions actions = new Actions(driver);
            // 尝试拖动滑块到最右边（这里的300是一个假设值，你可能需要调整它）
            actions.clickAndHold(slider)
                    .moveByOffset(300, 0) // 尝试拖动300像素到右边
                    .release()
                    .perform();

            // 等待页面加载完成
            Thread.sleep(3000);

            // 获取页面源码并解析
            String pageSource = driver.getPageSource();
            return Jsoup.parse(pageSource).getElementsByClass("goods-list-dom");

        } catch (Exception e) {
            e.printStackTrace();
            return new Elements(); // 返回空列表
        } finally {
            if (driver != null) {
                driver.quit(); // 关闭浏览器
            }
        }
    }


    public static void main(String[] args) {
        String url="https://m.jiaoyimao.com//jg2007615-3/f7973043-c7973044/o110//_____tmd_____/punish?x5secdata=xcffn4oejRKu8a20i0XM4khS8UWIMavae%2fVHYGFl7h2jE4b5MZD6lCV59BOEfVQDFvmmR00hHPOQbH5f6E4EkJceBLnIJIioAodBMmhL5fDgxHdO1blalBHrg2Z0PJyJHb2j2k3BjuVvXW4nlsvD4BIxAkhGCamD3olqigBsNYxPc9InH%2br%2fmBw5Wndv2uDF%2fUWM8l%2b7CIM111Bd6pLQ%2bjj8dZRw92lDiCz5xC9GYInnsmfge01%2bcfBt3LU27QT4sHZU%2fxT56RB004%2b74n89QJVYbT6xevFdHmKjvqkQFvDRD2dUABPp2it03BO6XX%2bLor__bx__m.jiaoyimao.com%2fjg2007615-3%2ff7973043-c7973044%2fo110%2f&x5step=1";
        // 自动下载并配置 ChromeDriver
        WebDriverManager.chromedriver().setup();

        // 配置浏览器选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");  // 隐藏自动化标记
        options.addArguments("--headless");  // 无头模式（不显示浏览器界面）
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        // 初始化 WebDriver
        WebDriver driver = new ChromeDriver(options);
        try {
            // 打开目标页面
            driver.get(url);

            // 获取页面源码并解析
            String pageSource2 = driver.getPageSource();
//            // 处理滑动验证码（假设验证码是一个滑块）
            WebElement slider = driver.findElement(By.id("nc_1_n1z"));
            Actions actions = new Actions(driver);
            actions.clickAndHold(slider)
                    .moveByOffset(258, 0) // 尝试拖动300像素到右边
                    .release()
                    .perform();
            // 定位滑块和目标位置
//            WebElement slider = driver.findElement(By.id("nc_1_n1z"));
//            WebElement target = driver.findElement(By.id("target"));
//
//            // 获取滑块的起始位置
//            int sliderStartX = slider.getLocation().getX();
//            int targetX = target.getLocation().getX();
//
//            // 计算滑块需要移动的距离
//            int distance = targetX - sliderStartX;

            // 模拟鼠标拖动滑块
//            Actions actions = new Actions(driver);
//            actions.clickAndHold(slider).moveByOffset(distance, 0).release().perform();

            // 等待页面加载完成
            Thread.sleep(3000);

            // 获取页面源码并解析
            String pageSource = driver.getPageSource();
             Jsoup.parse(pageSource).getElementsByClass("goods-list-dom");

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (driver != null) {
                driver.quit(); // 关闭浏览器
            }
        }

    }


}
