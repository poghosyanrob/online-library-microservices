package org.example.orderservice.mapper;

import org.example.orderservice.model.BookOrder;
import org.example.orderservice.model.dto.OrderDto;
import org.example.orderservice.model.dto.OrderRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDto toDto(BookOrder order);

    List<OrderDto> toDtoList(List<BookOrder> orders);

    BookOrder toEntity(OrderRequest request);
}
