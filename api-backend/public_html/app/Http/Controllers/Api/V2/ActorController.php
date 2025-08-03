<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Actor;
use App\Models\V2\ContentCast;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ActorController extends Controller
{
    /**
     * Get actor detail with their content
     */
    public function getActorDetail(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'actor_id' => 'required|integer|exists:actor,actor_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $actor = Actor::with(['contents' => function($query) {
                $query->where('is_show', 1)
                      ->with(['language', 'genres'])
                      ->orderBy('release_year', 'desc');
            }])
            ->find($request->actor_id);

        return response()->json([
            'status' => true,
            'message' => 'Actor detail fetched successfully',
            'data' => [
                'actor' => $actor,
                'contents' => $actor->contents->map(function($content) {
                    $data = $content->toArray();
                    // Add genre_ids for backward compatibility
                    $data['genre_ids'] = $content->genres->pluck('genre_id')->implode(',');
                    return $data;
                })
            ]
        ]);
    }
    
    /**
     * Get all actors with pagination
     */
    public function getAllActors(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'page' => 'integer|min:1',
            'per_page' => 'integer|min:1|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $perPage = $request->per_page ?? 20;
        $actors = Actor::withCount(['contents' => function($query) {
                $query->where('is_show', 1);
            }])
            ->orderBy('fullname', 'asc')
            ->paginate($perPage);

        return response()->json([
            'status' => true,
            'message' => 'Actors fetched successfully',
            'data' => $actors->items(),
            'pagination' => [
                'current_page' => $actors->currentPage(),
                'last_page' => $actors->lastPage(),
                'per_page' => $actors->perPage(),
                'total' => $actors->total()
            ]
        ]);
    }
    
    /**
     * V1 Compatible: Fetch actor details with all their content
     */
    public function fetchActorDetails(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'actor_id' => 'required|integer|exists:actor,actor_id',
            'profile_id' => 'nullable|integer|exists:app_user_profile,profile_id'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ]);
        }

        $actor = Actor::find($request->actor_id);
        if (!$actor) {
            return response()->json([
                'status' => false,
                'message' => 'Actor not found'
            ]);
        }

        // Get all content where this actor appears
        $profileId = $request->profile_id;
        $contents = $actor->contents()
            ->where('is_show', 1)
            ->with(['language', 'contentCast.actor', 'contentGenres.genre', 'ageLimits'])
            ->when($profileId, function($query) use ($profileId) {
                // Apply age restrictions if profile is provided
                return $query->filterByAgeRestrictions($profileId);
            })
            ->orderBy('release_year', 'desc')
            ->get();

        // Format the response for backward compatibility
        $actorData = $actor->toArray();
        $actorData['actorContent'] = $contents->map(function($content) use ($actor) {
            $data = [
                'id' => $content->content_id,
                'content_id' => $content->content_id,
                'title' => $content->title,
                'description' => $content->description,
                'type' => $content->type,
                'duration' => $content->duration,
                'release_year' => $content->release_year,
                'ratings' => $content->ratings,
                'language_id' => $content->language_id,
                'trailer_url' => $content->trailer_url,
                'vertical_poster' => $content->vertical_poster,
                'horizontal_poster' => $content->horizontal_poster,
                'genre_ids' => $content->genre_ids,
                'is_featured' => $content->is_featured,
                'total_view' => $content->total_view,
                'total_download' => $content->total_download,
                'total_share' => $content->total_share,
                'created_at' => $content->created_at,
                'updated_at' => $content->updated_at,
                'language' => $content->language,
                'character_name' => $content->pivot->character_name
            ];
            
            // Add age rating info
            if ($content->ageLimits->isNotEmpty()) {
                $ageLimit = $content->ageLimits->first();
                $data['age_rating'] = $ageLimit->code;
                $data['min_age'] = $ageLimit->min_age;
            }
            
            return $data;
        });

        return response()->json([
            'status' => true,
            'message' => 'Actor Details Fetched Successfully',
            'data' => $actorData
        ]);
    }
}