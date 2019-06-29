package com.teatez.lms.data

import com.mongodb.MongoClient
import org.bson.BsonDocument

val mongoClient = MongoClient()
val db = mongoClient.getDatabase("test com.teatez.lms.data.getDb")
val collection = db.getCollection("test com.teatez.lms.data.getCollection", BsonDocument::class.java)