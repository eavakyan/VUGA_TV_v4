<?php

namespace App\Http\Controllers;

use App\Language;
use Illuminate\Http\Request;

class LanguageController extends Controller
{
    public function languages()
    {
        return view('languages');
    }

    public function languagesList(Request $request)
    {
        $limit = $request->input('length');
        $start = $request->input('start');
        $columns = ['id'];
        $orderColumn = $columns[$request->input('order.0.column', 0)]; 
        $orderDir = $request->input('order.0.dir', 'DESC');
        $searchValue = $request->input('search.value');

        $query = Language::query();

        $totalData = $query->count();

        if (!empty($searchValue)) {
            $query->where('title', 'LIKE', "%{$searchValue}%");
        }

        $totalFiltered = $query->count();

        $result = $query->orderBy($orderColumn, $orderDir)
                        ->offset($start)
                        ->limit($limit)
                        ->get();

        $data = $result->map(function ($item) {
            $edit = "<a rel='{$item->id}' data-title='{$item->title}' data-code='{$item->code}' class='me-2 btn btn-success px-3 text-white edit'>" . __('edit') . "</a>";

            $delete = "<a href='#' class='btn btn-danger px-3 text-white delete' rel='{$item->id}'>" . __('delete') . "</a>";

            $action = "<div class='text-end action'>{$edit}{$delete}</div>";

            return [
                $item->title,
                $item->code,
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


    public function addLanguage(Request $request)
    {
        if ($request->has('languageCode')) {
            $langCode = $request->languageCode;

            $existingLanguage = Language::where('code', $langCode)->first();

            $allLanguages = Language::orderBy('created_at', 'DESC')->get();

            if (!$existingLanguage) {
                $languageCodeValue = $this->getLanguageName($langCode);

                $newLanguage = new Language();
                $newLanguage->title = $languageCodeValue;
                $newLanguage->code = $langCode;
                $newLanguage->save();

                $allLanguages = Language::orderBy('created_at', 'DESC')->get();

                return response()->json([
                    'status' => true,
                    'message' => 'Language Added Successfully',
                    'data' => $newLanguage->id,
                    'languageCodeValue' => $languageCodeValue,
                    'allLanguages' => $allLanguages,
                ]);
            }
            
            return response()->json([
                'status' => true,
                'message' => 'Language Already Exists',
                'data' => $existingLanguage->id,
                'allLanguages' => $allLanguages,
            ]);
        } else {
            $language = new Language();
            $language->title = $request->title;
            $language->code = $request->code;
            $language->save();

            return response()->json([
                'status' => true,
                'message' => 'Language Added Successfully',
                'data' => $language,
            ]);
        }
    }
 

    public function getLanguageName($code)
    {
        $languageMapping = json_decode(file_get_contents(resource_path('lang/code.json')), true);
        return $languageMapping[$code] ?? 'Unknown';
    }



    public function updateLanguage(Request $request)
    {
        $language = Language::where('id', $request->language_id)->first();
        if ($language == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }

        $language->title = $request->title;
        $language->code = $request->code;
        $language->save();

        return response()->json([
            'status' => true,
            'message' => 'Language Updated Successfully',
            'data' => $language,
        ]);
    }

    public function deleteLanguage(Request $request)
    {
        $language = Language::where('id', $request->language_id)->first();
        if ($language == null) {
            return response()->json([
                'status' => false,
                'message' => 'Something Went Wrong',
            ]);
        }
        $language->delete();

        return response()->json([
            'status' => true,
            'message' => 'language Deleted Successfully',
            'data' => $language,
        ]);
    }
}
