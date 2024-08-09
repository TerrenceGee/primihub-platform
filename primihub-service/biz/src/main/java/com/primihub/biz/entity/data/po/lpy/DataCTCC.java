package com.primihub.biz.entity.data.po.lpy;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DataCTCC {
    private Long id;
    /** todo 待实现 */
    private String ctcc;
    private Double y;
    /** 以下是营销分 */
    private Double score;
    @JsonIgnore
    private Integer isDel;
    @JsonIgnore
    private Date createDate;
    @JsonIgnore
    private Date updateDate;
}
