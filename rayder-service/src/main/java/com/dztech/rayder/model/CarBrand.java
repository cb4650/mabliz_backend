package com.dztech.rayder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "car_brands")
@Getter
@Setter
@NoArgsConstructor
public class CarBrand {

    @Id
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "brand_image_url", nullable = false, length = 255)
    private String brandImageUrl;
}
