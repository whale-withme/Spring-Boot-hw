package com.whale.restaurant.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Table;

import com.whale.restaurant.enums.ProductStatus;

import lombok.Data;

@Data
@Table(name = "product")
public class ProductDetail {
    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer restaurantId;
    private ProductStatus status;
    private Date date;
}
