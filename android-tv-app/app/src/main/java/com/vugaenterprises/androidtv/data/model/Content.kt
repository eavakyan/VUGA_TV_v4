package com.vugaenterprises.androidtv.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "content")
data class Content(
    @PrimaryKey
    @SerializedName("content_id")
    val contentId: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("type")
    val type: Int = 0,

    @SerializedName("duration")
    val duration: String = "",

    @SerializedName("release_year")
    val releaseYear: Int = 0,

    @SerializedName("ratings")
    val ratings: Double = 0.0,

    @SerializedName("language_id")
    val languageId: Int = 0,

    @SerializedName("download_link")
    val downloadLink: String = "",

    @SerializedName("trailer_url")
    val trailerUrl: String = "",

    @SerializedName("vertical_poster")
    val verticalPoster: String = "",

    @SerializedName("horizontal_poster")
    val horizontalPoster: String = "",

    @SerializedName("genre_ids")
    val genreIds: String = "",

    @SerializedName("is_featured")
    val isFeatured: Int = 0,

    @SerializedName("is_show")
    val isShow: Int = 0,

    @SerializedName("total_download")
    val totalDownload: Int = 0,

    @SerializedName("total_share")
    val totalShare: Int = 0,

    @SerializedName("total_view")
    val totalView: Int = 0,

    @SerializedName("actor_ids")
    val actorIds: String = "",

    @SerializedName("is_watchlist")
    val isWatchlist: Boolean = false,

    @SerializedName("contentCast")
    val contentCast: List<CastItem> = emptyList(),

    @SerializedName("content_sources")
    val contentSources: List<SourceItem> = emptyList(),

    @SerializedName("seasons")
    val seasons: List<SeasonItem> = emptyList(),

    @SerializedName("more_like_this")
    val moreLikeThis: List<Content> = emptyList(),

    @SerializedName("content_subtitles")
    val contentSubtitles: List<SubtitlesItem> = emptyList(),
    
    // Local fields for Room database
    var isFavorite: Boolean = false,
    var watchProgress: Float = 0f,
    var completed: Boolean = false
) {
    // Helper properties
    val genreList: List<String>
        get() = genreIds.split(",").filter { it.isNotBlank() }
    
    val genreString: String
        get() = genreIds.replace(",", ", ")
}

data class CastItem(
    @SerializedName("content_cast_id")
    val id: Int = 0,
    
    @SerializedName("content_id")
    val contentId: Int = 0,
    
    @SerializedName("actor_id")
    val actorId: Int = 0,
    
    @SerializedName("character_name")
    val characterName: String = "",
    
    @SerializedName("actor")
    val actor: ActorItem = ActorItem()
)

data class ActorItem(
    @SerializedName("actor_id")
    val id: Int = 0,
    
    @SerializedName("fullname")
    val name: String = "",
    
    @SerializedName("profile_image")
    val image: String = ""
)

data class SourceItem(
    @SerializedName("content_source_id")
    val id: Int = 0,
    
    @SerializedName("content_id")
    val contentId: Int = 0,
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("quality")
    val quality: String = "",
    
    @SerializedName("size")
    val size: String = "",
    
    @SerializedName("is_download")
    val isDownload: Int = 0,
    
    @SerializedName("access_type")
    val accessType: Int = 0,
    
    @SerializedName("type")
    val type: Int = 0, // 1=youtube, 2=m3u8, 7=file
    
    @SerializedName("source")
    val source: String = "",
    
    @SerializedName("episode_id")
    val episodeId: Int = 0,
    
    @SerializedName("media")
    val mediaItem: MediaItem? = null
) {
    // Local properties for download/playback state
    var downloadStatus: Int = 0
    var progress: Int = 0
    var playProgress: Int = 0
    var time: Long = 0
}

data class MediaItem(
    @SerializedName("media_gallery_id")
    val id: Int = 0,
    
    @SerializedName("file")
    val file: String = "",
    
    @SerializedName("title")
    val title: String = ""
)

data class SubtitlesItem(
    @SerializedName("subtitle_id")
    val id: Int = 0,
    
    @SerializedName("episode_id")
    val episodeId: Int = 0,
    
    @SerializedName("content_id")
    val contentId: Int = 0,
    
    @SerializedName("language_id")
    val languageId: Int = 0,
    
    @SerializedName("file")
    val subtitleFile: String = ""
)

data class SeasonItem(
    @SerializedName("season_id")
    val id: Int = 0,
    
    @SerializedName("content_id")
    val contentId: Int = 0,
    
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("trailer_url")
    val trailerUrl: String = "",
    
    @SerializedName("episodes")
    val episodes: List<EpisodeItem> = emptyList()
)

data class EpisodeItem(
    @SerializedName("episode_id")
    val id: Int = 0,

    @SerializedName("season_id")
    val seasonId: Int = 0,

    @SerializedName("number")
    val number: Int = 0,

    @SerializedName("thumbnail")
    val thumbnail: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("duration")
    val duration: String = "",

    @SerializedName("access_type")
    val accessType: Int = 0,

    @SerializedName("total_view")
    val totalView: Int = 0,

    @SerializedName("total_download")
    val totalDownload: Int = 0,

    @SerializedName("sources")
    val sources: List<SourceItem> = emptyList(),

    @SerializedName("subtitles")
    val subtitles: List<SubtitlesItem> = emptyList()
) 