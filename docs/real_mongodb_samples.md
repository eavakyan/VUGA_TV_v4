# Real MongoDB Document Examples

Based on actual data extracted from your database, here are the MongoDB document structures:

## Movie Document - "F1" (2025 Formula One Movie)

```json
{
  "_id": "ObjectId",
  "content_id": 1,
  "title": "F1",
  "description": "A Formula One driver comes out of retirement to mentor and team up with a younger driver.",
  "type": 1,
  "vertical_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747494097_vuga_f1-vert.png",
  "horizontal_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747494098_vuga_f1-horiz.png",
  "ratings": 5.0,
  "release_year": 2025,
  "duration": "140:13",
  "total_view": 16,
  "total_download": 0,
  "total_share": 0,
  "is_featured": 1,
  "is_show": 1,
  "created_at": "2025-05-17T15:01:38Z",
  "updated_at": "2025-07-15T05:33:19Z",
  
  "language": {
    "id": 1,
    "title": "English",
    "code": "en"
  },
  
  "genres": [
    {
      "id": 3,
      "title": "Drama",
      "created_at": "2025-05-17T14:53:51Z"
    }
  ],
  
  "cast": [
    {
      "cast_id": "actor_3",
      "character_name": "John Smith",
      "order": 1,
      "actor_details": {
        "fullname": "Brad Pitt",
        "profile_image": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg"
      }
    }
  ],
  
  "sources": [
    {
      "source_id": 4,
      "title": "F1 Trailer 1",
      "source_url": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/f1_trailer_1.mp4",
      "type": 4,
      "quality": "1080HD",
      "size": "35MB",
      "is_download": 0,
      "access_type": 1
    },
    {
      "source_id": 5,
      "title": "F1 Trailer 2", 
      "source_url": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/f1_trailer_2.mp4",
      "type": 4,
      "quality": "1080HD",
      "size": "35MB",
      "is_download": 0,
      "access_type": 1
    },
    {
      "source_id": 6,
      "title": "F1 Trailer 3",
      "source_url": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/f1_trailer_3.mp4",
      "type": 4,
      "quality": "1080HD",
      "size": "35MB",
      "is_download": 0,
      "access_type": 1
    }
  ],
  
  "subtitles": [],
  
  "trailer_url": "8skLAmcQEX4",
  "tmdb_id": null,
  "imdb_id": null,
  
  "search_tags": ["f1", "formula one", "racing", "drama", "brad pitt", "2025"],
  "age_rating": null,
  "country": null,
  
  "seo": {
    "meta_title": "F1 - 2025 Formula One Movie",
    "meta_description": "A Formula One driver comes out of retirement to mentor and team up with a younger driver.",
    "keywords": ["f1", "formula one", "racing", "drama", "brad pitt"]
  }
}
```

## TV Series Document - "Ari Global Show" (Celebrity Interview Show)

```json
{
  "_id": "ObjectId",
  "content_id": 4,
  "title": "Ari Global Show",
  "description": "ARI GLOBAL SHOW - celebrity interview show featuring such stars as Jennifer Lopez, Ben Affleck, Matt Damon, Ryan Reynolds, Emma Stone, Eva Longoria, Gerard Butler, Samuel Jackson, Henry Cavill, Shaquille O'Neal, Ruby Rose, and others. https://gossip-stone.com/ari-global-show/",
  "type": 2,
  "vertical_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119439_vuga_ari_global_vert.png",
  "horizontal_poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119440_vuga_ari_global_horiz.png",
  "ratings": 5.0,
  "release_year": 2022,
  "duration": null,
  "total_view": 0,
  "total_download": 0,
  "total_share": 0,
  "is_featured": 1,
  "is_show": 1,
  "created_at": "2025-05-24T20:44:00Z",
  "updated_at": "2025-05-24T20:44:06Z",
  
  "language": {
    "id": 1,
    "title": "English",
    "code": "en"
  },
  
  "genres": [
    {
      "id": 2,
      "title": "Reality",
      "created_at": "2025-05-17T14:53:29Z"
    }
  ],
  
  "cast": [],
  
  "sources": [],
  
  "subtitles": [],
  
  "seasons": [
    {
      "season_id": 1,
      "season_number": 1,
      "title": "Season 1",
      "description": "Celebrity interview season featuring A-list stars",
      "poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119439_vuga_ari_global_vert.png",
      "trailer_url": "58uC20vDPGE",
      "release_date": "2025-05-24",
      
      "episodes": [
        {
          "episode_id": 1,
          "episode_number": 1,
          "title": "Episode 1",
          "description": "Jennifer Marc Wahlberg, Ben Affleck, Matt Damon, Anna Kendrick, Natti Natasha, Luis Fonsi, Alejandra",
          "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748119659_vuga_ari_global_thumb_s1e1.jpg",
          "duration": "10:00",
          "air_date": "2025-05-24",
          "total_view": 2,
          "total_download": 0,
          
          "sources": [
            {
              "source_id": 1,
              "title": "Ari Global S1E1",
              "source_url": "https://gossip-stone.nyc3.digitaloceanspaces.com/ios_video_feed/AriGlobal/mp4/ariglobal_s1e1.mp4",
              "type": 4,
              "quality": "1080HD",
              "size": "35MB",
              "is_download": 0,
              "access_type": 1
            }
          ],
          
          "subtitles": []
        },
        {
          "episode_id": 2,
          "episode_number": 2,
          "title": "Episode 2",
          "description": "Gerald Butler, Emma Stone, Emma Thompson, Amy Schumer, Diego Boneta, Nicky Jam, Eva Longoria, Pedro Capo, Eugenio Derbez",
          "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748120033_vuga_ari_global_roku_thumb_s1e2.jpg",
          "duration": "10:00",
          "air_date": "2025-05-24",
          "total_view": 2,
          "total_download": 0,
          
          "sources": [],
          
          "subtitles": []
        },
        {
          "episode_id": 3,
          "episode_number": 3,
          "title": "Episode 3",
          "description": "Henry Cavill, Ryan Reynolds, Samuel Jackson, Ivy Queen, CNCO, Michael Pena",
          "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1748120981_vuga_ari-global-s1-e3.jpg",
          "duration": "10:00",
          "air_date": "2025-05-24",
          "total_view": 2,
          "total_download": 0,
          
          "sources": [],
          
          "subtitles": []
        }
      ]
    }
  ],
  
  "trailer_url": "S44ZWwYu59Y",
  "tmdb_id": null,
  "imdb_id": null,
  
  "search_tags": ["ari global", "celebrity", "interview", "reality", "talk show", "jennifer lopez", "ben affleck", "matt damon", "ryan reynolds", "emma stone"],
  "age_rating": null,
  "country": "United States",
  
  "seo": {
    "meta_title": "Ari Global Show - Celebrity Interview Series",
    "meta_description": "Celebrity interview show featuring A-list stars like Jennifer Lopez, Ben Affleck, Matt Damon, and more.",
    "keywords": ["ari global", "celebrity interviews", "reality show", "talk show"]
  }
}
```

