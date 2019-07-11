package com.teatez.lms.data

import com.mongodb.MongoClient
import com.mongodb.MongoException
import org.bson.BsonDocument

object Handiler {

    fun splitIntoSingleJson(): List<BsonDocument> {
        val testObj = BsonImporter.resourceToBsonElement("/testDb.json")
        val test = testObj.getDocument("testObjectOne")
        val list = test.map { (_, value) ->
            value.asDocument().getDocument("data")
        }
    return list
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var mongoClient: MongoClient? = null
        try {
            mongoClient = MongoClient("127.0.0.1", 27017)
            println("Connected to MongoDB!")
        } catch (e: MongoException) {
            e.printStackTrace()
        } finally {
            mongoClient!!.close()
        }
    }

}

