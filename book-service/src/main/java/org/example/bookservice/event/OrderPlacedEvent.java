package org.example.bookservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long bookId;
    private Integer quantity;
}
