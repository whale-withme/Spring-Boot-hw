package com.whale.restaurant.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.whale.restaurant.enums.RestaurantStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Entity
@Table(name = "restaurant")
@AllArgsConstructor
public class RestaurantDetail {
    @Id
    private int id;
    private String name;
    private String addressString;
    private RestaurantStatus status;
    private Date date;
    @Column(name = "settlement_id")
    private int settlementId;
}
