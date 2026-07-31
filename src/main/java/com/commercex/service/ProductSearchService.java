package com.commercex.service;

import com.commercex.dto.ProductSearchRequest;
import com.commercex.dto.ProductSearchResponse;

public interface ProductSearchService {
    ProductSearchResponse searchProducts(ProductSearchRequest request);
}
