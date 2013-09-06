package com.imaginea.feeder.mongo;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class ProductDAO {
    private final DBCollection products;

    public ProductDAO(MongoClient mongoClient, String dbname, String collection) {
        products = mongoClient.getDB(dbname).getCollection(collection);
    }

    public void addProduct(String productJSON) {
        products.save((DBObject) JSON.parse(productJSON));
    }

    public static void main(String[] args) throws UnknownHostException {
        MongoClient mongo = new MongoClient("localhost", 27017);
        DB feederDB = mongo.getDB("feeder");
        DBCollection products = feederDB.getCollection("products");
        System.out.println(products.find().size());
        mongo.close();
    }
}
