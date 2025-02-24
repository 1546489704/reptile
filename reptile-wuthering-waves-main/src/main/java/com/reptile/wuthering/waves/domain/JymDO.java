package com.reptile.wuthering.waves.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//import java.io.Serial;

/**
 * <p>
 * 
 * </p>
 *
 * @author xx
 * @since 2025-02-11
 */
@Getter
@Setter
@TableName("t_jym")
public class JymDO implements Serializable{

//    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id")
    private String productId;

    @TableField("rc_token")
    private String rcToken;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 游戏ID
     */
    @TableField("game_id")
    private String gameId;

    /**
     * 价格
     */
    @TableField("price")
    private String price;

    /**
     * 商品唯一号
     */
    @TableField("product_unique_no")
    private String productUniqueNo;

    /**
     * 商品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 商品类型
     */
    @TableField("product_type")
    private Integer productType;

    /**
     * 上架时间
     */
    @TableField("shelve_up_time")
    private String shelveUpTime;

    /**
     * 商品创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 主图
     */
    @TableField("main_image_url")
    private String mainImageUrl;

    /**
     * 武器数量
     */
    @TableField("arms_count")
    private Integer armsCount;

    /**
     * 角色数量
     */
    @TableField("role_count")
    private Integer roleCount;

    /**
     * 抽奖次数
     */
    @TableField("lottery_count")
    private Integer lotteryCount;

    /**
     * 下单链接
     */
    @TableField("pay_link")
    private String payLink;

    /**
     * 详情链接
     */
    @TableField("details_link")
    private String detailsLink;

    /**
     * 卖家id
     */
    @TableField("seller_id")
    private String sellerId;

    @TableField(exist = false)
    private List<Map<String,Object>> productAttrs;
}
