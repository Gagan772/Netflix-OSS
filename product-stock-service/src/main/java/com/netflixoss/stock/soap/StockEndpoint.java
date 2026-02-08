package com.netflixoss.stock.soap;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.netflixoss.stock.exception.ResourceNotFoundException;
import com.netflixoss.stock.service.ProductStockService;

@Endpoint
public class StockEndpoint {

    private final ProductStockService productStockService;

    public StockEndpoint(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @PayloadRoot(namespace = StockSoapNamespace.URI, localPart = "GetStockLevelRequest")
    @ResponsePayload
    public GetStockLevelResponse getStockLevel(@RequestPayload GetStockLevelRequest request) {
        GetStockLevelResponse response = new GetStockLevelResponse();
        try {
            int stock = productStockService.getStockLevel(request.getSku());
            response.setFound(true);
            response.setQuantity(stock);
        } catch (ResourceNotFoundException ex) {
            response.setFound(false);
            response.setQuantity(0);
        }
        return response;
    }

    @PayloadRoot(namespace = StockSoapNamespace.URI, localPart = "RestockProductRequest")
    @ResponsePayload
    public RestockProductResponse restockProduct(@RequestPayload RestockProductRequest request) {
        int newQuantity = productStockService.restockProduct(request.getSku(), request.getQty());

        RestockProductResponse response = new RestockProductResponse();
        response.setSuccess(true);
        response.setNewQuantity(newQuantity);
        response.setMessage("Product restocked");
        return response;
    }
}
