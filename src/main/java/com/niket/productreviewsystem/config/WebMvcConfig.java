package com.niket.productreviewsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration class to map the physical storage directory for product images
 * to a public URL path, allowing the browser to access them.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1. Define the physical upload directory on the file system.
        // This is the 'product-assets' folder in the root of your project.
        Path productUploadDir = Paths.get("./product-assets");
        String absolutePath = productUploadDir.toFile().getAbsolutePath();

        // 2. Map the public URL path to the absolute file system path.
        // When a request comes in for /product-assets/**, Spring will look
        // in the absolute directory defined above.
        // The 'file:///' prefix and trailing slash are crucial for Windows paths.
        registry.addResourceHandler("/product-assets/**")
                .addResourceLocations("file:///" + absolutePath + "/");

        // Ensure other static resources (CSS, JS, images in src/main/resources/static) still work
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}