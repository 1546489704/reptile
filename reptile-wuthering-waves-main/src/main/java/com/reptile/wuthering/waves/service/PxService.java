package com.reptile.wuthering.waves.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reptile.wuthering.waves.domain.PxDO;
import com.reptile.wuthering.waves.mapper.PxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *
 * </p>
 *
 * @since 2025/2/11
 */
@Service
@RequiredArgsConstructor
public class PxService extends ServiceImpl<PxMapper, PxDO> {

}
