package com.whale.restaurant.service;

import com.whale.restaurant.dto.OrderMessageDTO;

public interface RestaurantService {
    void handleOrderMessage(OrderMessageDTO orderMessageDTO);
}
