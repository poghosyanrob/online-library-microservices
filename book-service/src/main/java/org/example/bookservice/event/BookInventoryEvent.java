package org.example.bookservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookInventoryEvent implements Serializable {

    private static final long serialVersionUID = 2L;

    private Long id;
    private String title;
    private String author;
    private Double price;
    private Integer quantity;
    private String actionType;

}
