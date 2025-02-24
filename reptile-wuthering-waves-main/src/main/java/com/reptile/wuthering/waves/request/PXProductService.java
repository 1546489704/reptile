package com.reptile.wuthering.waves.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.reptile.wuthering.waves.domain.PxDO;
import com.reptile.wuthering.waves.util.JacksonUtil;
import com.reptile.wuthering.waves.util.RequestUtil;
import com.reptile.wuthering.waves.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @since 2025/2/11
 */
@Service
@RequiredArgsConstructor
public class PXProductService {
    // 命座规则映射
    private static final Map<String, Integer> CONSTELLATION_MAP = new HashMap<>();
    static {
        CONSTELLATION_MAP.put("1命", 1);
        CONSTELLATION_MAP.put("2命", 1);
        CONSTELLATION_MAP.put("3命", 1);
        CONSTELLATION_MAP.put("4命", 1);
        CONSTELLATION_MAP.put("5命", 1);
        CONSTELLATION_MAP.put("满命", 1);
    }

    // 精炼规则映射
    private static final Map<String, Integer> REFINEMENT_MAP = new HashMap<>();
    static {
        REFINEMENT_MAP.put("精1", 1);
        REFINEMENT_MAP.put("精2", 1);
        REFINEMENT_MAP.put("精3", 1);
        REFINEMENT_MAP.put("精4", 1);
        REFINEMENT_MAP.put("精5", 1);
    }

    // 需要排除的常驻五星角色
    private static final Set<String> EXCLUDED_CHARACTERS = new HashSet<>(Arrays.asList(
            "维里奈", "凌阳", "鉴心", "安可", "卡卡罗"
    ));

    // 需要排除的常驻五星角色
    private static final Set<String> EXCLUDED_WUQI = new HashSet<>(Arrays.asList(
            "千古洑流", "浩境粼光", "停驻之烟", "擎渊怒涛", "漪澜浮录"
    ));

    public ApiResponse<List<PxDO>> selectSearchPageList() {
        String json = "{\"query\":\"\",\"gameId\":\"10302\",\"pageIndex\":1,\"pageSize\":6,\"bizProd\":1,\"type\":\"1\",\"posType\":1,\"filterDTOList\":[],\"sortAttrId\":\"\",\"sortType\":2}";
        String res = RequestUtil.postRequest("https://www.pxb7.com/api/search/product/selectSearchPageList", json, String.class);
        return JacksonUtil.toObject(res, new TypeReference<ApiResponse<List<PxDO>>>() {});
    }

    public ApiResponse<PxDO> detail(String productId, String rcToken) {
        String requestURl = String.format("https://www.pxb7.com/api/product/web/product/detail?productId=%s&rcToken=%s", productId, rcToken);
        String json = RequestUtil.getRequest(requestURl);
        ApiResponse<PxDO> resp = JacksonUtil.toObject(json, new TypeReference<ApiResponse<PxDO>>() {});
        PxDO data = resp.getData();
        if (Objects.isNull(data)) {
            return resp;
        }
        List<Map<String, Object>> productAttrList = data.getProductAttrs();
        if (!CollectionUtils.isEmpty(productAttrList)) {
            Map<Object, Map<String, Object>> productAttrMap = productAttrList.stream().collect(Collectors.toMap(map -> map.get("attrId"), v -> v, (k, v) -> k));
//            //角色: 103022
//            data.setRoleCount(getCount(productAttrMap, "103022"));
//            //武器: 123467876401172
//            data.setArmsCount(getCount(productAttrMap, "123467876401172"));
            // 浮金波纹: 128593741193310 铸潮波纹: 128593827143724 星声: 128593869357091
            Map<String, Object> attr1 = productAttrMap.get("128593741193310");
//            Map<String, Object> attr2 = productAttrMap.get("128593827143724");
            Map<String, Object> attr3 = productAttrMap.get("128593869357091");
            int lotteryCount = 0;
            lotteryCount += getLc(attr1);
//            lotteryCount += getLc(attr2);
            lotteryCount += getLc(attr3) / 160;
            data.setLotteryCount(lotteryCount);
        }

        return resp;
    }

