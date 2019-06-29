package com.teatez.lms.data

import com.mongodb.MongoClient
import org.bson.BsonDocument

val mongoClient = MongoClient()
val db = mongoClient.getDatabase("testDb")
val collection = db.getCollection("testCollection", BsonDocument::class.java)