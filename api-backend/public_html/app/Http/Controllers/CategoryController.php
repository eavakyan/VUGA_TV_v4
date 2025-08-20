<?php

namespace App\Http\Controllers;

use App\Category;
use Illuminate\Http\Request;

class CategoryController extends Controller
{
    public function categories()
    {
        return view('categories');
    }

    public function categoriesList(Request $request)
    {
        $query = Category::withCount('contents');
        $totalData = $query->count();

        $columns = ['category_id'];

        $limit = $request->input('length');
        $start = $request->input('start');
        $orderColumn = $columns[$request->input('order.0.column')];
        $orderDir = $request->input('order.0.dir');
        $searchValue = $request->input('search.value');

        if (!empty($searchValue)) {
            $query->where('title', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy('title', 'asc')
            ->offset($start)
            ->limit($limit)
            ->get();

        $data = $result->map(function ($item) {
            $edit = "<a rel='{$item->category_id}' data-title='{$item->title}' class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";
            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->category_id}'>" . __('delete') . "</a>";
            $action = "<div class='text-end action'>{$edit}{$delete}</div>";
            $badgeClass = $item->contents_count > 0 ? 'bg-danger' : 'bg-secondary';
            $titleWithCount = $item->title . ' <span class="badge ' . $badgeClass . ' ms-2">' . $item->contents_count . '</span>';
            return [
                $titleWithCount,
                $action,
            ];
        });

        $json_data = [
            "draw" => intval($request->input('draw')),
            "recordsTotal" => intval($totalData),
            "recordsFiltered" => intval($totalFiltered),
            "data" => $data,
        ];

        return response()->json($json_data);
    }

    public function addCategory(Request $request)
    {
         
        if ($request->has('categorys')) {
           
            $categorysString = $request->categorys;
            $categorys = explode(', ', $categorysString);

            $category_ids = [];

            foreach ($categorys as $categoryName) {
                $categoryName = trim($categoryName);
                $existingCategory = Category::where('title', $categoryName)->first();

                if (!$existingCategory) {
                    $existingCategory = new Category();
                    $existingCategory->title = $categoryName;
                    $existingCategory->save();
                } 

                array_push($category_ids, $existingCategory);
            }

            $allCategorys = Category::orderBy('created_at', 'DESC')->get();
            
            return response()->json([
                'status' => true,
                'message' => 'Categorys processed successfully',
                'data' => $category_ids,
                'allCategorys' => $allCategorys,
            ]);
        } else {
            
            $category = new Category();
            $category->title = $request->title;
            $category->save();

            return response()->json([
                'status' => true,
                'message' => 'Category Added Successfully',
                'data' => $category,
            ]);
        }
    }


    public function updateCategory(Request $request)
    {
        $category = Category::where('category_id', $request->category_id)->first();
        if ($category == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $category->title = $request->title;
        $category->save();

        return response()->json([
            'status' => true,
            'message' => 'Category Updated Successfully',
            'data' => $category,
        ]);
    }

    public function deleteCategory(Request $request)
    {
        $category = Category::where('category_id', $request->category_id)->first();
        if ($category == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        $category->delete();
        
        return response()->json([
            'status' => true,
            'message' => 'Category Deleted Successfully',
            'data' => $category,
        ]);
    }
 
}
