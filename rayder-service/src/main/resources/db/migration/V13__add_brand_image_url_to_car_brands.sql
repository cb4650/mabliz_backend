ALTER TABLE car_brands
    ADD COLUMN brand_image_url VARCHAR(255) NOT NULL DEFAULT 'https://www.shutterstock.com/image-vector/flat-car-picture-placeholder-symbol-600nw-2366856295.jpg';

UPDATE car_brands
SET brand_image_url = 'https://www.shutterstock.com/image-vector/flat-car-picture-placeholder-symbol-600nw-2366856295.jpg'
WHERE brand_image_url IS NULL;
