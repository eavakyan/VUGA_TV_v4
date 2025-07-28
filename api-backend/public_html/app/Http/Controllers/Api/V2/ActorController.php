<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Actor;
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
}