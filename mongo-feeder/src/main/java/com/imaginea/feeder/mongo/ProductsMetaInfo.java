package com.imaginea.feeder.mongo;

import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.node.ObjectNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductsMetaInfo {
    private int totalPages, total, currentPage;

    private List<ObjectNode> products;

    @XmlElementWrapper(name = "products")
    public List<ObjectNode> getProducts() {
        return products;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
