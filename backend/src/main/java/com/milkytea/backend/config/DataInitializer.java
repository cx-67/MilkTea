package com.milkytea.backend.config;

import com.milkytea.backend.entity.Brand;
import com.milkytea.backend.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final BrandRepository brandRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            initBrands();
        };
    }

    private void initBrands() {
        log.info("Checking and initializing brands...");

        List<Brand> brands = Arrays.asList(
            createBrand("喜茶", "灵感之茶", "/logos/喜茶.jpg"),
            createBrand("奈雪的茶", "一杯好茶，一口软欧包", "https://q8.itc.cn/q_70/images03/20250506/ca06d6e896f74406be94825cb54facf7.png"),
            createBrand("茶百道", "鲜果与中国茶", "/logos/茶百道.png"),
            createBrand("古茗", "每天一杯喝不腻", "/logos/古茗.png"),
            createBrand("蜜雪冰城", "你爱我，我爱你，蜜雪冰城甜蜜蜜", "/logos/蜜雪冰城.png"),
            createBrand("一点点", "台式手摇茶", "/logos/一点点.png"),
            createBrand("CoCo都可", "分享快乐", "/logos/coco都可.png"),
            createBrand("霸王茶姬", "以东方茶，会世界友", "/logos/霸王茶姬.png"),
            createBrand("煲珠公", "做女儿也爱喝的珍珠奶茶", "/logos/煲珠公.png"),
            createBrand("茉莉奶白", "东方茉莉，白茶底", "/logos/茉莉奶白.png"),
            createBrand("茶颜悦色", "中式茶饮", "/logos/茶颜悦色.png")
        );

        int count = 0;
        int updated = 0;
        for (Brand brand : brands) {
            Brand existingBrand = brandRepository.findByName(brand.getName()).orElse(null);
            if (existingBrand == null) {
                brandRepository.save(brand);
                count++;
            } else {
                // Update existing brand logo if changed
                if (!existingBrand.getLogoUrl().equals(brand.getLogoUrl())) {
                    existingBrand.setLogoUrl(brand.getLogoUrl());
                    existingBrand.setDescription(brand.getDescription());
                    brandRepository.save(existingBrand);
                    updated++;
                }
            }
        }
        
        log.info("Initialized {} new brands, updated {} brands.", count, updated);
    }

    private Brand createBrand(String name, String description, String logoUrl) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription(description);
        brand.setLogoUrl(logoUrl);
        return brand;
    }
}
