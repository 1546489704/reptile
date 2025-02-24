package com.reptile.wuthering.waves.vo;

import lombok.Data;

//import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @since 2025/2/11
 */
@Data
public class ApiResponse<T> implements Serializable {


//    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean success;

    private String errCode;

    private String errMessage;

    private Integer total;

    private T data;

}
