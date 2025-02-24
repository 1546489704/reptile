package com.reptile.wuthering.waves.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reptile.wuthering.waves.domain.PxDO;
import com.reptile.wuthering.waves.service.PxService;
import com.reptile.wuthering.waves.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class PxController {
    @Autowired
    private PxService pxService;

    @GetMapping("/list")
    public R list() {
        // 创建分页对象
        Page<PxDO> page = new Page<>(1, 10);
        QueryWrapper<PxDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("shelve_up_time"); // 按 create_time 字段降序排序
        List<PxDO> list = pxService.list(page,queryWrapper);
        return R.ok().put("Px", list);

    }
}
