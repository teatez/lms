package com.teatez.lms.data

import com.mongodb.MongoClient
import org.bson.BsonDocument

val mongoClient = MongoClient("127.0.0.0", 27017)
val db = mongoClient.getDatabase("testDb")
val collection = db.getCollection("testCollection", BsonDocument::class.java)

