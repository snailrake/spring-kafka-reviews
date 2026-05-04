package com.snailrake.dataservice.mapper;

import com.snailrake.dataservice.dto.ReviewEvent;
import com.snailrake.dataservice.model.NewReview;
import com.snailrake.dataservice.model.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewEventMapper {

    @Mapping(target = "name", source = "restaurantName")
    Restaurant toRestaurant(ReviewEvent event);

    NewReview toNewReview(ReviewEvent event);
}