package pl.mobi.ui.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Product {
    private String id;
    private String name;
    private double price;
    private String img;
}
