package com.whale.restaurant.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.whale.restaurant.entity.RestaurantDetail;

public interface RestaurantDao extends JpaRepository<RestaurantDetail, Integer>{
    
}
