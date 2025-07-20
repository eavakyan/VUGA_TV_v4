<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ContentResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'title' => $this->title,
            'description' => $this->description,
            'poster' => $this->poster,
            'backdrop' => $this->backdrop,
            'trailer_url' => $this->trailer_url,
            'type' => $this->type,
            'type_name' => $this->type == 1 ? 'Movie' : 'Series',
            'release_date' => $this->release_date,
            'rating' => $this->rating,
            'duration' => $this->duration,
            'year' => $this->year,
            'language_id' => $this->language_id,
            'genre_ids' => $this->genre_ids,
            'genres' => $this->whenLoaded('genres'),
            'is_featured' => (bool) $this->is_featured,
            'is_watchlist' => $this->when(isset($this->is_watchlist), (bool) $this->is_watchlist),
            'total_view' => $this->total_view ?? 0,
            'total_download' => $this->total_download ?? 0,
            'total_share' => $this->total_share ?? 0,
            'created_at' => $this->created_at?->toISOString(),
            'updated_at' => $this->updated_at?->toISOString(),
            
            // Conditional includes for detailed views
            'cast' => $this->whenLoaded('contentCast', function() {
                return ActorResource::collection($this->contentCast);
            }),
            'sources' => $this->whenLoaded('sources', function() {
                return ContentSourceResource::collection($this->sources);
            }),
            'subtitles' => $this->whenLoaded('subtitles', function() {
                return SubtitleResource::collection($this->subtitles);
            }),
            'seasons' => $this->whenLoaded('seasons', function() {
                return SeasonResource::collection($this->seasons);
            }),
            'more_like_this' => $this->when(isset($this->more_like_this), function() {
                return ContentResource::collection($this->more_like_this);
            }),
        ];
    }
} 