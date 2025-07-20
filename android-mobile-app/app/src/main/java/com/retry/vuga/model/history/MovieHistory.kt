package com.retry.vuga.model.history

import com.retry.vuga.model.ContentDetail.SourceItem

class MovieHistory(
    var id: Int? = null,
    var movieId: Int? = null,
    var movieName: String? = null,
    var thumbnail: String? = null,
    var time: Long? = null,
    var sources: ArrayList<SourceItem>? = null
)