## Cast & Crew Document - "Brad Pitt"

```json
{
  "_id": "ObjectId", 
  "cast_id": "actor_3",
  "fullname": "Brad Pitt",
  "stage_name": "Brad Pitt",
  "biography": "William Bradley Pitt is an American actor and film producer. He has received multiple awards, including two Golden Globe Awards and an Academy Award for his acting, in addition to another Academy Award and a Primetime Emmy Award as producer under his production company, Plan B Entertainment.",
  "date_of_birth": "1963-12-18",
  "place_of_birth": "Shawnee, Oklahoma, USA",
  "nationality": "American",
  "gender": "Male",
  
  "profile_images": {
    "thumbnail": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg",
    "medium": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg",
    "large": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747492493_vuga_brad-pitt.jpeg"
  },
  
  "social_media": {
    "instagram": null,
    "twitter": null,
    "facebook": null,
    "imdb": "nm0000093"
  },
  
  "roles": ["Actor", "Producer"],
  "primary_role": "Actor",
  
  "filmography": [
    {
      "content_id": 1,
      "content_title": "F1",
      "character_name": "John Smith",
      "role_type": "Lead Actor",
      "year": 2025,
      "poster": "https://iosdev.nyc3.cdn.digitaloceanspaces.com/vuga/uploads/1747494097_vuga_f1-vert.png"
    }
  ],
  
  "awards": [],
  
  "statistics": {
    "total_movies": 1,
    "total_tv_shows": 0,
    "career_start_year": 1987,
    "total_awards_won": 0,
    "total_nominations": 0,
    "box_office_total": 0
  },
  
  "personal_details": {
    "height": "5'11\"",
    "eye_color": "Blue",
    "hair_color": "Blonde",
    "marital_status": "Divorced",
    "spouse": null,
    "children": 6
  },
  
  "agency_info": {
    "agent": null,
    "manager": null,
    "publicist": null
  },
  
  "is_active": true,
  "is_featured": true,
  "popularity_score": 95.0,
  "fan_rating": 4.8,
  
  "created_at": "2025-05-17T18:02:21Z",
  "updated_at": "2025-05-17T18:02:21Z",
  
  "search_tags": ["brad pitt", "actor", "producer", "hollywood", "a-list"],
  
  "seo": {
    "meta_title": "Brad Pitt - Actor Profile",
    "meta_description": "Complete filmography and biography of Brad Pitt, Academy Award-winning actor and producer.",
    "keywords": ["brad pitt", "actor", "producer", "hollywood", "movies"]
  }
}
```

## Key Observations from Real Data:

1. **File Storage**: Using DigitalOcean Spaces for media storage
2. **Content Types**: Movies (type 1) and TV Series (type 2)  
3. **Source Types**: Using type 4 for video files (not YouTube in this case)
4. **Real Genres**: Drama, Reality
5. **Actual Analytics**: Real view counts and engagement metrics
6. **Episode Structure**: Simple numbering system, 10-minute episodes
7. **Celebrity Content**: Focus on Hollywood celebrities and interviews

This real data shows your actual content structure and will help you build the Node.js API with the correct data patterns! 