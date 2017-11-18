package core.entity

import org.bson.types.ObjectId

data class KnownIssue(var _id: ObjectId? = null,
                      var name: String,
                      var keyWords: MutableList<String>,
                      var comment: String)