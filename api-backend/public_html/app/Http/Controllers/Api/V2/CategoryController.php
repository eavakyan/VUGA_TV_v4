<?php

namespace App\Http\Controllers\Api\V2;

use App\Http\Controllers\Controller;
use App\Models\V2\Category;
use App\Models\V2\Content;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class CategoryController extends Controller
{
    /**
     * Get all categorys
     */
    public function getAllCategorys(Request $request)
    {
        $categorys = Category::orderBy('title', 'asc')->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Categorys fetched successfully',
            'data' => $categorys
        ]);
    }
    
    /**
     * Get categorys with content count
     */
    public function getCategorysWithContentCount(Request $request)
    {
        $categorys = Category::withCount(['contents' => function($query) {
                $query->where('is_show', 1);
            }])
            ->having('contents_count', '>', 0)
            ->orderBy('title', 'asc')
            ->get();
        
        return response()->json([
            'status' => true,
            'message' => 'Categorys with content count fetched successfully',
            'data' => $categorys->map(function($category) {
                return [
                    'category_id' => $category->category_id,
                    'title' => $category->title,
                    'content_count' => $category->contents_count,
                    'created_at' => $category->created_at,
                    'updated_at' => $category->updated_at
                ];
            })
        ]);
    }
}