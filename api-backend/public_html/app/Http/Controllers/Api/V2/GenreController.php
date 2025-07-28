<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Genre;
use App\Models\V2\Content;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class GenreController extends Controller
{
    /**
     * Get all genres
     */
    public function getAllGenres(Request $request)
    {
        $genres = Genre::orderBy('title', 'asc')->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Genres fetched successfully',
            'data' => $genres
        ]);
    }
    
    /**
     * Get genres with content count
     */
    public function getGenresWithContentCount(Request $request)
    {
        $genres = Genre::withCount(['contents' => function($query) {
                $query->where('is_show', 1);
            }])
            ->having('contents_count', '>', 0)
            ->orderBy('title', 'asc')
            ->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Genres with content count fetched successfully',
            'data' => $genres->map(function($genre) {
                return [
                    'genre_id' => $genre->genre_id,
                    'title' => $genre->title,
                    'content_count' => $genre->contents_count,
                    'created_at' => $genre->created_at,
                    'updated_at' => $genre->updated_at
                ];
            })
        ]);
    }
}