<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Content;
use App\Models\V2\ContentTrailer;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class ContentTrailerController extends Controller
{
    /**
     * Get all trailers for a specific content
     */
    public function getContentTrailers(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $content = Content::find($request->content_id);
        
        if (!$content) {
            return response()->json([
                'status' => false,
                'message' => 'Content not found'
            ], 404);
        }

        $trailers = ContentTrailer::getContentTrailers($request->content_id);

        return response()->json([
            'status' => true,
            'message' => 'Trailers fetched successfully',
            'data' => $trailers->map(function($trailer) {
                return [
                    'content_trailer_id' => $trailer->content_trailer_id,
                    'content_id' => $trailer->content_id,
                    'title' => $trailer->title,
                    'youtube_id' => $trailer->youtube_id,
                    'trailer_url' => $trailer->trailer_url,
                    'embed_url' => $trailer->embed_url,
                    'watch_url' => $trailer->watch_url,
                    'thumbnail_url' => $trailer->thumbnail_url,
                    'is_primary' => $trailer->is_primary,
                    'sort_order' => $trailer->sort_order,
                    'created_at' => $trailer->created_at,
                    'updated_at' => $trailer->updated_at
                ];
            })
        ]);
    }

    /**
     * Get the primary trailer for a specific content
     */
    public function getPrimaryTrailer(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $trailer = ContentTrailer::getPrimaryTrailer($request->content_id);

        if (!$trailer) {
            return response()->json([
                'status' => false,
                'message' => 'No primary trailer found for this content'
            ], 404);
        }

        return response()->json([
            'status' => true,
            'message' => 'Primary trailer fetched successfully',
            'data' => [
                'content_trailer_id' => $trailer->content_trailer_id,
                'content_id' => $trailer->content_id,
                'title' => $trailer->title,
                'youtube_id' => $trailer->youtube_id,
                'trailer_url' => $trailer->trailer_url,
                'embed_url' => $trailer->embed_url,
                'watch_url' => $trailer->watch_url,
                'thumbnail_url' => $trailer->thumbnail_url,
                'is_primary' => $trailer->is_primary,
                'sort_order' => $trailer->sort_order,
                'created_at' => $trailer->created_at,
                'updated_at' => $trailer->updated_at
            ]
        ]);
    }

    /**
     * Add a new trailer to content
     */
    public function addTrailer(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
            'trailer_url' => 'required|string|url',
            'title' => 'nullable|string|max:255',
            'is_primary' => 'nullable|boolean',
            'sort_order' => 'nullable|integer|min:0'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            $trailer = ContentTrailer::createFromUrl(
                $request->content_id,
                $request->trailer_url,
                $request->title ?? 'Trailer',
                $request->is_primary ?? false,
                $request->sort_order ?? 0
            );

            return response()->json([
                'status' => true,
                'message' => 'Trailer added successfully',
                'data' => [
                    'content_trailer_id' => $trailer->content_trailer_id,
                    'content_id' => $trailer->content_id,
                    'title' => $trailer->title,
                    'youtube_id' => $trailer->youtube_id,
                    'trailer_url' => $trailer->trailer_url,
                    'embed_url' => $trailer->embed_url,
                    'watch_url' => $trailer->watch_url,
                    'thumbnail_url' => $trailer->thumbnail_url,
                    'is_primary' => $trailer->is_primary,
                    'sort_order' => $trailer->sort_order,
                    'created_at' => $trailer->created_at,
                    'updated_at' => $trailer->updated_at
                ]
            ], 201);

        } catch (\InvalidArgumentException $e) {
            return response()->json([
                'status' => false,
                'message' => $e->getMessage()
            ], 400);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to add trailer'
            ], 500);
        }
    }

    /**
     * Update an existing trailer
     */
    public function updateTrailer(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_trailer_id' => 'required|integer|exists:content_trailer,content_trailer_id',
            'title' => 'nullable|string|max:255',
            'trailer_url' => 'nullable|string|url',
            'is_primary' => 'nullable|boolean',
            'sort_order' => 'nullable|integer|min:0'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $trailer = ContentTrailer::find($request->content_trailer_id);

        if (!$trailer) {
            return response()->json([
                'status' => false,
                'message' => 'Trailer not found'
            ], 404);
        }

        try {
            // Update title if provided
            if ($request->has('title')) {
                $trailer->title = $request->title;
            }

            // Update trailer URL if provided
            if ($request->has('trailer_url')) {
                $youtubeId = ContentTrailer::extractYouTubeId($request->trailer_url);
                if (!$youtubeId) {
                    throw new \InvalidArgumentException('Invalid YouTube URL');
                }
                $trailer->youtube_id = $youtubeId;
                $trailer->trailer_url = $request->trailer_url;
            }

            // Update sort order if provided
            if ($request->has('sort_order')) {
                $trailer->sort_order = $request->sort_order;
            }

            $trailer->save();

            // Set as primary if requested
            if ($request->has('is_primary') && $request->is_primary) {
                $trailer->setPrimary();
            }

            return response()->json([
                'status' => true,
                'message' => 'Trailer updated successfully',
                'data' => [
                    'content_trailer_id' => $trailer->content_trailer_id,
                    'content_id' => $trailer->content_id,
                    'title' => $trailer->title,
                    'youtube_id' => $trailer->youtube_id,
                    'trailer_url' => $trailer->trailer_url,
                    'embed_url' => $trailer->embed_url,
                    'watch_url' => $trailer->watch_url,
                    'thumbnail_url' => $trailer->thumbnail_url,
                    'is_primary' => $trailer->is_primary,
                    'sort_order' => $trailer->sort_order,
                    'created_at' => $trailer->created_at,
                    'updated_at' => $trailer->updated_at
                ]
            ]);

        } catch (\InvalidArgumentException $e) {
            return response()->json([
                'status' => false,
                'message' => $e->getMessage()
            ], 400);
        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to update trailer'
            ], 500);
        }
    }

    /**
     * Delete a trailer
     */
    public function deleteTrailer(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_trailer_id' => 'required|integer|exists:content_trailer,content_trailer_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $trailer = ContentTrailer::find($request->content_trailer_id);

        if (!$trailer) {
            return response()->json([
                'status' => false,
                'message' => 'Trailer not found'
            ], 404);
        }

        $trailer->delete();

        return response()->json([
            'status' => true,
            'message' => 'Trailer deleted successfully'
        ]);
    }

    /**
     * Set a trailer as primary
     */
    public function setPrimaryTrailer(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_trailer_id' => 'required|integer|exists:content_trailer,content_trailer_id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        $trailer = ContentTrailer::find($request->content_trailer_id);

        if (!$trailer) {
            return response()->json([
                'status' => false,
                'message' => 'Trailer not found'
            ], 404);
        }

        $trailer->setPrimary();

        return response()->json([
            'status' => true,
            'message' => 'Trailer set as primary successfully'
        ]);
    }

    /**
     * Reorder trailers for a content
     */
    public function reorderTrailers(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'content_id' => 'required|integer|exists:content,content_id',
            'trailer_orders' => 'required|array',
            'trailer_orders.*.content_trailer_id' => 'required|integer|exists:content_trailer,content_trailer_id',
            'trailer_orders.*.sort_order' => 'required|integer|min:0'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => false,
                'message' => $validator->errors()->first()
            ], 400);
        }

        try {
            foreach ($request->trailer_orders as $order) {
                ContentTrailer::where('content_trailer_id', $order['content_trailer_id'])
                    ->where('content_id', $request->content_id)
                    ->update(['sort_order' => $order['sort_order']]);
            }

            return response()->json([
                'status' => true,
                'message' => 'Trailers reordered successfully'
            ]);

        } catch (\Exception $e) {
            return response()->json([
                'status' => false,
                'message' => 'Failed to reorder trailers'
            ], 500);
        }
    }
}