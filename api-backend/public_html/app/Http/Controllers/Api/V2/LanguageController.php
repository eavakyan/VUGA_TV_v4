<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\AppLanguage;
use Illuminate\Http\Request;

class LanguageController extends Controller
{
    /**
     * Get all languages
     */
    public function getAllLanguages(Request $request)
    {
        $languages = AppLanguage::withCount(['contents' => function($query) {
                $query->where('is_show', 1);
            }])
            ->orderBy('title', 'asc')
            ->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Languages fetched successfully',
            'data' => $languages->map(function($language) {
                return [
                    'app_language_id' => $language->app_language_id,
                    'title' => $language->title,
                    'code' => $language->code,
                    'content_count' => $language->contents_count,
                    'created_at' => $language->created_at,
                    'updated_at' => $language->updated_at
                ];
            })
        ]);
    }
}