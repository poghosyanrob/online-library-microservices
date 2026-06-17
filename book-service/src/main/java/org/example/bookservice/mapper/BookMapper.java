package org.example.bookservice.mapper;

import org.example.bookservice.model.Book;
import org.example.bookservice.model.dto.BookDto;
import org.example.bookservice.model.dto.BookRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {

    BookDto toDto(Book book);

    List<BookDto> toDtoList(List<Book> books);

    Book toEntity(BookRequest request);

    void updateEntityFromRequest(BookRequest request, @MappingTarget Book book);

}