    // 统计符合条件的五星角色数量
    public int countValidFiveStarCharacters(String input) {
        // 需要排除的五星角色
        Set<String> excludedCharacters = new HashSet<>();
        excludedCharacters.add("维里奈");
        excludedCharacters.add("凌阳");
        excludedCharacters.add("鉴心");
        excludedCharacters.add("安可");
        excludedCharacters.add("卡卡罗");

        // 正则表达式模式，用于匹配五星角色列表
        String characterPatternString = "(\\d+个五星角色：([^；]+)；)";
        Pattern characterPattern = Pattern.compile(characterPatternString);
        Matcher characterMatcher = characterPattern.matcher(input);

        int validCharacterCount = 0;

        if (characterMatcher.find()) {
            String characterList = characterMatcher.group(2);
            String[] characters = characterList.split(",");
            for (String character : characters) {
                // 去除前后的空格、可能的"命"字前缀以及括号内的内容（如果有）
                String cleanedCharacter = character.replaceAll("^\\d+命|满命\\s*|\\s*$", "").trim();
                if (!excludedCharacters.contains(cleanedCharacter)) {
                    validCharacterCount++;
                 //   System.out.println("原始角色: " + character + " → 清理后: " + cleanedCharacter);
                }

            }
        }

        return validCharacterCount;
    }

    // 统计符合条件的五星武器数量
    public int countValidFiveStarWeapons(String input) {
        // 需要排除的五星武器
        Set<String> excludedWeapons = new HashSet<>();
        excludedWeapons.add("千古洑流");
        excludedWeapons.add("浩境粼光");
        excludedWeapons.add("停驻之烟");
        excludedWeapons.add("擎渊怒涛");
        excludedWeapons.add("漪澜浮录");

        // 正则表达式模式，用于匹配五星武器列表
        String weaponPatternString = "(\\d+个五星武器：([^；]+)；)";
        Pattern weaponPattern = Pattern.compile(weaponPatternString);
        Matcher weaponMatcher = weaponPattern.matcher(input);

        int validWeaponCount = 0;

        if (weaponMatcher.find()) {
            String weaponList = weaponMatcher.group(2);
            String[] weapons = weaponList.split(",");
            for (String weapon : weapons) {
                // 去除前后的空格和可能的"精几"前缀
                String cleanedWeapon = weapon.replaceAll("^精\\d+\\s*|\\s*$", "").trim();
                if (!excludedWeapons.contains(cleanedWeapon)) {
                    validWeaponCount++;
                  //  System.out.println("原始武器: " + weapon + " → 清理后: " + cleanedWeapon);

                }
            }
        }

        return validWeaponCount;
    }

    /**
     * 计算五星角色总数
     */
    public int countCharacters(String input) {
        int count = 0;
        Pattern pattern = Pattern.compile("(\\d命|满命)?([^,]+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String constellation = matcher.group(1);
            String name = matcher.group(2).trim();
            // 如果角色在排除列表中，跳过
            if (EXCLUDED_CHARACTERS.contains(name)) {
                continue;
            }
            if (constellation != null) {
                count += CONSTELLATION_MAP.getOrDefault(constellation, 1);
            } else {
                count += 1; // 无命座信息默认1个
            }
        }
        return count;
    }

    /**
     * 计算五星武器总数
     */
    public int countWeapons(String input) {
        int count = 0;
        Pattern pattern = Pattern.compile("精(\\d)([^,]+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String refinement = "精" + matcher.group(1);
            String name=matcher.group(2).trim();
            if (EXCLUDED_WUQI.contains(name)){
                continue;
            }
            count += REFINEMENT_MAP.getOrDefault(refinement, 1);
        }
        return count;
    }

    private static Integer getCount(Map<Object, Map<String, Object>> productAttrMap, String attrId) {
        Map<String, Object> armsAttr = productAttrMap.get(attrId);
        List<Map<String, Object>> attrValList = (List<Map<String, Object>> ) MapUtils.getObject(armsAttr, "attrVals");
        if (!CollectionUtils.isEmpty(attrValList)) {
//            if (attrId=="103022"){
//                attrValList.
//            }
            return attrValList.size();
        }
        return 0;
    }

    private static Integer getLc(Map<String, Object> attr3) {
        if (MapUtils.isNotEmpty(attr3)) {
            List<Map<String, Object>> attrValList = (List<Map<String, Object>> ) MapUtils.getObject(attr3, "attrVals");
            if (!CollectionUtils.isEmpty(attrValList)) {
                String lc = MapUtils.getString(attrValList.get(0), "attrValue", "0");
                return Integer.parseInt(lc);
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        String json = "{\"query\":\"\",\"gameId\":\"10302\",\"pageIndex\":1,\"pageSize\":12,\"bizProd\":1,\"type\":\"1\",\"posType\":1,\"filterDTOList\":[],\"sortAttrId\":\"\",\"sortType\":2}";
        String res = RequestUtil.postRequest("https://www.pxb7.com/api/search/product/selectSearchPageList", json, String.class);
        System.out.println(res);
    }

}

